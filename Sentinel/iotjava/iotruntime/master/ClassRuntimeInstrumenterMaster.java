package iotruntime.master;

// Java basic packages
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.sql.*;
import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ASM packages
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.TypePath;

// More imports
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTRelation;

/** Class ClassRuntimeInstrumenterMaster helps instrument the bytecode.
 *  This class basically reads annotations @config in the code,
 *  allocates the right objects, and runs the function init in
 *  the instrumented program code.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */
public final class ClassRuntimeInstrumenterMaster extends ClassVisitor implements Opcodes {

	/**
	 *  ClassRuntimeInstrumenterMaster class properties
	 *  <p>
	 *  At most we will have 3 class types
	 *  IoTSet<class_type> -> type 1: Set and type 2: class_type
	 *  IoTRelation<class_type1, class_type2>
	 *  -> type 1: IoTRelation, type 2: class_type1, type 3: class_type2
	 *  Store class type for annotation processing in strClassType
	 */
	private String[] strClassType;
	private static int iCntClassType = 0;
	private String strInstrumentedClassName;
	private String strFieldName;
	private HashMap<String,Object> hmObj;
	private String strObjectID;
	private boolean bVerbose;

	/**
	 * ClassRuntimeInstrumenterMaster class constants
	 */
	private static final int INT_NUM_CLASS_TYPE = 3;
	//private static final String STR_IOT_ANNOTATION_SIGNATURE = "Liotchecker/qual/config;";
	private static final String STR_IOT_ANNOTATION_SIGNATURE = "Liotcode/annotation/config;";
	private static final String STR_IOT_SET_SIGNATURE = "Liotruntime/slave/IoTSet;";
	private static final String STR_IOT_RELATION_SIGNATURE = "Liotruntime/slave/IoTRelation;";
	private static final String STR_IOT_CONSTRAINT_SIGNATURE = "Liotcode/annotation/constraint;";
	private static final String STR_CONFIG_EXTENSION = ".config";
	private static final String STR_CONFIG_FILE_PATH = "mysql/";

	/**
	 * Main Constructor
	 */
	public ClassRuntimeInstrumenterMaster(final ClassVisitor cv, String strObjID, boolean _bVerbose) {

		super(ASM5, cv);

		// Initialize strClassType
		strClassType = new String[INT_NUM_CLASS_TYPE];
		for(int i=0; i<INT_NUM_CLASS_TYPE; i++) {
			strClassType[i] = new String();
		}
		strInstrumentedClassName = "";
		strFieldName = "";
		hmObj = new HashMap<String,Object>();
		strObjectID = strObjID;
		bVerbose = _bVerbose;
	}

	/**
	 * Make a visit to a class. This is the method called first.
	 */
	@Override
	public void visit(int version, int access, String name,
		String signature, String superName, String[] interfaces) {

		// Get the name of this instrumented class
		strInstrumentedClassName = name;

		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Make a visit to a field.
	 */
	@Override
	public FieldVisitor visitField(int access, String name,
		String desc, String signature, Object value) {

		strFieldName = name;
		super.visitField(access, name, desc, signature, value);

		if (signature != null) {
			new SignatureReader(signature)
			.accept(new SignatureRuntimeInstrumenter(strClassType));
		}
		iCntClassType = 0;
		return new FieldRuntimeInstrumenter(signature, desc);
	}

	/**
	 * Visit a method when a method is encountered
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name,
		String desc, String signature, String[] exceptions) {

		super.visitMethod(access, name, desc, signature, exceptions);
		return new MethodRuntimeInstrumenter();
	}

	/**
	 * A subclass that visits signature. This is called when traversing a class
	 * and a signature visit is needed.
	 */
	private class SignatureRuntimeInstrumenter extends SignatureVisitor {

		public SignatureRuntimeInstrumenter(String[] strCType) {
			super(Opcodes.ASM5);

			// Counter for extracting +class type
			iCntClassType = 0;

			// Initializing input String array
			for (int i=0; i<INT_NUM_CLASS_TYPE; i++) {
				strClassType[i] = strCType[i];
			}
		}

		@Override
		public void visitClassType(final String name) {

			strClassType[iCntClassType++] = name;
			super.visitClassType(name);
		}
	}

	/**
	 * A helper function that returns class name from class type pattern
	 * <p>
	 * e.g. get "ProximitySensor" from "iotcode/ProximitySensor"
	 * With this regex pattern,
	 * group(0) gives the entire input string, e.g. "iotcode/ProximitySensor"
	 * group(1) gives just the front string, e.g. "iotcode"
	 * group(2) gives just the slash, e.g. "/"
	 * group(3) gives just the back string, e.g. "ProximitySensor"
	 *
	 * @param  strInput  String to be matched by regex
	 * @return           String
	 */
	public String getClassName(String strInput) {

		Pattern pattern = Pattern.compile("(\\S+)(\\/)(\\S+)");
		Matcher matcher = pattern.matcher(strInput);
		if (matcher.find()) {
			return matcher.group(3);
		}
		return null;
	}

	/**
	 * A class that instruments instructions in method through visiting a method.
	 * Instruction and variable types can be extracted.
	 */
	class FieldRuntimeInstrumenter extends FieldVisitor {

		private String strFieldSignature;
		private String strFieldDesc;

		public FieldRuntimeInstrumenter(String strFSign, String strFDesc) {

			super(Opcodes.ASM5);
			strFieldSignature = strFSign;
			strFieldDesc = strFDesc;
		}

		/**
		 *  This method visits annotation, so we can instrument @config here
		 *  <p>
		 *  Steps:
		 *  1) Check whether it is IoTSet or IoTRelation declaration
		 *  2) Create a new Set class instrumenter
		 *     strClassType[0] will contain "IoTSet" or "IoTRelation"
		 *     strClassType[1] will contain the first specific class
		 *         e.g. IoTSet<ProximitySensor> -> strClassType[1] == "ProximitySensor"
		 *     strClassType[2] will contain the other specific class
		 *     (doesn't apply to IoTSet)
		 *         e.g. IoTRelation<ProximitySensor, LightBulb>
		 *              -> strClassType[1] == "ProximitySensor"
		 *              -> strClassType[2] == "LightBulb"
		 *  3) Instantiate field objects, e.g. IoTSet or IoTRelation class object
		 *  4) Instantiate a new object of the instrumented class, e.g. AcmeThermostat
		 *  5) Initialize the field of the instrumented class objects with actual field objects
		 *  6) Run the init() function of the instrumented class
		 *
		 * @param  desc     String
		 * @param  visible  boolean
		 * @return          AnnotationVisitor
		 */
		/**
		 *  This method visits annotation that is meta-annotated as TYPE_USE, so we can instrument @config here
		 */
		@Override
		public AnnotationVisitor visitAnnotation(String desc,
			boolean visible) {

			RuntimeOutput.print("ClassRuntimeInstrumenterMaster@AnnotationVisitor: " + desc, bVerbose);

			// Check annotation description @config
			if(desc.equals(STR_IOT_ANNOTATION_SIGNATURE)) {
				// Check if this is a Set class, then process it
				if (strFieldDesc.equals(STR_IOT_SET_SIGNATURE)) {
					RuntimeOutput.print("@config: IoTSet is detected!", bVerbose);
					SetInstrumenter setInstrument = new
						SetInstrumenter(getClassName(strClassType[1]),
							STR_CONFIG_FILE_PATH + strFieldName + STR_CONFIG_EXTENSION, strObjectID, bVerbose);

					hmObj.put(strFieldName, setInstrument);
					// Check if this is a Relation class, then process it
				} else if (strFieldDesc.equals(STR_IOT_RELATION_SIGNATURE)) {
					RuntimeOutput.print("@config: IoTRelation is detected!", bVerbose);
					RelationInstrumenter relInstrument = new
						RelationInstrumenter(getClassName(strClassType[1]),
							getClassName(strClassType[2]), STR_CONFIG_FILE_PATH +
							strFieldName + STR_CONFIG_EXTENSION, bVerbose);

					hmObj.put(strFieldName, relInstrument);
				} else if (strFieldDesc.equals(STR_IOT_CONSTRAINT_SIGNATURE)) {
					// TO DO: PROCESS THIS CONSTRAINT ANNOTATION
					RuntimeOutput.print("ClassRuntimeInstrumenterMaster@AnnotationTypeVisitor: Constraint annotation detected!", bVerbose);

				} else {
					throw new Error("ClassRuntimeInstrumenterMaster@AnnotationTypeVisitor: " + strFieldDesc + " not recognized!");
				}
			}
			return super.visitAnnotation(desc, visible);
		}
	}

	/**
	 * A subclass that instruments instructions in method through visiting a method.
	 * Instruction and variable types can be extracted.
	 */
	protected class MethodRuntimeInstrumenter extends MethodVisitor {

		public MethodRuntimeInstrumenter() {
			super(Opcodes.ASM5);
		}

	}

	/**
	 * A method that returns HashMap hmObj
	 *
	 * @return         HashMap<String,Object>
	 */
	public HashMap<String,Object> getFieldObjects() {

		return hmObj;
	}

	public static void main(String[] args) {

		try {
			// Instrumenting one file
			FileInputStream is = new FileInputStream(args[0]);

			ClassReader cr = new ClassReader(is);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassRuntimeInstrumenterMaster crim = new ClassRuntimeInstrumenterMaster(cw, "LB2", true);
			cr.accept(crim, 0);

			// Get the object and the class names
			HashMap<String,Object> hm = crim.getFieldObjects();
			for(Map.Entry<String,Object> map : hm.entrySet()) {
				System.out.println(map.getKey());
				System.out.println(map.getValue().getClass().getName());
				SetInstrumenter si = (SetInstrumenter) map.getValue();
				System.out.println("Field values: " + Arrays.toString(si.fieldValues(0)));
				System.out.println("Field classes: " + Arrays.toString(si.fieldClasses(0)));
				System.out.println("Field object ID: " + si.fieldObjectID(0));
				System.out.println("Field entry type: " + si.fieldEntryType("LB1"));
				System.out.println("Field entry type: " + si.fieldEntryType("LB2"));
				System.out.println("Number of rows: " + si.numberOfRows());
			}

		} catch (IOException ex) {
			System.out.println("ClassRuntimeInstrumenterMaster@RunInstrumentation: IOException: "
												 + ex.getMessage());
			ex.printStackTrace();
		}
	}
}
