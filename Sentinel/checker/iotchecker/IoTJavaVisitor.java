package iotchecker;

import iotchecker.qual.*;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.InternalUtils;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.util.PurityChecker;
import org.checkerframework.dataflow.util.PurityChecker.PurityResult;
import org.checkerframework.dataflow.util.PurityUtils;
import org.checkerframework.framework.qual.FieldIsExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collection;
import java.lang.annotation.Annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic.Kind;

import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.ReturnTree;

import java.io.Serializable;
import java.lang.reflect.*;
import java.net.*;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

/** Class IoTJavaVisitor is a class that extends
 *  BaseTypeVisitor. The purpose of this class is to
 *  annotate variables/objects with the right annotations,
 *  i.e. Normal, LocalRemote, NonLocalRemote, or CanBeRemote
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @author      Bin Xu <xub3 @ uci.edu>
 * @version     1.0
 * @since       2016-03-25
 */
public final class IoTJavaVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

	public IoTJavaVisitor(BaseTypeChecker checker) {
		super(checker);
	}


	/**
	 * This visitVariable() method is taken directly from BaseTypeVisitor
	 * <p>
	 * We need to override the checkArguments() to not complain about the switch
	 * from @LocalRemote to @NonLocalRemote.
	 */
	@Override
	public Void visitVariable(VariableTree node, Void p) {
		Pair<Tree, AnnotatedTypeMirror> preAssCtxt = visitorState.getAssignmentContext();
		visitorState.setAssignmentContext(Pair.of((Tree) node, atypeFactory.getAnnotatedType(node)));

		try {
			// Check and reject any existence/declaration of Java Reflection and Java Networking
			checkIfJavaReflection(node);
			checkIfJavaNetworking(node);

			return null;
		} finally {
			visitorState.setAssignmentContext(preAssCtxt);
		}
	}


	/**
	 * Return the AnnotatedTypeMirror of the node
	 * This handles normal variables and array type
	 */
	protected AnnotatedTypeMirror getAnnotatedTypeUniversal(Tree treeNode) {
		AnnotatedTypeMirror atmType = atypeFactory.getAnnotatedType(treeNode);
		// Check the object annotation - an array type is treated differently

		return (atmType.getKind() == TypeKind.ARRAY?((AnnotatedArrayType) atmType).getComponentType():atmType);
	}


	/**
	 * Helper function to check if the variable is a part of Java Reflection
	 */
	protected Void checkIfJavaReflection(VariableTree varTree) {
		if (isJavaReflection(varTree)) {
			checker.report(Result.failure("reflection.found", varTree), varTree);
		}

		return null;
	}


	/**
	 * Helper function to check if the variable is a part of Java Networking
	 */
	protected Void checkIfJavaNetworking(VariableTree varTree) {
		if (isJavaNetworking(varTree)) {
			checker.report(Result.failure("java.net.found", varTree), varTree);
		}

		return null;
	}


	/**
	 * Helper function to check if the variable is a part of Java Networking library
	 * <p>
	 * i.e. java.net.XXX classes
	 */
	protected boolean isJavaNetworking(VariableTree varTree) {
		AnnotatedTypeMirror typeVar = getAnnotatedTypeUniversal(varTree);
		Class<?> varClass = typeMirrorToClass(typeVar.getUnderlyingType());

		/* An alternative is to list down the class names
		   like the following

		   if ((varClass == InetAddress.class)       ||
		    (varClass == DatagramSocket.class)    ||
		    (varClass == DatagramPacket.class)    ||
		    (varClass == URL.class)               ||
		    (varClass == HttpURLConnection.class) ||
		    (varClass == Socket.class)            ||
		    (varClass == ServerSocket.class)) {
		 */

		// We still allow the java.net.*Exception classes
		// to be allowed in a try { } catch { } structure
		return ((varClass != null) &&
						varClass.toString().contains("java.net") &&
						!varClass.toString().contains("Exception"));
	}


	/**
	 * Helper function to check if the variable is a part of Java Reflection
	 * <p>
	 * i.e. java.lang.reflect.XXX classes
	 */
	protected boolean isJavaReflection(VariableTree varTree) {
		AnnotatedTypeMirror typeVar = getAnnotatedTypeUniversal(varTree);
		Class<?> varClass = typeMirrorToClass(typeVar.getUnderlyingType());

		/* An alternative is to list down the class names
		   like the following

		   if ((varClass == Class.class)       ||
		    (varClass == Constructor.class) ||
		    (varClass == Method.class)      ||
		    (varClass == Type.class)) {
		 */

		return ((varClass != null) &&
						(varClass.toString().contains("java.lang.reflect")));
	}


	/**
	 * Helper method/function adapted from FormatterTreeUtil.java / I18nFormatterTreeUtil.java
	 * to find a basic type of a variable
	 */
	protected final Class<?extends Object> typeMirrorToClass(final TypeMirror type) {
		return type.accept(new SimpleTypeVisitor7<Class<?extends Object>, Class<Void> >() {
			@Override
			public Class<?extends Object> visitPrimitive(PrimitiveType t, Class<Void> v) {
				switch (t.getKind()) {
				case BOOLEAN:
					return Boolean.class;

				case BYTE:
					return Byte.class;

				case CHAR:
					return Character.class;

				case SHORT:
					return Short.class;

				case INT:
					return Integer.class;

				case LONG:
					return Long.class;

				case FLOAT:
					return Float.class;

				case DOUBLE:
					return Double.class;

				default:
					return null;
				}
			}

			@Override
			public Class<?extends Object> visitDeclared(DeclaredType dt, Class<Void> v) {
				return dt.asElement().accept(new SimpleElementVisitor7<Class<?extends Object>, Class<Void> >() {
					@Override
					public Class<?extends Object> visitType(TypeElement e, Class<Void> v) {
						try {
							return Class.forName(e.getQualifiedName().toString());
						} catch (ClassNotFoundException e1) {
							return null;																																																																																																																					// the lookup should work for all the
							// classes we care about
						}
					}
				}, Void.TYPE);
			}
		}, Void.TYPE);
	}
}
