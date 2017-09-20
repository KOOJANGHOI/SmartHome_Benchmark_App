package iotpolicy;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ScannerBuffer;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iotpolicy.parser.Lexer;
import iotpolicy.parser.Parser;
import iotpolicy.tree.ParseNode;
import iotpolicy.tree.ParseNodeVector;
import iotpolicy.tree.ParseTreeHandler;
import iotpolicy.tree.Declaration;
import iotpolicy.tree.DeclarationHandler;
import iotpolicy.tree.CapabilityDecl;
import iotpolicy.tree.InterfaceDecl;
import iotpolicy.tree.RequiresDecl;
import iotpolicy.tree.EnumDecl;
import iotpolicy.tree.StructDecl;

import iotrmi.Java.IoTRMITypes;


/** Class IoTCompiler is the main interface/stub compiler for
 *  files generation. This class calls helper classes
 *  such as Parser, Lexer, InterfaceDecl, CapabilityDecl,
 *  RequiresDecl, ParseTreeHandler, etc.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-22
 */
public class IoTCompiler {

	/**
	 * Class properties
	 */
	// Maps multiple interfaces to multiple objects of ParseTreeHandler
	private Map<String,ParseTreeHandler> mapIntfacePTH;
	private Map<String,DeclarationHandler> mapIntDeclHand;
	private Map<String,Map<String,Set<String>>> mapInt2NewInts;
	private Map<String,String> mapInt2NewIntName;
	// Data structure to store our types (primitives and non-primitives) for compilation
	private Map<String,String> mapPrimitives;
	private Map<String,String> mapNonPrimitivesJava;
	private Map<String,String> mapNonPrimitivesCplus;
	// Other data structures
	private Map<String,Integer> mapIntfaceObjId;		// Maps interface name to object Id
	private Map<String,Integer> mapNewIntfaceObjId;		// Maps new interface name to its object Id (keep track of stubs)
	private PrintWriter pw;
	private String dir;
	private String subdir;
	private Map<String,Integer> mapPortCount;	// Counter for ports
	private static int portCount = 0;
	private static int countObjId = 1;			// Always increment object Id for a new stub/skeleton
	private String mainClass;


	/**
	 * Class constants
	 */
	private final static String OUTPUT_DIRECTORY = "output_files";

	private enum ParamCategory {

		PRIMITIVES,		// All the primitive types, e.g. byte, short, int, long, etc.
		NONPRIMITIVES,	// Non-primitive types, e.g. Set, Map, List, etc.
		ENUM,			// Enum type
		STRUCT,			// Struct type
		USERDEFINED		// Assumed as driver classes
	}


	/**
	 * Class constructors
	 */
	public IoTCompiler() {

		mapIntfacePTH = new HashMap<String,ParseTreeHandler>();
		mapIntDeclHand = new HashMap<String,DeclarationHandler>();
		mapInt2NewInts = new HashMap<String,Map<String,Set<String>>>();
		mapInt2NewIntName = new HashMap<String,String>();
		mapIntfaceObjId = new HashMap<String,Integer>();
		mapNewIntfaceObjId = new HashMap<String,Integer>();
		mapPrimitives = new HashMap<String,String>();
			arraysToMap(mapPrimitives, IoTRMITypes.primitivesJava, IoTRMITypes.primitivesCplus);
		mapNonPrimitivesJava = new HashMap<String,String>();
			arraysToMap(mapNonPrimitivesJava, IoTRMITypes.nonPrimitivesJava, IoTRMITypes.nonPrimitiveJavaLibs);
		mapNonPrimitivesCplus = new HashMap<String,String>();
			arraysToMap(mapNonPrimitivesCplus, IoTRMITypes.nonPrimitivesJava, IoTRMITypes.nonPrimitivesCplus);
		mapPortCount = new HashMap<String,Integer>();
		pw = null;
		dir = OUTPUT_DIRECTORY;
		subdir = null;
		mainClass = null;
	}


	/**
	 * setDataStructures() sets parse tree and other data structures based on policy files.
	 * <p>
	 * It also generates parse tree (ParseTreeHandler) and
	 * copies useful information from parse tree into
	 * InterfaceDecl, CapabilityDecl, and RequiresDecl 
	 * data structures.
	 * Additionally, the data structure handles are
	 * returned from tree-parsing for further process.
	 */
	public void setDataStructures(String origInt, ParseNode pnPol, ParseNode pnReq) {

		ParseTreeHandler ptHandler = new ParseTreeHandler(origInt, pnPol, pnReq);
		DeclarationHandler decHandler = new DeclarationHandler();
		// Process ParseNode and generate Declaration objects
		// Interface
		ptHandler.processInterfaceDecl();
		InterfaceDecl intDecl = ptHandler.getInterfaceDecl();
		decHandler.addInterfaceDecl(origInt, intDecl);
		// Capabilities
		ptHandler.processCapabilityDecl();
		CapabilityDecl capDecl = ptHandler.getCapabilityDecl();
		decHandler.addCapabilityDecl(origInt, capDecl);
		// Requires
		ptHandler.processRequiresDecl();
		RequiresDecl reqDecl = ptHandler.getRequiresDecl();
		decHandler.addRequiresDecl(origInt, reqDecl);
		// Enumeration
		ptHandler.processEnumDecl();
		EnumDecl enumDecl = ptHandler.getEnumDecl();
		decHandler.addEnumDecl(origInt, enumDecl);
		// Struct
		ptHandler.processStructDecl();
		StructDecl structDecl = ptHandler.getStructDecl();
		decHandler.addStructDecl(origInt, structDecl);

		mapIntfacePTH.put(origInt, ptHandler);
		mapIntDeclHand.put(origInt, decHandler);
		// Set object Id counter to 0 for each interface
		mapIntfaceObjId.put(origInt, countObjId++);
	}


	/**
	 * getMethodsForIntface() reads for methods in the data structure
	 * <p>
	 * It is going to give list of methods for a certain interface
	 * 		based on the declaration of capabilities.
	 */
	public void getMethodsForIntface(String origInt) {

		ParseTreeHandler ptHandler = mapIntfacePTH.get(origInt);
		Map<String,Set<String>> mapNewIntMethods = new HashMap<String,Set<String>>();
		// Get set of new interfaces, e.g. CameraWithCaptureAndData
		// Generate this new interface with all the methods it needs
		//		from different capabilities it declares
		DeclarationHandler decHandler = mapIntDeclHand.get(origInt);
		RequiresDecl reqDecl = (RequiresDecl) decHandler.getRequiresDecl(origInt);
		Set<String> setIntfaces = reqDecl.getInterfaces();
		for (String strInt : setIntfaces) {

			// Initialize a set of methods
			Set<String> setMethods = new HashSet<String>();
			// Get list of capabilities, e.g. ImageCapture, VideoRecording, etc.
			List<String> listCapab = reqDecl.getCapabList(strInt);
			for (String strCap : listCapab) {

				// Get list of methods for each capability
				CapabilityDecl capDecl = (CapabilityDecl) decHandler.getCapabilityDecl(origInt);
				List<String> listCapabMeth = capDecl.getMethods(strCap);
				for (String strMeth : listCapabMeth) {

					// Add methods into setMethods
					// This is to also handle redundancies (say two capabilities
					//		share the same methods)
					setMethods.add(strMeth);
				}
			}
			// Add interface and methods information into map
			mapNewIntMethods.put(strInt, setMethods);
			// Map new interface method name to the original interface
			// TODO: perhaps need to check in the future if we have more than 1 stub interface for one original interface
			mapInt2NewIntName.put(origInt, strInt);
			if (mainClass == null)	// Take the first class as the main class (whichever is placed first in the order of compilation files)
				mainClass = origInt;
		}
		// Map the map of interface-methods to the original interface
		mapInt2NewInts.put(origInt, mapNewIntMethods);
	}

	
/*================
 * Java generation
 *================/

	/**
	 * HELPER: writeMethodJavaLocalInterface() writes the method of the local interface
	 */
	private void writeMethodJavaLocalInterface(Collection<String> methods, InterfaceDecl intDecl) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			print("public " + intDecl.getMethodType(method) + " " +
				intDecl.getMethodId(method) + "(");
			for (int i = 0; i < methParams.size(); i++) {
				// Check for params with driver class types and exchange it 
				// 		with its remote interface
				String paramType = checkAndGetParamClass(methPrmTypes.get(i));
				print(paramType + " " + methParams.get(i));
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(");");
		}
	}


	/**
	 * HELPER: writeMethodJavaInterface() writes the method of the interface
	 */
	private void writeMethodJavaInterface(Collection<String> methods, InterfaceDecl intDecl) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			print("public " + intDecl.getMethodType(method) + " " +
				intDecl.getMethodId(method) + "(");
			for (int i = 0; i < methParams.size(); i++) {
				// Check for params with driver class types and exchange it 
				// 		with its remote interface
				String paramType = methPrmTypes.get(i);
				print(paramType + " " + methParams.get(i));
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(");");
		}
	}


	/**
	 * HELPER: generateEnumJava() writes the enumeration declaration
	 */
	private void generateEnumJava() throws IOException {

		// Create a new directory
		createDirectory(dir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Get the right EnumDecl
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			EnumDecl enumDecl = (EnumDecl) decHandler.getEnumDecl(intface);
			Set<String> enumTypes = enumDecl.getEnumDeclarations();
			// Iterate over enum declarations
			for (String enType : enumTypes) {
				// Open a new file to write into
				FileWriter fw = new FileWriter(dir + "/" + enType + ".java");
				pw = new PrintWriter(new BufferedWriter(fw));
				println("public enum " + enType + " {");
				List<String> enumMembers = enumDecl.getMembers(enType);
				for (int i = 0; i < enumMembers.size(); i++) {

					String member = enumMembers.get(i);
					print(member);
					// Check if this is the last element (don't print a comma)
					if (i != enumMembers.size() - 1)
						println(",");
					else
						println("");
				}
				println("}\n");
				pw.close();
				System.out.println("IoTCompiler: Generated enum class " + enType + ".java...");
			}
		}
	}


	/**
	 * HELPER: generateStructJava() writes the struct declaration
	 */
	private void generateStructJava() throws IOException {

		// Create a new directory
		createDirectory(dir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Get the right StructDecl
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			StructDecl structDecl = (StructDecl) decHandler.getStructDecl(intface);
			List<String> structTypes = structDecl.getStructTypes();
			// Iterate over enum declarations
			for (String stType : structTypes) {
				// Open a new file to write into
				FileWriter fw = new FileWriter(dir + "/" + stType + ".java");
				pw = new PrintWriter(new BufferedWriter(fw));
				println("public class " + stType + " {");
				List<String> structMemberTypes = structDecl.getMemberTypes(stType);
				List<String> structMembers = structDecl.getMembers(stType);
				for (int i = 0; i < structMembers.size(); i++) {

					String memberType = structMemberTypes.get(i);
					String member = structMembers.get(i);
					println("public static " + memberType + " " + member + ";");
				}
				println("}\n");
				pw.close();
				System.out.println("IoTCompiler: Generated struct class " + stType + ".java...");
			}
		}
	}


	/**
	 * generateJavaLocalInterface() writes the local interface and provides type-checking.
	 * <p>
	 * It needs to rewrite and exchange USERDEFINED types in input parameters of stub
	 * and original interfaces, e.g. exchange Camera and CameraWithVideoAndRecording.
	 * The local interface has to be the input parameter for the stub and the stub 
	 * interface has to be the input parameter for the local class.
	 */
	public void generateJavaLocalInterfaces() throws IOException {

		// Create a new directory
		createDirectory(dir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Open a new file to write into
			FileWriter fw = new FileWriter(dir + "/" + intface + ".java");
			pw = new PrintWriter(new BufferedWriter(fw));
			// Pass in set of methods and get import classes
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
			List<String> methods = intDecl.getMethods();
			Set<String> importClasses = getImportClasses(methods, intDecl);
			List<String> stdImportClasses = getStandardJavaIntfaceImportClasses();
			List<String> allImportClasses = getAllLibClasses(stdImportClasses, importClasses);
			printImportStatements(allImportClasses);
			// Write interface header
			println("");
			println("public interface " + intface + " {");
			// Write methods
			writeMethodJavaLocalInterface(methods, intDecl);
			println("}");
			pw.close();
			System.out.println("IoTCompiler: Generated local interface " + intface + ".java...");
		}
	}


	/**
	 * HELPER: updateIntfaceObjIdMap() updates the mapping between new interface and object Id
	 */
	private void updateIntfaceObjIdMap(String intface, String newIntface) {

		// We are assuming that we only generate one stub per one skeleton at this point @Feb 2017
		Integer objId = mapIntfaceObjId.get(intface);
		mapNewIntfaceObjId.put(newIntface, objId);
	}


	/**
	 * generateJavaInterfaces() generate stub interfaces based on the methods list in Java
	 */
	public void generateJavaInterfaces() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {

			Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
			for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {

				// Open a new file to write into
				String newIntface = intMeth.getKey();
				FileWriter fw = new FileWriter(path + "/" + newIntface + ".java");
				pw = new PrintWriter(new BufferedWriter(fw));
				DeclarationHandler decHandler = mapIntDeclHand.get(intface);
				InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
				// Pass in set of methods and get import classes
				Set<String> methods = intMeth.getValue();
				Set<String> importClasses = getImportClasses(methods, intDecl);
				List<String> stdImportClasses = getStandardJavaIntfaceImportClasses();
				List<String> allImportClasses = getAllLibClasses(stdImportClasses, importClasses);
				printImportStatements(allImportClasses);
				// Write interface header
				println("");
				println("public interface " + newIntface + " {\n");
				updateIntfaceObjIdMap(intface, newIntface);
				// Write methods
				writeMethodJavaInterface(methods, intDecl);
				println("}");
				pw.close();
				System.out.println("IoTCompiler: Generated interface " + newIntface + ".java...");
			}
		}
	}


	/**
	 * HELPER: writePropertiesJavaPermission() writes the permission in properties
	 */
	private void writePropertiesJavaPermission(String intface, InterfaceDecl intDecl) {

		Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
		for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
			String newIntface = intMeth.getKey();
			int newObjectId = getNewIntfaceObjectId(newIntface);
			//println("private final static int object" + newObjectId + "Id = " + 
			//	newObjectId + ";\t//" + newIntface);
			Set<String> methodIds = intMeth.getValue();
			print("private static Integer[] object" + newObjectId + "Permission = { ");
			int i = 0;
			for (String methodId : methodIds) {
				int methodNumId = intDecl.getMethodNumId(methodId);
				print(Integer.toString(methodNumId));
				// Check if this is the last element (don't print a comma)
				if (i != methodIds.size() - 1) {
					print(", ");
				}
				i++;
			}
			println(" };");
			println("private static List<Integer> set" + newObjectId + "Allowed;");
		}
	}


	/**
	 * HELPER: writePropertiesJavaStub() writes the properties of the stub class
	 */
	private void writePropertiesJavaStub(String intface, Set<String> methods, InterfaceDecl intDecl) {

		// Get the object Id
		Integer objId = mapIntfaceObjId.get(intface);
		println("private int objectId = " + objId + ";");
		println("private IoTRMIComm rmiComm;");
		// Write the list of AtomicBoolean variables
		println("// Synchronization variables");
		for (String method : methods) {
			// Generate AtomicBooleans for methods that have return values
			String returnType = intDecl.getMethodType(method);
			int methodNumId = intDecl.getMethodNumId(method);
			if (!returnType.equals("void")) {
				println("private AtomicBoolean retValueReceived" + methodNumId + " = new AtomicBoolean(false);");
			}
		}
		println("\n");
	}


	/**
	 * HELPER: writeConstructorJavaPermission() writes the permission in constructor
	 */
	private void writeConstructorJavaPermission(String intface) {

		Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
		for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
			String newIntface = intMeth.getKey();
			int newObjectId = getNewIntfaceObjectId(newIntface);
			println("set" + newObjectId + "Allowed = new ArrayList<Integer>(Arrays.asList(object" + newObjectId +"Permission));");
		}
	}


	/**
	 * HELPER: writeConstructorJavaStub() writes the constructor of the stub class
	 */
	private void writeConstructorJavaStub(String intface, String newStubClass, Set<String> methods, InterfaceDecl intDecl) {

		println("public " + newStubClass + "(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {");
		println("if (_localPortSend != 0 && _localPortRecv != 0) {");
		println("rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);");
		println("} else");
		println("{");
		println("rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);");
		println("}");
		// Register the AtomicBoolean variables
		for (String method : methods) {
			// Generate AtomicBooleans for methods that have return values
			String returnType = intDecl.getMethodType(method);
			int methodNumId = intDecl.getMethodNumId(method);
			if (!returnType.equals("void")) {
				println("rmiComm.registerStub(objectId, " + methodNumId + ", retValueReceived" + methodNumId + ");");
			}
		}
		println("IoTRMIUtil.mapStub.put(objectId, this);");
		println("}\n");
	}


	/**
	 * HELPER: writeCallbackConstructorJavaStub() writes the callback constructor of the stub class
	 */
	private void writeCallbackConstructorJavaStub(String intface, String newStubClass, Set<String> methods, InterfaceDecl intDecl) {

		println("public " + newStubClass + "(IoTRMIComm _rmiComm, int _objectId) throws Exception {");
		println("rmiComm = _rmiComm;");
		println("objectId = _objectId;");
		// Register the AtomicBoolean variables
		for (String method : methods) {
			// Generate AtomicBooleans for methods that have return values
			String returnType = intDecl.getMethodType(method);
			int methodNumId = intDecl.getMethodNumId(method);
			if (!returnType.equals("void")) {
				println("rmiComm.registerStub(objectId, " + methodNumId + ", retValueReceived" + methodNumId + ");");
			}
		}
		println("}\n");
	}


	/**
	 * HELPER: getPortCount() gets port count for different stubs and skeletons
	 */
	private int getPortCount(String intface) {

		if (!mapPortCount.containsKey(intface))
			mapPortCount.put(intface, portCount++);
		return mapPortCount.get(intface);
	}


	/**
	 * HELPER: checkAndWriteEnumTypeJavaStub() writes the enum type (convert from enum to int)
	 */
	private void checkAndWriteEnumTypeJavaStub(List<String> methParams, List<String> methPrmTypes) {

		// Iterate and find enum declarations
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isEnumClass(simpleType)) {
			// Check if this is enum type
				if (isArray(param)) {	// An array
					println("int len" + i + " = " + getSimpleIdentifier(param) + ".length;");
					println("int paramEnum" + i + "[] = new int[len" + i + "];");
					println("for (int i = 0; i < len" + i + "; i++) {");
					println("paramEnum" + i + "[i] = " + getSimpleIdentifier(param) + "[i].ordinal();");
					println("}");
				} else if (isList(paramType)) {	// A list
					println("int len" + i + " = " + getSimpleIdentifier(param) + ".size();");
					println("int paramEnum" + i + "[] = new int[len" + i + "];");
					println("for (int i = 0; i < len" + i + "; i++) {");
					println("paramEnum" + i + "[i] = " + getSimpleIdentifier(param) + ".get(i).ordinal();");
					println("}");
				} else {	// Just one element
					println("int paramEnum" + i + "[] = new int[1];");
					println("paramEnum" + i + "[0] = " + param + ".ordinal();");
				}
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumRetTypeJavaStub() writes the enum return type (convert from enum to int)
	 */
	private void checkAndWriteEnumRetTypeJavaStub(String retType, String method, InterfaceDecl intDecl) {

		// Write the wait-for-return-value part
		writeWaitForReturnValueJava(method, intDecl, "Object retObj = rmiComm.getReturnValue(retType, null);");
		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(getGenericType(retType));
		// Take the inner type of generic
		if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(retType);
		if (isEnumClass(pureType)) {
		// Check if this is enum type
			// Enum decoder
			println("int[] retEnum = (int[]) retObj;");
			println(pureType + "[] enumVals = " + pureType + ".values();");
			if (isArray(retType)) {			// An array
				println("int retLen = retEnum.length;");
				println(pureType + "[] enumRetVal = new " + pureType + "[retLen];");
				println("for (int i = 0; i < retLen; i++) {");
				println("enumRetVal[i] = enumVals[retEnum[i]];");
				println("}");
			} else if (isList(retType)) {	// A list
				println("int retLen = retEnum.length;");
				println("List<" + pureType + "> enumRetVal = new ArrayList<" + pureType + ">();");
				println("for (int i = 0; i < retLen; i++) {");
				println("enumRetVal.add(enumVals[retEnum[i]]);");
				println("}");
			} else {	// Just one element
				println(pureType + " enumRetVal = enumVals[retEnum[0]];");
			}
			println("return enumRetVal;");
		}
	}


	/**
	 * HELPER: checkAndWriteStructSetupJavaStub() writes the struct type setup
	 */
	private void checkAndWriteStructSetupJavaStub(List<String> methParams, List<String> methPrmTypes, 
			InterfaceDecl intDecl, String method) {
		
		// Iterate and find struct declarations
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
			// Check if this is enum type
				int methodNumId = intDecl.getMethodNumId(method);
				String helperMethod = methodNumId + "struct" + i;
				println("int methodIdStruct" + i + " = " + intDecl.getHelperMethodNumId(helperMethod) + ";");
				println("Class<?>[] paramClsStruct" + i + " = new Class<?>[] { int.class };");
				if (isArray(param)) {	// An array
					println("Object[] paramObjStruct" + i + " = new Object[] { " + getSimpleArrayType(param) + ".length };");
				} else if (isList(paramType)) {	// A list
					println("Object[] paramObjStruct" + i + " = new Object[] { " + getSimpleArrayType(param) + ".size() };");
				} else {	// Just one element
					println("Object[] paramObjStruct" + i + " = new Object[] { new Integer(1) };");
				}
				println("rmiComm.remoteCall(objectId, methodIdStruct" + i + 
						", paramClsStruct" + i + ", paramObjStruct" + i + ");\n");
			}
		}
	}


	/**
	 * HELPER: isStructPresent() checks presence of struct
	 */
	private boolean isStructPresent(List<String> methParams, List<String> methPrmTypes) {

		// Iterate and find enum declarations
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType))
				return true;
		}
		return false;
	}


	/**
	 * HELPER: writeLengthStructParamClassJavaStub() writes lengths of parameters
	 */
	private void writeLengthStructParamClassJavaStub(List<String> methParams, List<String> methPrmTypes) {

		// Iterate and find struct declarations - count number of params
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				int members = getNumOfMembers(simpleType);
				if (isArray(param)) {			// An array
					String structLen = getSimpleArrayType(param) + ".length";
					print(members + "*" + structLen);
				} else if (isList(paramType)) {	// A list
					String structLen = getSimpleArrayType(param) + ".size()";
					print(members + "*" + structLen);
				} else
					print(Integer.toString(members));
			} else
				print("1");
			if (i != methParams.size() - 1) {
				print("+");
			}
		}
	}


	/**
	 * HELPER: writeStructMembersJavaStub() writes parameters of struct
	 */
	private void writeStructMembersJavaStub(String simpleType, String paramType, String param) {

		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArray(param)) {			// An array
			println("for(int i = 0; i < " + getSimpleIdentifier(param) + ".length; i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("paramCls[pos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("paramObj[pos++] = " + getSimpleIdentifier(param) + "[i].");
				print(getSimpleIdentifier(members.get(i)));
				println(";");
			}
			println("}");
		} else if (isList(paramType)) {	// A list
			println("for(int i = 0; i < " + getSimpleIdentifier(param) + ".size(); i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("paramCls[pos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("paramObj[pos++] = " + getSimpleIdentifier(param) + ".get(i).");
				print(getSimpleIdentifier(members.get(i)));
				println(";");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("paramCls[pos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("paramObj[pos++] = " + getSimpleIdentifier(param) + ".");
				print(getSimpleIdentifier(members.get(i)));
				println(";");
			}
		}
	}


	/**
	 * HELPER: writeStructParamClassJavaStub() writes parameters if struct is present
	 */
	private void writeStructParamClassJavaStub(List<String> methParams, List<String> methPrmTypes, Set<String> callbackType) {

		print("int paramLen = ");
		writeLengthStructParamClassJavaStub(methParams, methPrmTypes);
		println(";");
		println("Object[] paramObj = new Object[paramLen];");
		println("Class<?>[] paramCls = new Class<?>[paramLen];");
		println("int pos = 0;");
		// Iterate again over the parameters
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				writeStructMembersJavaStub(simpleType, paramType, param);
			} else if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				println("paramCls[pos] = int[].class;");
				println("paramObj[pos++] = objIdSent" + i + ";");
			} else {
				String prmType = checkAndGetArray(methPrmTypes.get(i), methParams.get(i));
				println("paramCls[pos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("paramObj[pos++] = ");
				print(getEnumParam(methPrmTypes.get(i), getSimpleIdentifier(methParams.get(i)), i));
				println(";");
			}
		}
		
	}


	/**
	 * HELPER: writeStructRetMembersJavaStub() writes parameters of struct for return statement
	 */
	private void writeStructRetMembersJavaStub(String simpleType, String retType) {

		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArrayOrList(retType, retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
		}
		if (isArray(retType)) {	// An array
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				print("structRet[i]." + getSimpleIdentifier(members.get(i)));
				println(" = (" + getSimpleType(getEnumType(prmType)) + ") retActualObj[retObjPos++];");
			}
			println("}");
		} else if (isList(retType)) {	// A list
			println(simpleType + " structRetMem = new " + simpleType + "();");
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				print("structRetMem." + getSimpleIdentifier(members.get(i)));
				println(" = (" + getSimpleType(getEnumType(prmType)) + ") retActualObj[retObjPos++];");
			}
			println("structRet.add(structRetMem);");
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				print("structRet." + getSimpleIdentifier(members.get(i)));
				println(" = (" + getSimpleType(getEnumType(prmType)) + ") retActualObj[retObjPos++];");
			}
		}
		println("return structRet;");
	}


	/**
	 * HELPER: writeStructReturnJavaStub() writes parameters if struct is present for return statement
	 */
	private void writeStructReturnJavaStub(String simpleType, String retType, String method, InterfaceDecl intDecl) {

		// Handle the returned struct size
		writeWaitForReturnValueJava(method, intDecl, "Object retObj = rmiComm.getReturnValue(retType, null);");
		// Minimum retLen is 1 if this is a single struct object
		println("int retLen = (int) retObj;");
		int numMem = getNumOfMembers(simpleType);
		println("Class<?>[] retCls = new Class<?>[" + numMem + "*retLen];");
		println("Class<?>[] retClsVal = new Class<?>[" + numMem + "*retLen];");
		println("int retPos = 0;");
		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArrayOrList(retType, retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("retCls[retPos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				println("retClsVal[retPos++] = null;");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("retCls[retPos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				println("retClsVal[retPos++] = null;");
			}
		}
		//println("Object[] retActualObj = rmiComm.getStructObjects(retCls, retClsVal);");
		// Handle the actual returned struct
		writeWaitForReturnValueJava(method, intDecl, "Object[] retActualObj = rmiComm.getStructObjects(retCls, retClsVal);");
		if (isArray(retType)) {			// An array
			println(simpleType + "[] structRet = new " + simpleType + "[retLen];");
			println("for(int i = 0; i < retLen; i++) {");
			println("structRet[i] = new " + simpleType + "();");
			println("}");
		} else if (isList(retType)) {	// A list
			println("List<" + simpleType + "> structRet = new ArrayList<" + simpleType + ">();");
		} else
			println(simpleType + " structRet = new " + simpleType + "();");
		println("int retObjPos = 0;");
		writeStructRetMembersJavaStub(simpleType, retType);
	}


	/**
	 * HELPER: writeWaitForReturnValueJava() writes the synchronization part for return values
	 */
	private void writeWaitForReturnValueJava(String method, InterfaceDecl intDecl, String getReturnValue) {

		println("// Waiting for return value");		
		int methodNumId = intDecl.getMethodNumId(method);
		println("while (!retValueReceived" + methodNumId + ".get());");
		println(getReturnValue);
		println("retValueReceived" + methodNumId + ".set(false);");
		println("rmiComm.setGetReturnBytes();\n");
	}


	/**
	 * HELPER: writeStdMethodBodyJavaStub() writes the standard method body in the stub class
	 */
	private void writeStdMethodBodyJavaStub(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, Set<String> callbackType) {

		checkAndWriteStructSetupJavaStub(methParams, methPrmTypes, intDecl, method);
		println("int methodId = " + intDecl.getMethodNumId(method) + ";");
		String retType = intDecl.getMethodType(method);
		println("Class<?> retType = " + getSimpleType(getStructType(getEnumType(retType))) + ".class;");
		checkAndWriteEnumTypeJavaStub(methParams, methPrmTypes);
		// Generate array of parameter types
		if (isStructPresent(methParams, methPrmTypes)) {
			writeStructParamClassJavaStub(methParams, methPrmTypes, callbackType);
		} else {
			print("Class<?>[] paramCls = new Class<?>[] { ");
			for (int i = 0; i < methParams.size(); i++) {
				String prmType = methPrmTypes.get(i);
				if (checkCallbackType(prmType, callbackType)) { // Check if this has callback object
					print("int[].class");
				} else { // Generate normal classes if it's not a callback object
					String paramType = checkAndGetArray(methPrmTypes.get(i), methParams.get(i));
					print(getSimpleType(getEnumType(paramType)) + ".class");
				}
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(" };");
			// Generate array of parameter objects
			print("Object[] paramObj = new Object[] { ");
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
					print("objIdSent" + i);
				} else
					print(getEnumParam(methPrmTypes.get(i), getSimpleIdentifier(methParams.get(i)), i));
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(" };");
		}
		// Send method call first and wait for return value separately
		println("rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);");
		// Check if this is "void"
		if (!retType.equals("void")) { // We do have a return value
			// Generate array of parameter types
			if (isStructClass(getGenericType(getSimpleArrayType(retType)))) {
				writeStructReturnJavaStub(getGenericType(getSimpleArrayType(retType)), retType, method, intDecl);
			} else {
				// This is an enum type
				if (getParamCategory(getGenericType(getSimpleArrayType(retType))) == ParamCategory.ENUM) {
					//println("Object retObj = rmiCall.remoteCall(objectId, methodId, retType, null, paramCls, paramObj);");
					checkAndWriteEnumRetTypeJavaStub(retType, method, intDecl);
				} else if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES) {
				// Check if the return value NONPRIMITIVES
					String retGenValType = getGenericType(retType);
					println("Class<?> retGenValType = " + retGenValType + ".class;");
					writeWaitForReturnValueJava(method, intDecl, "Object retObj = rmiComm.getReturnValue(retType, retGenValType);");
					println("return (" + retType + ")retObj;");
				} else {
					writeWaitForReturnValueJava(method, intDecl, "Object retObj = rmiComm.getReturnValue(retType, null);");
					println("return (" + retType + ")retObj;");
				}
			}
		}
	}


	/**
	 * HELPER: returnGenericCallbackType() returns the callback type
	 */
	private String returnGenericCallbackType(String paramType) {

		if (getParamCategory(paramType) == ParamCategory.NONPRIMITIVES)
			return getGenericType(paramType);
		else
			return paramType;
	}


	/**
	 * HELPER: checkCallbackType() checks the callback type
	 */
	private boolean checkCallbackType(String paramType, Set<String> callbackType) {

		String prmType = returnGenericCallbackType(paramType);
		if (callbackType == null)	// If there is no callbackType it means not a callback method
			return false;
		else {
			for (String type : callbackType) {
				if (type.equals(prmType))
					return true;	// Check callbackType one by one
			}
			return false;
		}
	}


	/**
	 * HELPER: checkCallbackType() checks the callback type
	 */
	private boolean checkCallbackType(String paramType, String callbackType) {

		String prmType = returnGenericCallbackType(paramType);
		if (callbackType == null)	// If there is no callbackType it means not a callback method
			return false;
		else
			return callbackType.equals(prmType);
	}


	/**
	 * HELPER: writeCallbackInstantiationMethodBodyJavaStub() writes the callback object instantiation in the method of the stub class
	 */
	private void writeCallbackInstantiationMethodBodyJavaStub(String paramIdent, String callbackType, int counter, boolean isMultipleCallbacks) {

		println("if (!IoTRMIUtil.mapSkel.containsKey(" + paramIdent + ")) {");
		println("int newObjIdSent = rmiComm.getObjectIdCounter();");
		if (isMultipleCallbacks)
			println("objIdSent" + counter + "[cnt" + counter + "++] = newObjIdSent;");
		else
			println("objIdSent" + counter + "[0] = newObjIdSent;");
		println("rmiComm.decrementObjectIdCounter();");
		println(callbackType + "_Skeleton skel" + counter + " = new " + callbackType + "_Skeleton(" + paramIdent + ", rmiComm, newObjIdSent);");
		println("IoTRMIUtil.mapSkel.put(" + paramIdent + ", skel" + counter + ");");
		println("IoTRMIUtil.mapSkelId.put(" + paramIdent + ", newObjIdSent);");
		println("Thread thread = new Thread() {");
		println("public void run() {");
		println("try {");
		println("skel" + counter + ".___waitRequestInvokeMethod();");
		println("} catch (Exception ex) {");
		println("ex.printStackTrace();");
		println("throw new Error(\"Exception when trying to run ___waitRequestInvokeMethod() for " + 
			callbackType + "_Skeleton!\");");
		println("}");
		println("}");
		println("};");
		println("thread.start();");
		println("while(!skel" + counter + ".didAlreadyInitWaitInvoke());");
		println("}");
		println("else");
		println("{");
		println("int newObjIdSent = IoTRMIUtil.mapSkelId.get(" + paramIdent + ");");
		if (isMultipleCallbacks)
			println("objIdSent" + counter + "[cnt" + counter + "++] = newObjIdSent;");
		else
			println("objIdSent" + counter + "[0] = newObjIdSent;");
		println("}");
	}


	/**
	 * HELPER: writeCallbackMethodBodyJavaStub() writes the callback method of the stub class
	 */
	private void writeCallbackMethodBodyJavaStub(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, Set<String> callbackType) {

		// Determine callback object counter type (List vs. single variable)
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				print("int[] objIdSent" + i + " = ");
				String param = methParams.get(i);
				if (isArray(methParams.get(i)))
					println("new int[" + getSimpleIdentifier(methParams.get(i)) + ".length];");
				else if (isList(methPrmTypes.get(i)))
					println("new int[" + getSimpleIdentifier(methParams.get(i)) + ".size()];");
				else
					println("new int[1];");
			}
		}
		println("try {");
		// Check if this is single object, array, or list of objects
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				String param = methParams.get(i);
				if (isArrayOrList(paramType, param)) {	// Generate loop
					println("int cnt" + i + " = 0;");
					println("for (" + getGenericType(paramType) + " cb : " + getSimpleIdentifier(param) + ") {");
					writeCallbackInstantiationMethodBodyJavaStub("cb", returnGenericCallbackType(paramType), i, true);
				} else
					writeCallbackInstantiationMethodBodyJavaStub(getSimpleIdentifier(param), returnGenericCallbackType(paramType), i, false);
				if (isArrayOrList(paramType, param))
					println("}");
			}
		}
		print("}");
		println(" catch (Exception ex) {");
		println("ex.printStackTrace();");
		println("throw new Error(\"Exception when generating skeleton objects!\");");
		println("}\n");
	}


	/**
	 * HELPER: writeMethodJavaStub() writes the methods of the stub class
	 */
	private void writeMethodJavaStub(Collection<String> methods, InterfaceDecl intDecl, Set<String> callbackClasses, String newStubClass) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			print("public " + intDecl.getMethodType(method) + " " +
				intDecl.getMethodId(method) + "(");
			boolean isCallbackMethod = false;
			//String callbackType = null;
			Set<String> callbackType = new HashSet<String>();
			for (int i = 0; i < methParams.size(); i++) {

				String paramType = returnGenericCallbackType(methPrmTypes.get(i));
				// Check if this has callback object
				if (callbackClasses.contains(paramType)) {
					isCallbackMethod = true;
					//callbackType = paramType;
					callbackType.add(paramType);
					// Even if there're 2 callback arguments, we expect them to be of the same interface
				}
				print(methPrmTypes.get(i) + " " + methParams.get(i));
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(") {");
			// Now, write the body of stub!
			if (isCallbackMethod)
				writeCallbackMethodBodyJavaStub(intDecl, methParams, methPrmTypes, method, callbackType);
			//else
			writeStdMethodBodyJavaStub(intDecl, methParams, methPrmTypes, method, callbackType);
			println("}\n");
		}
	}


	/**
	 * HELPER: getStubInterface() gets stub interface name based on original interface
	 */
	public String getStubInterface(String intface) {

		return mapInt2NewIntName.get(intface);
	}


	/**
	 * generateJavaStubClasses() generate stubs based on the methods list in Java
	 */
	public void generateJavaStubClasses() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {

			Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
			for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {

				// Open a new file to write into
				String newIntface = intMeth.getKey();
				String newStubClass = newIntface + "_Stub";
				FileWriter fw = new FileWriter(path + "/" + newStubClass + ".java");
				pw = new PrintWriter(new BufferedWriter(fw));
				DeclarationHandler decHandler = mapIntDeclHand.get(intface);
				InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
				// Pass in set of methods and get import classes
				Set<String> methods = intMeth.getValue();
				Set<String> importClasses = getImportClasses(methods, intDecl);
				List<String> stdImportClasses = getStandardJavaImportClasses();
				List<String> allImportClasses = getAllLibClasses(stdImportClasses, importClasses);
				printImportStatements(allImportClasses); println("");
				// Find out if there are callback objects
				Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
				boolean callbackExist = !callbackClasses.isEmpty();
				// Write class header
				println("public class " + newStubClass + " implements " + newIntface + " {\n");
				// Write properties
				writePropertiesJavaStub(intface, intMeth.getValue(), intDecl);
				// Write constructor
				writeConstructorJavaStub(intface, newStubClass, intMeth.getValue(), intDecl);
				// Write callback constructor (used if this stub is treated as a callback stub)
				writeCallbackConstructorJavaStub(intface, newStubClass, intMeth.getValue(), intDecl);
				// Write methods
				writeMethodJavaStub(intMeth.getValue(), intDecl, callbackClasses, newStubClass);
				println("}");
				pw.close();
				System.out.println("IoTCompiler: Generated stub class " + newStubClass + ".java...");
			}
		}
	}


	/**
	 * HELPER: writePropertiesJavaSkeleton() writes the properties of the skeleton class
	 */
	private void writePropertiesJavaSkeleton(String intface, InterfaceDecl intDecl) {

		println("private " + intface + " mainObj;");
		//println("private int objectId = 0;");
		Integer objId = mapIntfaceObjId.get(intface);
		println("private int objectId = " + objId + ";");
		println("// Communications and synchronizations");
		println("private IoTRMIComm rmiComm;");
		println("private AtomicBoolean didAlreadyInitWaitInvoke;");
		println("private AtomicBoolean methodReceived;");
		println("private byte[] methodBytes = null;");
		println("// Permissions");
		writePropertiesJavaPermission(intface, intDecl);
		println("\n");
	}


	/**
	 * HELPER: writeStructPermissionJavaSkeleton() writes permission for struct helper
	 */
	private void writeStructPermissionJavaSkeleton(Collection<String> methods, InterfaceDecl intDecl, String intface) {

		// Use this set to handle two same methodIds
		for (String method : methods) {
			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					String helperMethod = methodNumId + "struct" + i;
					int methodHelperNumId = intDecl.getHelperMethodNumId(helperMethod);
					// Iterate over interfaces to give permissions to
					Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
					for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
						String newIntface = intMeth.getKey();
						int newObjectId = getNewIntfaceObjectId(newIntface);
						println("set" + newObjectId + "Allowed.add(" + methodHelperNumId + ");");
					}
				}
			}
		}
	}


	/**
	 * HELPER: writeConstructorJavaSkeleton() writes the constructor of the skeleton class
	 */
	private void writeConstructorJavaSkeleton(String newSkelClass, String intface, InterfaceDecl intDecl, 
			Collection<String> methods, boolean callbackExist) {

		println("public " + newSkelClass + "(" + intface + " _mainObj, int _portSend, int _portRecv) throws Exception {");
		println("mainObj = _mainObj;");
		println("rmiComm = new IoTRMICommServer(_portSend, _portRecv);");
		// Generate permission control initialization
		writeConstructorJavaPermission(intface);
		writeStructPermissionJavaSkeleton(methods, intDecl, intface);
		println("IoTRMIUtil.mapSkel.put(_mainObj, this);");
		println("IoTRMIUtil.mapSkelId.put(_mainObj, objectId);");
		println("didAlreadyInitWaitInvoke = new AtomicBoolean(false);");
		println("methodReceived = new AtomicBoolean(false);");
		println("rmiComm.registerSkeleton(objectId, methodReceived);");
		println("Thread thread1 = new Thread() {");
		println("public void run() {");
		println("try {");
		println("___waitRequestInvokeMethod();");
		println("}");
		println("catch (Exception ex)");
		println("{");
		println("ex.printStackTrace();");
		println("}");
		println("}");
		println("};");
		println("thread1.start();");
		println("}\n");
	}


	/**
	 * HELPER: writeCallbackConstructorJavaSkeleton() writes the constructor of the skeleton class
	 */
	private void writeCallbackConstructorJavaSkeleton(String newSkelClass, String intface, InterfaceDecl intDecl, 
			Collection<String> methods, boolean callbackExist) {

		println("public " + newSkelClass + "(" + intface + " _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {");
		println("mainObj = _mainObj;");
		println("rmiComm = _rmiComm;");
		println("objectId = _objectId;");
		// Generate permission control initialization
		writeConstructorJavaPermission(intface);
		writeStructPermissionJavaSkeleton(methods, intDecl, intface);
		println("didAlreadyInitWaitInvoke = new AtomicBoolean(false);");
		println("methodReceived = new AtomicBoolean(false);");
		println("rmiComm.registerSkeleton(objectId, methodReceived);");
		println("}\n");
	}


	/**
	 * HELPER: writeStdMethodBodyJavaSkeleton() writes the standard method body in the skeleton class
	 */
	private void writeStdMethodBodyJavaSkeleton(List<String> methParams, String methodId, String methodType) {

		if (methodType.equals("void"))
			print("mainObj." + methodId + "(");
		else
			print("return mainObj." + methodId + "(");
		for (int i = 0; i < methParams.size(); i++) {

			print(getSimpleIdentifier(methParams.get(i)));
			// Check if this is the last element (don't print a comma)
			if (i != methParams.size() - 1) {
				print(", ");
			}
		}
		println(");");
	}


	/**
	 * HELPER: writeMethodJavaSkeleton() writes the method of the skeleton class
	 */
	private void writeMethodJavaSkeleton(Collection<String> methods, InterfaceDecl intDecl) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			String methodId = intDecl.getMethodId(method);
			print("public " + intDecl.getMethodType(method) + " " + methodId + "(");
			for (int i = 0; i < methParams.size(); i++) {

				String origParamType = methPrmTypes.get(i);
				String paramType = checkAndGetParamClass(origParamType);
				print(paramType + " " + methParams.get(i));
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(") {");
			// Now, write the body of skeleton!
			writeStdMethodBodyJavaSkeleton(methParams, methodId, intDecl.getMethodType(method));
			println("}\n");
		}
	}


	/**
	 * HELPER: writeCallbackInstantiationJavaStubGeneration() writes the instantiation of callback stubs
	 */
	private void writeCallbackInstantiationJavaStubGeneration(String exchParamType, int counter) {

		println(exchParamType + " newStub" + counter + " = null;");
		println("if(!IoTRMIUtil.mapStub.containsKey(objIdRecv" + counter + ")) {");
		println("newStub" + counter + " = new " + exchParamType + "_Stub(rmiComm, objIdRecv" + counter + ");");
		println("IoTRMIUtil.mapStub.put(objIdRecv" + counter + ", newStub" + counter + ");");
		println("rmiComm.setObjectIdCounter(objIdRecv" + counter + ");");
		println("rmiComm.decrementObjectIdCounter();");
		println("}");
		println("else {");
		println("newStub" + counter + " = (" + exchParamType + "_Stub) IoTRMIUtil.mapStub.get(objIdRecv" + counter + ");");
		println("}");
	}


	/**
	 * HELPER: writeCallbackJavaStubGeneration() writes the callback stub generation part
	 */
	private Map<Integer,String> writeCallbackJavaStubGeneration(List<String> methParams, List<String> methPrmTypes, 
			Set<String> callbackType, boolean isStructMethod) {

		Map<Integer,String> mapStubParam = new HashMap<Integer,String>();
		String offsetPfx = "";
		if (isStructMethod)
			offsetPfx = "offset";
		// Iterate over callback objects
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				String exchParamType = checkAndGetParamClass(getGenericType(paramType));
				// Print array if this is array or list if this is a list of callback objects
				println("int[] stubIdArray" + i + " = (int[]) paramObj[" + offsetPfx + i + "];");
				if (isArray(param)) {				
					println("int numStubs" + i + " = stubIdArray" + i + ".length;");
					println(exchParamType + "[] stub" + i + " = new " + exchParamType + "[numStubs" + i + "];");
				} else if (isList(paramType)) {
					println("int numStubs" + i + " = stubIdArray" + i + ".length;");
					println("List<" + exchParamType + "> stub" + i + " = new ArrayList<" + exchParamType + ">();");
				} else {
					println("int objIdRecv" + i + " = stubIdArray" + i + "[0];");
					writeCallbackInstantiationJavaStubGeneration(exchParamType, i);
				}
			}
			// Generate a loop if needed
			if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				String exchParamType = checkAndGetParamClass(getGenericType(paramType));
				if (isArray(param)) {
					println("for (int i = 0; i < numStubs" + i + "; i++) {");
					println("int objIdRecv" + i + " = stubIdArray" + i + "[i];");
					writeCallbackInstantiationJavaStubGeneration(exchParamType, i);
					println("stub" + i + "[i] = newStub" + i + ";");
					println("}");
				} else if (isList(paramType)) {
					println("for (int i = 0; i < numStubs" + i + "; i++) {");
					println("int objIdRecv" + i + " = stubIdArray" + i + "[i];");
					writeCallbackInstantiationJavaStubGeneration(exchParamType, i);
					println("stub" + i + ".add(newStub" + i + ");");
					println("}");
				} else
					println(exchParamType + " stub" + i + " = newStub" + i + ";");
				mapStubParam.put(i, "stub" + i);	// List of all stub parameters
			}
		}
		return mapStubParam;
	}


	/**
	 * HELPER: checkAndWriteEnumTypeJavaSkeleton() writes the enum type (convert from enum to int)
	 */
	private void checkAndWriteEnumTypeJavaSkeleton(List<String> methParams, List<String> methPrmTypes, boolean isStructMethod) {

		String offsetPfx = "";
		if (isStructMethod)
			offsetPfx = "offset";
		// Iterate and find enum declarations
		boolean printed = false;
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isEnumClass(simpleType)) {
			// Check if this is enum type
				println("int paramInt" + i + "[] = (int[]) paramObj[" + offsetPfx + i + "];");
				if (!printed) {
					println(simpleType + "[] enumVals = " + simpleType + ".values();");
					printed = true;
				}
				if (isArray(param)) {	// An array
					println("int len" + i + " = paramInt" + i + ".length;");
					println(simpleType + "[] paramEnum" + i + " = new " + simpleType + "[len" + i + "];");
					println("for (int i = 0; i < len" + i + "; i++) {");
					println("paramEnum" + i + "[i] = enumVals[paramInt" + i + "[i]];");
					println("}");
				} else if (isList(paramType)) {	// A list
					println("int len" + i + " = paramInt" + i + ".length;");
					println("List<" + simpleType + "> paramEnum" + i + " = new ArrayList<" + simpleType + ">();");
					println("for (int i = 0; i < len" + i + "; i++) {");
					println("paramEnum" + i + ".add(enumVals[paramInt" + i + "[i]]);");
					println("}");
				} else {	// Just one element
					println(simpleType + " paramEnum" + i + " = enumVals[paramInt" + i + "[0]];");
				}
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumRetTypeJavaSkeleton() writes the enum return type (convert from enum to int)
	 */
	private void checkAndWriteEnumRetTypeJavaSkeleton(String retType, String methodId) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(getGenericType(retType));
		// Take the inner type of generic
		if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(retType);
		if (isEnumClass(pureType)) {
		// Check if this is enum type
			// Enum decoder
			if (isArray(retType)) {			// An array
				print(pureType + "[] retEnum = " + methodId + "(");
			} else if (isList(retType)) {	// A list
				print("List<" + pureType + "> retEnum = " + methodId + "(");
			} else {	// Just one element
				print(pureType + " retEnum = " + methodId + "(");
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumRetConvJavaSkeleton() writes the enum return type (convert from enum to int)
	 */
	private void checkAndWriteEnumRetConvJavaSkeleton(String retType) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(getGenericType(retType));
		// Take the inner type of generic
		if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(retType);
		if (isEnumClass(pureType)) {
		// Check if this is enum type
			if (isArray(retType)) {	// An array
				println("int retLen = retEnum.length;");
				println("int[] retEnumVal = new int[retLen];");
				println("for (int i = 0; i < retLen; i++) {");
				println("retEnumVal[i] = retEnum[i].ordinal();");
				println("}");
			} else if (isList(retType)) {	// A list
				println("int retLen = retEnum.size();");
				println("int[] retEnumVal = new int[retLen];");
				println("for (int i = 0; i < retLen; i++) {");
				println("retEnumVal[i] = retEnum.get(i).ordinal();");
				println("}");
			} else {	// Just one element
				println("int[] retEnumVal = new int[1];");
				println("retEnumVal[0] = retEnum.ordinal();");
			}
			println("Object retObj = retEnumVal;");
		}
	}
	
	
	/**
	 * HELPER: writeLengthStructParamClassSkeleton() writes lengths of params
	 */
	private void writeLengthStructParamClassSkeleton(List<String> methParams, List<String> methPrmTypes, 
			String method, InterfaceDecl intDecl) {

		// Iterate and find struct declarations - count number of params
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				int members = getNumOfMembers(simpleType);
				print(Integer.toString(members) + "*");
				int methodNumId = intDecl.getMethodNumId(method);
				print("struct" + methodNumId + "Size" + i);
			} else
				print("1");
			if (i != methParams.size() - 1) {
				print("+");
			}
		}
	}

	
	/**
	 * HELPER: writeStructMembersJavaSkeleton() writes member parameters of struct
	 */
	private void writeStructMembersJavaSkeleton(String simpleType, String paramType, 
			String param, String method, InterfaceDecl intDecl, int iVar) {

		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArrayOrList(paramType, param)) {	// An array or list
			int methodNumId = intDecl.getMethodNumId(method);
			String counter = "struct" + methodNumId + "Size" + iVar;
			println("for(int i = 0; i < " + counter + "; i++) {");
		}
		if (isArrayOrList(paramType, param)) {	// An array or list
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("paramCls[pos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				println("paramClsGen[pos++] = null;");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("paramCls[pos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				println("paramClsGen[pos++] = null;");
			}
		}
	}


	/**
	 * HELPER: writeStructMembersInitJavaSkeleton() writes member parameters initialization of struct
	 */
	private void writeStructMembersInitJavaSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method) {

		println("int objPos = 0;");
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				int methodNumId = intDecl.getMethodNumId(method);
				String counter = "struct" + methodNumId + "Size" + i;
				// Declaration
				if (isArray(param)) {			// An array
					println(simpleType + "[] paramStruct" + i + " = new " + simpleType + "[" + counter + "];");
					println("for(int i = 0; i < " + counter + "; i++) {");
					println("paramStruct" + i + "[i] = new " + simpleType + "();");
					println("}");
				} else if (isList(paramType)) {	// A list
					println("List<" + simpleType + "> paramStruct" + i + " = new ArrayList<" + simpleType + ">();");
				} else
					println(simpleType + " paramStruct" + i + " = new " + simpleType + "();");
				// Initialize members
				StructDecl structDecl = getStructDecl(simpleType);
				List<String> members = structDecl.getMembers(simpleType);
				List<String> memTypes = structDecl.getMemberTypes(simpleType);
				if (isArrayOrList(paramType, param)) {	// An array or list
					println("for(int i = 0; i < " + counter + "; i++) {");
				}
				if (isArray(param)) {	// An array
					for (int j = 0; j < members.size(); j++) {
						String prmType = checkAndGetArray(memTypes.get(j), members.get(j));
						print("paramStruct" + i + "[i]." + getSimpleIdentifier(members.get(j)));
						println(" = (" + getSimpleType(getEnumType(prmType)) + ") paramObj[objPos++];");
					}
					println("}");
				} else if (isList(paramType)) {	// A list
					println(simpleType + " paramStructMem = new " + simpleType + "();");
					for (int j = 0; j < members.size(); j++) {
						String prmType = checkAndGetArray(memTypes.get(j), members.get(j));
						print("paramStructMem." + getSimpleIdentifier(members.get(j)));
						println(" = (" + getSimpleType(getEnumType(prmType)) + ") paramObj[objPos++];");
					}
					println("paramStruct" + i + ".add(paramStructMem);");
					println("}");
				} else {	// Just one struct element
					for (int j = 0; j < members.size(); j++) {
						String prmType = checkAndGetArray(memTypes.get(j), members.get(j));
						print("paramStruct" + i + "." + getSimpleIdentifier(members.get(j)));
						println(" = (" + getSimpleType(getEnumType(prmType)) + ") paramObj[objPos++];");
					}
				}
			} else {
				// Take offsets of parameters
				println("int offset" + i +" = objPos++;");
			}
		}
	}


	/**
	 * HELPER: writeStructReturnJavaSkeleton() writes struct for return statement
	 */
	private void writeStructReturnJavaSkeleton(String simpleType, String retType) {

		// Minimum retLen is 1 if this is a single struct object
		if (isArray(retType))
			println("int retLen = retStruct.length;");
		else if (isList(retType))
			println("int retLen = retStruct.size();");
		else	// Just single struct object
			println("int retLen = 1;");
		println("Object retLenObj = retLen;");
		println("rmiComm.sendReturnObj(retLenObj, localMethodBytes);");
		int numMem = getNumOfMembers(simpleType);
		println("Class<?>[] retCls = new Class<?>[" + numMem + "*retLen];");
		println("Object[] retObj = new Object[" + numMem + "*retLen];");
		println("int retPos = 0;");
		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArray(retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("retCls[retPos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("retObj[retPos++] = retStruct[i].");
				print(getEnumParam(memTypes.get(i), getSimpleIdentifier(members.get(i)), i));
				println(";");
			}
			println("}");
		} else if (isList(retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("retCls[retPos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("retObj[retPos++] = retStruct.get(i).");
				print(getEnumParam(memTypes.get(i), getSimpleIdentifier(members.get(i)), i));
				println(";");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				println("retCls[retPos] = " + getSimpleType(getEnumType(prmType)) + ".class;");
				print("retObj[retPos++] = retStruct.");
				print(getEnumParam(memTypes.get(i), getSimpleIdentifier(members.get(i)), i));
				println(";");
			}
		}

	}


	/**
	 * HELPER: writeMethodHelperReturnJavaSkeleton() writes return statement part in skeleton
	 */
	private void writeMethodHelperReturnJavaSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, boolean isCallbackMethod, Set<String> callbackType,
			boolean isStructMethod) {

		checkAndWriteEnumTypeJavaSkeleton(methParams, methPrmTypes, isStructMethod);
		Map<Integer,String> mapStubParam = null;
		if (isCallbackMethod) {
			println("try {");
			mapStubParam = writeCallbackJavaStubGeneration(methParams, methPrmTypes, callbackType, isStructMethod);
		}
		// Check if this is "void"
		String retType = intDecl.getMethodType(method);
		if (retType.equals("void")) {
			print(intDecl.getMethodId(method) + "(");
		} else if (isEnumClass(getSimpleArrayType(getGenericType(retType)))) {	// Enum type
			checkAndWriteEnumRetTypeJavaSkeleton(retType, intDecl.getMethodId(method));
		} else if (isStructClass(getSimpleArrayType(getGenericType(retType)))) {	// Struct type
			print(retType + " retStruct = " + intDecl.getMethodId(method) + "(");
		} else { // We do have a return value
			print("Object retObj = " + intDecl.getMethodId(method) + "(");
		}
		for (int i = 0; i < methParams.size(); i++) {

			String paramType = methPrmTypes.get(i);
			if (isCallbackMethod && checkCallbackType(paramType, callbackType)) {
				print(mapStubParam.get(i));	// Get the callback parameter
			} else if (isEnumClass(getGenericType(paramType))) { // Enum class
				print(getEnumParam(paramType, methParams.get(i), i));
			} else if (isStructClass(getGenericType(paramType))) {
				print("paramStruct" + i);
			} else {
				String prmType = checkAndGetArray(paramType, methParams.get(i));
				if (isStructMethod)
					print("(" + prmType + ") paramObj[offset" + i + "]");
				else
					print("(" + prmType + ") paramObj[" + i + "]");
			}
			if (i != methParams.size() - 1)
				print(", ");
		}
		println(");");
		if (!retType.equals("void")) {
			if (isEnumClass(getSimpleArrayType(getGenericType(retType)))) { // Enum type
				checkAndWriteEnumRetConvJavaSkeleton(retType);
				println("rmiComm.sendReturnObj(retObj, localMethodBytes);");
			} else if (isStructClass(getSimpleArrayType(getGenericType(retType)))) { // Struct type
				writeStructReturnJavaSkeleton(getSimpleArrayType(getGenericType(retType)), retType);
				println("rmiComm.sendReturnObj(retCls, retObj, localMethodBytes);");
			} else
				println("rmiComm.sendReturnObj(retObj, localMethodBytes);");
		}
		if (isCallbackMethod) {	// Catch exception if this is callback
			print("}");
			println(" catch(Exception ex) {");
			println("ex.printStackTrace();");
			println("throw new Error(\"Exception from callback object instantiation!\");");
			println("}");
		}
	}


	/**
	 * HELPER: writeMethodHelperStructJavaSkeleton() writes the struct in skeleton
	 */
	private void writeMethodHelperStructJavaSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, Set<String> callbackClasses) {

		// Generate array of parameter objects
		boolean isCallbackMethod = false;
		//String callbackType = null;
		Set<String> callbackType = new HashSet<String>();
		println("byte[] localMethodBytes = methodBytes;");
		println("rmiComm.setGetMethodBytes();");
		print("int paramLen = ");
		writeLengthStructParamClassSkeleton(methParams, methPrmTypes, method, intDecl);
		println(";");
		println("Class<?>[] paramCls = new Class<?>[paramLen];");
		println("Class<?>[] paramClsGen = new Class<?>[paramLen];");
		println("int pos = 0;");
		// Iterate again over the parameters
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				writeStructMembersJavaSkeleton(simpleType, paramType, param, method, intDecl, i);
			} else {
				String prmType = returnGenericCallbackType(methPrmTypes.get(i));
				if (callbackClasses.contains(prmType)) {
					isCallbackMethod = true;
					//callbackType = prmType;
					callbackType.add(prmType);
					println("paramCls[pos] = int[].class;");
					println("paramClsGen[pos++] = null;");
				} else {	// Generate normal classes if it's not a callback object
					String paramTypeOth = checkAndGetArray(methPrmTypes.get(i), methParams.get(i));
					println("paramCls[pos] = " + getSimpleType(getEnumType(paramTypeOth)) + ".class;");
					print("paramClsGen[pos++] = ");
					String prmTypeOth = methPrmTypes.get(i);
					if (getParamCategory(prmTypeOth) == ParamCategory.NONPRIMITIVES)
						println(getTypeOfGeneric(prmType)[0] + ".class;");
					else
						println("null;");
				}
			}
		}
		println("Object[] paramObj = rmiComm.getMethodParams(paramCls, paramClsGen, localMethodBytes);");
		writeStructMembersInitJavaSkeleton(intDecl, methParams, methPrmTypes, method);
		// Write the return value part
		writeMethodHelperReturnJavaSkeleton(intDecl, methParams, methPrmTypes, method, isCallbackMethod, callbackType, true);
	}


	/**
	 * HELPER: writeStdMethodHelperBodyJavaSkeleton() writes the standard method body helper in the skeleton class
	 */
	private void writeStdMethodHelperBodyJavaSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, Set<String> callbackClasses) {

		// Generate array of parameter objects
		boolean isCallbackMethod = false;
		//String callbackType = null;
		Set<String> callbackType = new HashSet<String>();
		println("byte[] localMethodBytes = methodBytes;");
		println("rmiComm.setGetMethodBytes();");
		print("Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { ");
		for (int i = 0; i < methParams.size(); i++) {

			String paramType = returnGenericCallbackType(methPrmTypes.get(i));
			if (callbackClasses.contains(paramType)) {
				isCallbackMethod = true;
				//callbackType = paramType;
				callbackType.add(paramType);
				print("int[].class");
			} else {	// Generate normal classes if it's not a callback object
				String prmType = checkAndGetArray(methPrmTypes.get(i), methParams.get(i));
				print(getSimpleType(getEnumType(prmType)) + ".class");
			}
			if (i != methParams.size() - 1)
				print(", ");
		}
		//println(" }, ");
		// Generate generic class if it's a generic type.. null otherwise
		print(" }, new Class<?>[] { ");
		for (int i = 0; i < methParams.size(); i++) {
			String prmType = methPrmTypes.get(i);
			if ((getParamCategory(prmType) == ParamCategory.NONPRIMITIVES) &&
				!isEnumClass(getGenericType(prmType)) &&
				!callbackClasses.contains(getGenericType(prmType)))
					print(getGenericType(prmType) + ".class");
			else
				print("null");
			if (i != methParams.size() - 1)
				print(", ");
		}
		println(" }, localMethodBytes);");
		// Write the return value part
		writeMethodHelperReturnJavaSkeleton(intDecl, methParams, methPrmTypes, method, isCallbackMethod, callbackType, false);
	}


	/**
	 * HELPER: writeMethodHelperJavaSkeleton() writes the method helper of the skeleton class
	 */
	private void writeMethodHelperJavaSkeleton(Collection<String> methods, InterfaceDecl intDecl, Set<String> callbackClasses) {

		// Use this set to handle two same methodIds
		Set<String> uniqueMethodIds = new HashSet<String>();
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			if (isStructPresent(methParams, methPrmTypes)) {	// Treat struct differently
				String methodId = intDecl.getMethodId(method);
				print("public void ___");
				String helperMethod = methodId;
				if (uniqueMethodIds.contains(methodId))
					helperMethod = helperMethod + intDecl.getMethodNumId(method);
				else
					uniqueMethodIds.add(methodId);
				String retType = intDecl.getMethodType(method);
				print(helperMethod + "(");
				boolean begin = true;
				for (int i = 0; i < methParams.size(); i++) { // Print size variables
					String paramType = methPrmTypes.get(i);
					String param = methParams.get(i);
					String simpleType = getGenericType(paramType);
					if (isStructClass(simpleType)) {
						if (!begin)	// Generate comma for not the beginning variable
							print(", ");
						else
							begin = false;
						int methodNumId = intDecl.getMethodNumId(method);
						print("int struct" + methodNumId + "Size" + i);
					}
				}
				// Check if this is "void"
				if (retType.equals("void"))
					println(") {");
				else
					println(") throws IOException {");
				writeMethodHelperStructJavaSkeleton(intDecl, methParams, methPrmTypes, method, callbackClasses);
				println("}\n");
			} else {
				String methodId = intDecl.getMethodId(method);
				print("public void ___");
				String helperMethod = methodId;
				if (uniqueMethodIds.contains(methodId))
					helperMethod = helperMethod + intDecl.getMethodNumId(method);
				else
					uniqueMethodIds.add(methodId);
				// Check if this is "void"
				String retType = intDecl.getMethodType(method);
				if (retType.equals("void"))
					println(helperMethod + "() {");
				else
					println(helperMethod + "() throws IOException {");
				// Now, write the helper body of skeleton!
				writeStdMethodHelperBodyJavaSkeleton(intDecl, methParams, methPrmTypes, method, callbackClasses);
				println("}\n");
			}
		}
		// Write method helper for structs
		writeMethodHelperStructSetupJavaSkeleton(methods, intDecl);
	}


	/**
	 * HELPER: writeMethodHelperStructSetupJavaSkeleton() writes the method helper of struct setup in skeleton class
	 */
	private void writeMethodHelperStructSetupJavaSkeleton(Collection<String> methods, 
			InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("public int ___");
					String helperMethod = methodNumId + "struct" + i;
					println(helperMethod + "() {");
					// Now, write the helper body of skeleton!
					println("byte[] localMethodBytes = methodBytes;");
					println("rmiComm.setGetMethodBytes();");
					println("Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class }, new Class<?>[] { null }, localMethodBytes);");
					println("return (int) paramObj[0];");
					println("}\n");
				}
			}
		}
	}


	/**
	 * HELPER: writeMethodHelperStructSetupJavaCallbackSkeleton() writes the method helper of struct setup in callback skeleton class
	 */
	private void writeMethodHelperStructSetupJavaCallbackSkeleton(Collection<String> methods, 
			InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("public int ___");
					String helperMethod = methodNumId + "struct" + i;
					println(helperMethod + "(IoTRMIObject rmiObj) {");
					// Now, write the helper body of skeleton!
					println("Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class }, new Class<?>[] { null });");
					println("return (int) paramObj[0];");
					println("}\n");
				}
			}
		}
	}


	/**
	 * HELPER: writeCountVarStructSkeleton() writes counter variable of struct for skeleton
	 */
	private void writeCountVarStructSkeleton(Collection<String> methods, InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					println("int struct" + methodNumId + "Size" + i + " = 0;");
				}
			}
		}
	}
	

	/**
	 * HELPER: writeInputCountVarStructJavaSkeleton() writes input counter variable of struct for skeleton
	 */
	private boolean writeInputCountVarStructJavaSkeleton(String method, InterfaceDecl intDecl) {

		List<String> methParams = intDecl.getMethodParams(method);
		List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
		boolean structExist = false;
		boolean begin = true;
		// Check for params with structs
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				structExist = true;
				if (!begin)
					print(", ");
				else
					begin = false;
				int methodNumId = intDecl.getMethodNumId(method);
				print("struct" + methodNumId + "Size" + i + "Final");
			}
		}
		return structExist;
	}

	
	/**
	 * HELPER: writeInputCountVarStructCplusSkeleton() writes input counter variable of struct for skeleton
	 */
	private boolean writeInputCountVarStructCplusSkeleton(String method, InterfaceDecl intDecl) {

		List<String> methParams = intDecl.getMethodParams(method);
		List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
		boolean structExist = false;
		boolean begin = true;
		// Check for params with structs
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				structExist = true;
				if (!begin)
					print(", ");
				else
					begin = false;
				int methodNumId = intDecl.getMethodNumId(method);
				print("struct" + methodNumId + "Size" + i);
			}
		}
		return structExist;
	}


	/**
	 * HELPER: writeMethodCallStructJavaSkeleton() writes method call for wait invoke in skeleton
	 */
	private void writeMethodCallStructJavaSkeleton(Collection<String> methods, InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("case ");
					String helperMethod = methodNumId + "struct" + i;
					String tempVar = "struct" + methodNumId + "Size" + i;
					print(intDecl.getHelperMethodNumId(helperMethod) + ": ");
					print(tempVar + " = ___");
					println(helperMethod + "(); break;");
				}
			}
		}
	}


	/**
	 * HELPER: writeMethodCallStructCplusSkeleton() writes method call for wait invoke in skeleton
	 */
	private void writeMethodCallStructCplusSkeleton(Collection<String> methods, InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("case ");
					String helperMethod = methodNumId + "struct" + i;
					String tempVar = "struct" + methodNumId + "Size" + i;
					print(intDecl.getHelperMethodNumId(helperMethod) + ": ");
					print(tempVar + " = ___");
					println(helperMethod + "(skel); break;");
				}
			}
		}
	}


	/**
	 * HELPER: writeMethodCallStructCallbackSkeleton() writes method call for wait invoke in skeleton
	 */
	private void writeMethodCallStructCallbackSkeleton(Collection<String> methods, InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("case ");
					String helperMethod = methodNumId + "struct" + i;
					String tempVar = "struct" + methodNumId + "Size" + i;
					print(intDecl.getHelperMethodNumId(helperMethod) + ": ");
					print(tempVar + " = ___");
					println(helperMethod + "(rmiObj); break;");
				}
			}
		}
	}


	/**
	 * HELPER: writeJavaMethodPermission() writes permission checks in skeleton
	 */
	private void writeJavaMethodPermission(String intface) {

		// Get all the different stubs
		Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
		for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
			String newIntface = intMeth.getKey();
			int newObjectId = getNewIntfaceObjectId(newIntface);
			println("if (_objectId == objectId) {");
			println("if (!set" + newObjectId + "Allowed.contains(methodId)) {");
			println("throw new Error(\"Object with object Id: \" + _objectId + \"  is not allowed to access method: \" + methodId);");
			println("}");
			println("}");
			println("else {");
			println("continue;");
			println("}");
		}
	}


	/**
	 * HELPER: writeFinalInputCountVarStructSkeleton() writes the final version of input counter variable of struct for skeleton
	 */
	private boolean writeFinalInputCountVarStructSkeleton(String method, InterfaceDecl intDecl) {

		List<String> methParams = intDecl.getMethodParams(method);
		List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
		boolean structExist = false;
		boolean begin = true;
		// Check for params with structs
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				structExist = true;
				int methodNumId = intDecl.getMethodNumId(method);
				println("final int struct" + methodNumId + "Size" + i + 
					"Final = struct" + methodNumId + "Size" + i + ";");
			}
		}
		return structExist;
	}


	/**
	 * HELPER: writeJavaWaitRequestInvokeMethod() writes the main loop of the skeleton class
	 */
	private void writeJavaWaitRequestInvokeMethod(Collection<String> methods, InterfaceDecl intDecl, String intface) {

		// Use this set to handle two same methodIds
		Set<String> uniqueMethodIds = new HashSet<String>();
		println("public void ___waitRequestInvokeMethod() throws IOException {");
		// Write variables here if we have callbacks or enums or structs
		writeCountVarStructSkeleton(methods, intDecl);
		println("didAlreadyInitWaitInvoke.compareAndSet(false, true);");
		println("while (true) {");
		println("if (!methodReceived.get()) {");
		println("continue;");
		println("}");
		println("methodBytes = rmiComm.getMethodBytes();");
		println("methodReceived.set(false);");
		println("int _objectId = IoTRMIComm.getObjectId(methodBytes);");
		println("int methodId = IoTRMIComm.getMethodId(methodBytes);");
		// Generate permission check
		writeJavaMethodPermission(intface);
		println("switch (methodId) {");
		// Print methods and method Ids
		for (String method : methods) {
			String methodId = intDecl.getMethodId(method);
			int methodNumId = intDecl.getMethodNumId(method);
			println("case " + methodNumId + ":");
			// Check for stuct counters
			writeFinalInputCountVarStructSkeleton(method, intDecl);
			println("new Thread() {");
			println("public void run() {");
			println("try {");
			print("___");
			String helperMethod = methodId;
			if (uniqueMethodIds.contains(methodId))
				helperMethod = helperMethod + methodNumId;
			else
				uniqueMethodIds.add(methodId);
			print(helperMethod + "(");
			writeInputCountVarStructJavaSkeleton(method, intDecl);
			println(");");
			println("}");
			println("catch (Exception ex) {");
			println("ex.printStackTrace();");
			println("}");
			println("}");
			println("}.start();");
			println("break;");
		}
		String method = "___initCallBack()";
		writeMethodCallStructJavaSkeleton(methods, intDecl);
		println("default: ");
		println("throw new Error(\"Method Id \" + methodId + \" not recognized!\");");
		println("}");
		println("}");
		println("}\n");
	}


	/**
	 * HELPER: writeReturnDidAlreadyInitWaitInvoke() writes the function to return didAlreadyInitWaitInvoke
	 */
	private void writeReturnDidAlreadyInitWaitInvoke() {

		println("public boolean didAlreadyInitWaitInvoke() {");
		println("return didAlreadyInitWaitInvoke.get();");
		println("}\n");
	}


	/**
	 * generateJavaSkeletonClass() generate skeletons based on the methods list in Java
	 */
	public void generateJavaSkeletonClass() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Open a new file to write into
			String newSkelClass = intface + "_Skeleton";
			FileWriter fw = new FileWriter(path + "/" + newSkelClass + ".java");
			pw = new PrintWriter(new BufferedWriter(fw));
			// Pass in set of methods and get import classes
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
			List<String> methods = intDecl.getMethods();
			Set<String> importClasses = getImportClasses(methods, intDecl);
			List<String> stdImportClasses = getStandardJavaImportClasses();
			List<String> allImportClasses = getAllLibClasses(stdImportClasses, importClasses);
			printImportStatements(allImportClasses);
			// Find out if there are callback objects
			Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
			boolean callbackExist = !callbackClasses.isEmpty();
			// Write class header
			println("");
			println("public class " + newSkelClass  + " implements " + intface + " {\n");
			// Write properties
			writePropertiesJavaSkeleton(intface, intDecl);
			// Write constructor
			writeConstructorJavaSkeleton(newSkelClass, intface, intDecl, methods, callbackExist);
			// Write constructor that is called when this object is a callback object
			writeCallbackConstructorJavaSkeleton(newSkelClass, intface, intDecl, methods, callbackExist);
			// Write function to return didAlreadyInitWaitInvoke
			writeReturnDidAlreadyInitWaitInvoke();
			// Write methods
			writeMethodJavaSkeleton(methods, intDecl);
			// Write method helper
			writeMethodHelperJavaSkeleton(methods, intDecl, callbackClasses);
			// Write waitRequestInvokeMethod() - main loop
			writeJavaWaitRequestInvokeMethod(methods, intDecl, intface);
			println("}");
			pw.close();
			System.out.println("IoTCompiler: Generated skeleton class " + newSkelClass + ".java...");
		}
	}

	
/*================================================================================
 *
 * 		C++ generation
 *
 *================================================================================/

	/**
	 * HELPER: writeMethodCplusLocalInterface() writes the method of the local interface
	 */
	private void writeMethodCplusLocalInterface(Collection<String> methods, InterfaceDecl intDecl) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			print("virtual " + checkAndGetCplusType(intDecl.getMethodType(method)) + " " +
				intDecl.getMethodId(method) + "(");
			for (int i = 0; i < methParams.size(); i++) {
				// Check for params with driver class types and exchange it 
				// 		with its remote interface
				String paramType = checkAndGetParamClass(methPrmTypes.get(i));
				paramType = checkAndGetCplusType(paramType);
				// Check for arrays - translate into vector in C++
				String paramComplete = checkAndGetCplusArray(paramType, methParams.get(i));
				print(paramComplete);
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(") = 0;");
		}
	}


	/**
	 * HELPER: writeMethodCplusInterface() writes the method of the interface
	 */
	private void writeMethodCplusInterface(Collection<String> methods, InterfaceDecl intDecl) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			print("virtual " + checkAndGetCplusType(intDecl.getMethodType(method)) + " " +
				intDecl.getMethodId(method) + "(");
			for (int i = 0; i < methParams.size(); i++) {
				// Check for params with driver class types and exchange it 
				// 		with its remote interface
				String paramType = methPrmTypes.get(i);
				paramType = checkAndGetCplusType(paramType);
				// Check for arrays - translate into vector in C++
				String paramComplete = checkAndGetCplusArray(paramType, methParams.get(i));
				print(paramComplete);
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(") = 0;");
		}
	}


	/**
	 * HELPER: generateEnumCplus() writes the enumeration declaration
	 */
	public void generateEnumCplus() throws IOException {

		// Create a new directory
		createDirectory(dir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Get the right StructDecl
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			EnumDecl enumDecl = (EnumDecl) decHandler.getEnumDecl(intface);
			Set<String> enumTypes = enumDecl.getEnumDeclarations();
			// Iterate over enum declarations
			for (String enType : enumTypes) {
				// Open a new file to write into
				FileWriter fw = new FileWriter(dir + "/" + enType + ".hpp");
				pw = new PrintWriter(new BufferedWriter(fw));
				// Write file headers
				println("#ifndef _" + enType.toUpperCase() + "_HPP__");
				println("#define _" + enType.toUpperCase() + "_HPP__");
				println("enum " + enType + " {");
				List<String> enumMembers = enumDecl.getMembers(enType);
				for (int i = 0; i < enumMembers.size(); i++) {

					String member = enumMembers.get(i);
					print(member);
					// Check if this is the last element (don't print a comma)
					if (i != enumMembers.size() - 1)
						println(",");
					else
						println("");
				}
				println("};\n");
				println("#endif");
				pw.close();
				System.out.println("IoTCompiler: Generated enum " + enType + ".hpp...");
			}
		}
	}


	/**
	 * HELPER: generateStructCplus() writes the struct declaration
	 */
	public void generateStructCplus() throws IOException {

		// Create a new directory
		createDirectory(dir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Get the right StructDecl
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			StructDecl structDecl = (StructDecl) decHandler.getStructDecl(intface);
			List<String> structTypes = structDecl.getStructTypes();
			// Iterate over enum declarations
			for (String stType : structTypes) {
				// Open a new file to write into
				FileWriter fw = new FileWriter(dir + "/" + stType + ".hpp");
				pw = new PrintWriter(new BufferedWriter(fw));
				// Write file headers
				println("#ifndef _" + stType.toUpperCase() + "_HPP__");
				println("#define _" + stType.toUpperCase() + "_HPP__");
				println("using namespace std;");
				println("struct " + stType + " {");
				List<String> structMemberTypes = structDecl.getMemberTypes(stType);
				List<String> structMembers = structDecl.getMembers(stType);
				for (int i = 0; i < structMembers.size(); i++) {

					String memberType = structMemberTypes.get(i);
					String member = structMembers.get(i);
					String structTypeC = checkAndGetCplusType(memberType);
					String structComplete = checkAndGetCplusArray(structTypeC, member);
					println(structComplete + ";");
				}
				println("};\n");
				println("#endif");
				pw.close();
				System.out.println("IoTCompiler: Generated struct " + stType + ".hpp...");
			}
		}
	}


	/**
	 * generateCplusLocalInterfaces() writes the local interfaces and provides type-checking.
	 * <p>
	 * It needs to rewrite and exchange USERDEFINED types in input parameters of stub
	 * and original interfaces, e.g. exchange Camera and CameraWithVideoAndRecording.
	 * The local interface has to be the input parameter for the stub and the stub 
	 * interface has to be the input parameter for the local class.
	 */
	public void generateCplusLocalInterfaces() throws IOException {

		// Create a new directory
		createDirectory(dir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Open a new file to write into
			FileWriter fw = new FileWriter(dir + "/" + intface + ".hpp");
			pw = new PrintWriter(new BufferedWriter(fw));
			// Write file headers
			println("#ifndef _" + intface.toUpperCase() + "_HPP__");
			println("#define _" + intface.toUpperCase() + "_HPP__");
			println("#include <iostream>");
			// Pass in set of methods and get include classes
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
			List<String> methods = intDecl.getMethods();
			Set<String> includeClasses = getIncludeClasses(methods, intDecl, intface, true);
			printIncludeStatements(includeClasses); println("");
			println("using namespace std;\n");
			Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
			if (!intface.equals(mainClass))	// Forward declare if not main class
				writeMethodCplusInterfaceForwardDecl(methods, intDecl, callbackClasses, true);
			println("class " + intface); println("{");
			println("public:");
			// Write methods
			writeMethodCplusLocalInterface(methods, intDecl);
			println("};");
			println("#endif");
			pw.close();
			System.out.println("IoTCompiler: Generated local interface " + intface + ".hpp...");
		}
	}


	/**
	 * HELPER: writeMethodCplusInterfaceForwardDecl() writes the forward declaration of the interface
	 */
	private void writeMethodCplusInterfaceForwardDecl(Collection<String> methods, InterfaceDecl intDecl, Set<String> callbackClasses, boolean needNewIntface) {

		Set<String> isDefined = new HashSet<String>();
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = returnGenericCallbackType(methPrmTypes.get(i));
				// Check if this has callback object
				if (callbackClasses.contains(paramType)) {
					if (!isDefined.contains(paramType)) {
						if (needNewIntface)
							println("class " + getStubInterface(paramType) + ";\n");
						else
							println("class " + paramType + ";\n");
						isDefined.add(paramType);
					}						
				}
			}
		}
	}


	/**
	 * generateCPlusInterfaces() generate stub interfaces based on the methods list in C++
	 * <p>
	 * For C++ we use virtual classe as interface
	 */
	public void generateCPlusInterfaces() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {

			Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
			for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {

				// Open a new file to write into
				String newIntface = intMeth.getKey();
				FileWriter fw = new FileWriter(path + "/" + newIntface + ".hpp");
				pw = new PrintWriter(new BufferedWriter(fw));
				DeclarationHandler decHandler = mapIntDeclHand.get(intface);
				InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
				// Write file headers
				println("#ifndef _" + newIntface.toUpperCase() + "_HPP__");
				println("#define _" + newIntface.toUpperCase() + "_HPP__");
				println("#include <iostream>");
				updateIntfaceObjIdMap(intface, newIntface);
				// Pass in set of methods and get import classes
				Set<String> methods = intMeth.getValue();
				Set<String> includeClasses = getIncludeClasses(methods, intDecl, intface, false);
				printIncludeStatements(includeClasses); println("");
				println("using namespace std;\n");
				Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
				writeMethodCplusInterfaceForwardDecl(methods, intDecl, callbackClasses, false);
				println("class " + newIntface);
				println("{");
				println("public:");
				// Write methods
				writeMethodCplusInterface(methods, intDecl);
				println("};");
				println("#endif");
				pw.close();
				System.out.println("IoTCompiler: Generated interface " + newIntface + ".hpp...");
			}
		}
	}


	/**
	 * HELPER: writeMethodDeclCplusStub() writes the method declarations of the stub
	 */
	private void writeMethodDeclCplusStub(Collection<String> methods, InterfaceDecl intDecl) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			print(checkAndGetCplusType(intDecl.getMethodType(method)) + " " +
				intDecl.getMethodId(method) + "(");
			for (int i = 0; i < methParams.size(); i++) {

				String paramType = returnGenericCallbackType(methPrmTypes.get(i));
				String methPrmType = checkAndGetCplusType(methPrmTypes.get(i));
				String methParamComplete = checkAndGetCplusArray(methPrmType, methParams.get(i));
				print(methParamComplete);
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(");");
		}
	}


	/**
	 * HELPER: writeMethodCplusStub() writes the methods of the stub
	 */
	private void writeMethodCplusStub(Collection<String> methods, InterfaceDecl intDecl, Set<String> callbackClasses, String newStubClass) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Print the mutex lock first
			int methodNumId = intDecl.getMethodNumId(method);
			String mutexVar = "mtx" + newStubClass + "MethodExec" + methodNumId;
			println("mutex " + mutexVar + ";");
			print(checkAndGetCplusType(intDecl.getMethodType(method)) + " " + newStubClass + "::" +
				intDecl.getMethodId(method) + "(");
			boolean isCallbackMethod = false;
			Set<String> callbackType = new HashSet<String>();
			for (int i = 0; i < methParams.size(); i++) {

				String paramType = returnGenericCallbackType(methPrmTypes.get(i));
				// Check if this has callback object
				if (callbackClasses.contains(paramType)) {
					isCallbackMethod = true;
					//callbackType = paramType;
					callbackType.add(paramType);
					// Even if there're 2 callback arguments, we expect them to be of the same interface
				}
				String methPrmType = checkAndGetCplusType(methPrmTypes.get(i));
				String methParamComplete = checkAndGetCplusArray(methPrmType, methParams.get(i));
				print(methParamComplete);
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(") { ");
			println("lock_guard<mutex> guard(" + mutexVar + ");");
			if (isCallbackMethod)
				writeCallbackMethodBodyCplusStub(intDecl, methParams, methPrmTypes, method, callbackType);
			writeStdMethodBodyCplusStub(intDecl, methParams, methPrmTypes, method, callbackType, isCallbackMethod);
			println("}\n");

		}
	}


	/**
	 * HELPER: writeCallbackInstantiationMethodBodyCplusStub() writes the callback object instantiation in the method of the stub class
	 */
	private void writeCallbackInstantiationMethodBodyCplusStub(String paramIdent, String callbackType, int counter) {

		println("auto it" + counter + " = IoTRMIUtil::mapSkel->find(" + paramIdent + ");");
		println("if (it" + counter + " == IoTRMIUtil::mapSkel->end()) {");
		println("int newObjIdSent = rmiComm->getObjectIdCounter();");
		println("objIdSent" + counter + ".push_back(newObjIdSent);");
		println("rmiComm->decrementObjectIdCounter();");
		println(callbackType + "_Skeleton* skel" + counter + " = new " + callbackType + "_Skeleton(" + paramIdent + ", rmiComm, newObjIdSent);");
		println("IoTRMIUtil::mapSkel->insert(make_pair(" + paramIdent + ", skel" + counter + "));");
		println("IoTRMIUtil::mapSkelId->insert(make_pair(" + paramIdent + ", newObjIdSent));");
		println("thread th" + counter + " (&" + callbackType + "_Skeleton::___waitRequestInvokeMethod, std::ref(skel" + counter + 
			"), std::ref(skel" + counter +"));");
		println("th" + counter + ".detach();");
		println("while(!skel" + counter + "->didInitWaitInvoke());");
		println("}");
		println("else");
		println("{");
		println("auto itId = IoTRMIUtil::mapSkelId->find(" + paramIdent + ");");
		println("objIdSent" + counter + ".push_back(itId->second);");
		println("}");
	}


	/**
	 * HELPER: writeCallbackMethodBodyCplusStub() writes the callback method of the stub class
	 */
	private void writeCallbackMethodBodyCplusStub(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, Set<String> callbackType) {

		// Check if this is single object, array, or list of objects
		boolean isArrayOrList = false;
		String callbackParam = null;
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				println("vector<int> objIdSent" + i + ";");
				String param = methParams.get(i);
				if (isArrayOrList(paramType, param)) {	// Generate loop				
					println("for (" + getGenericType(paramType) + "* cb : " + getSimpleIdentifier(param) + ") {");
					writeCallbackInstantiationMethodBodyCplusStub("cb", returnGenericCallbackType(paramType), i);
					isArrayOrList = true;
					callbackParam = getSimpleIdentifier(param);
				} else {
					writeCallbackInstantiationMethodBodyCplusStub(getSimpleIdentifier(param), returnGenericCallbackType(paramType), i);
				}
				if (isArrayOrList)
					println("}");
				println("vector<int> ___paramCB" + i + " = objIdSent" + i + ";");
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumTypeCplusStub() writes the enum type (convert from enum to int)
	 */
	private void checkAndWriteEnumTypeCplusStub(List<String> methParams, List<String> methPrmTypes) {

		// Iterate and find enum declarations
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			if (isEnumClass(getGenericType(paramType))) {
			// Check if this is enum type
				if (isArrayOrList(paramType, param)) {	// An array or vector
					println("int len" + i + " = " + getSimpleIdentifier(param) + ".size();");
					println("vector<int> paramEnum" + i + "(len" + i + ");");
					println("for (int i = 0; i < len" + i + "; i++) {");
					println("paramEnum" + i + "[i] = (int) " + getSimpleIdentifier(param) + "[i];");
					println("}");
				} else {	// Just one element
					println("vector<int> paramEnum" + i + "(1);");
					println("paramEnum" + i + "[0] = (int) " + param + ";");
				}
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumRetTypeCplusStub() writes the enum return type (convert from enum to int)
	 */
	private void checkAndWriteEnumRetTypeCplusStub(String retType, String method, InterfaceDecl intDecl) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(getGenericType(retType));
		// Take the inner type of generic
		if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(retType);
		if (isEnumClass(pureType)) {
		// Check if this is enum type
			println("vector<int> retEnumInt;");
			println("void* retObj = &retEnumInt;");
			println("rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);");
			writeWaitForReturnValueCplus(method, intDecl, "rmiComm->getReturnValue(retType, retObj);");
			if (isArrayOrList(retType, retType)) {	// An array or vector
				println("int retLen = retEnumInt.size();");
				println("vector<" + pureType + "> retVal(retLen);");
				println("for (int i = 0; i < retLen; i++) {");
				println("retVal[i] = (" + pureType + ") retEnumInt[i];");
				println("}");
			} else {	// Just one element
				println(pureType + " retVal = (" + pureType + ") retEnumInt[0];");
			}
			println("return retVal;");
		}
	}


	/**
	 * HELPER: checkAndWriteStructSetupCplusStub() writes the struct type setup
	 */
	private void checkAndWriteStructSetupCplusStub(List<String> methParams, List<String> methPrmTypes, 
			InterfaceDecl intDecl, String method) {
		
		// Iterate and find struct declarations
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
			// Check if this is enum type
				println("int numParam" + i + " = 1;");
				int methodNumId = intDecl.getMethodNumId(method);
				String helperMethod = methodNumId + "struct" + i;
				println("int methodIdStruct" + i + " = " + intDecl.getHelperMethodNumId(helperMethod) + ";");
				//println("string retTypeStruct" + i + " = \"void\";");
				println("string paramClsStruct" + i + "[] = { \"int\" };");
				print("int structLen" + i + " = ");
				if (isArrayOrList(paramType, param)) {	// An array
					println(getSimpleArrayType(param) + ".size();");
				} else {	// Just one element
					println("1;");
				}
				println("void* paramObjStruct" + i + "[] = { &structLen" + i + " };");
				//println("void* retStructLen" + i + " = NULL;");
				println("rmiComm->remoteCall(objectId, methodIdStruct" + i + 
						", paramClsStruct" + i + ", paramObjStruct" + i + 
						", numParam" + i + ");\n");
			}
		}
	}


	/**
	 * HELPER: writeLengthStructParamClassCplusStub() writes lengths of params
	 */
	private void writeLengthStructParamClassCplusStub(List<String> methParams, List<String> methPrmTypes) {

		// Iterate and find struct declarations - count number of params
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				int members = getNumOfMembers(simpleType);
				if (isArrayOrList(paramType, param)) {	// An array or list
					String structLen = getSimpleIdentifier(param) + ".size()";
					print(members + "*" + structLen);
				} else
					print(Integer.toString(members));
			} else
				print("1");
			if (i != methParams.size() - 1) {
				print("+");
			}
		}
	}


	/**
	 * HELPER: writeStructMembersCplusStub() writes member parameters of struct
	 */
	private void writeStructMembersCplusStub(String simpleType, String paramType, String param) {

		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArrayOrList(paramType, param)) {	// An array or list
			println("for(int i = 0; i < " + getSimpleIdentifier(param) + ".size(); i++) {");
		}
		if (isArrayOrList(paramType, param)) {	// An array or list
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("paramCls[pos] = \"" + prmTypeC + "\";");
				print("paramObj[pos++] = &" + getSimpleIdentifier(param) + "[i].");
				print(getSimpleIdentifier(members.get(i)));
				println(";");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("paramCls[pos] = \"" + prmTypeC + "\";");
				print("paramObj[pos++] = &" + param + ".");
				print(getSimpleIdentifier(members.get(i)));
				println(";");
			}
		}
	}


	/**
	 * HELPER: writeStructParamClassCplusStub() writes member parameters of struct
	 */
	private void writeStructParamClassCplusStub(List<String> methParams, List<String> methPrmTypes, Set<String> callbackType) {

		print("int numParam = ");
		writeLengthStructParamClassCplusStub(methParams, methPrmTypes);
		println(";");
		println("void* paramObj[numParam];");
		println("string paramCls[numParam];");
		println("int pos = 0;");
		// Iterate again over the parameters
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				writeStructMembersCplusStub(simpleType, paramType, param);
			} else if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				println("paramCls[pos] = \"int\";");
				println("paramObj[pos++] = &___paramCB" + i + ";");
			} else {
				String prmTypeC = checkAndGetCplusArgClsType(methPrmTypes.get(i), methParams.get(i));
				println("paramCls[pos] = \"" + prmTypeC + "\";");
				print("paramObj[pos++] = &");
				print(getEnumParam(methPrmTypes.get(i), getSimpleIdentifier(methParams.get(i)), i));
				println(";");
			}
		}
		
	}


	/**
	 * HELPER: writeStructRetMembersCplusStub() writes member parameters of struct for return statement
	 */
	private void writeStructRetMembersCplusStub(String simpleType, String retType) {

		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArrayOrList(retType, retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
		}
		if (isArrayOrList(retType, retType)) {	// An array or list
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				print("structRet[i]." + getSimpleIdentifier(members.get(i)));
				println(" = retParam" + i + "[i];");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmType = checkAndGetArray(memTypes.get(i), members.get(i));
				print("structRet." + getSimpleIdentifier(members.get(i)));
				println(" = retParam" + i + ";");
			}
		}
		println("return structRet;");
	}


	/**
	 * HELPER: writeStructReturnCplusStub() writes member parameters of struct for return statement
	 */
	private void writeStructReturnCplusStub(String simpleType, String retType, String method, InterfaceDecl intDecl) {

		// Minimum retLen is 1 if this is a single struct object
		println("int retLen = 0;");
		println("void* retLenObj = { &retLen };");
		// Handle the returned struct!!!
		println("rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);");
		writeWaitForReturnValueCplus(method, intDecl, "rmiComm->getReturnValue(retType, retLenObj);");
		int numMem = getNumOfMembers(simpleType);
		println("int numRet = " + numMem + "*retLen;");
		println("string retCls[numRet];");
		println("void* retObj[numRet];");
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		// Set up variables
		if (isArrayOrList(retType, retType)) {	// An array or list
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusType(memTypes.get(i));
				String prmType = checkAndGetCplusArrayType(prmTypeC, members.get(i));
				println(getSimpleType(getEnumType(prmType)) + " retParam" + i + "[retLen];");
			}
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusType(memTypes.get(i));
				String prmType = checkAndGetCplusArrayType(prmTypeC, members.get(i));
				println(getSimpleType(getEnumType(prmType)) + " retParam" + i + ";");
			}
		}
		println("int retPos = 0;");
		// Get the struct declaration for this struct and generate initialization code
		if (isArrayOrList(retType, retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("retCls[retPos] = \"" + prmTypeC + "\";");
				println("retObj[retPos++] = &retParam" + i + "[i];");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("retCls[retPos] = \"" + prmTypeC + "\";");
				println("retObj[retPos++] = &retParam" + i + ";");
			}
		}
		//println("rmiComm->getStructObjects(retCls, numRet, retObj);");
		writeWaitForReturnValueCplus(method, intDecl, "rmiComm->getStructObjects(retCls, numRet, retObj);");
		if (isArrayOrList(retType, retType)) {	// An array or list
			println("vector<" + simpleType + "> structRet(retLen);");
		} else
			println(simpleType + " structRet;");
		writeStructRetMembersCplusStub(simpleType, retType);
	}


	/**
	 * HELPER: writeWaitForReturnValueCplus() writes the synchronization part for return values
	 */
	private void writeWaitForReturnValueCplus(String method, InterfaceDecl intDecl, String getReturnValue) {

		println("// Waiting for return value");		
		int methodNumId = intDecl.getMethodNumId(method);
		println("while (!retValueReceived" + methodNumId + ");");
		println(getReturnValue);
		println("retValueReceived" + methodNumId + " = false;");
		println("didGetReturnBytes.exchange(true);\n");
	}


	/**
	 * HELPER: writeStdMethodBodyCplusStub() writes the standard method body in the stub class
	 */
	private void writeStdMethodBodyCplusStub(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, Set<String> callbackType, boolean isCallbackMethod) {

		checkAndWriteStructSetupCplusStub(methParams, methPrmTypes, intDecl, method);
		println("int methodId = " + intDecl.getMethodNumId(method) + ";");
		String retType = intDecl.getMethodType(method);
		println("string retType = \"" + checkAndGetCplusRetClsType(getStructType(getEnumType(retType))) + "\";");
		checkAndWriteEnumTypeCplusStub(methParams, methPrmTypes);
		// Generate array of parameter types
		if (isStructPresent(methParams, methPrmTypes)) {
			writeStructParamClassCplusStub(methParams, methPrmTypes, callbackType);
		} else {
			println("int numParam = " + methParams.size() + ";");
			print("string paramCls[] = { ");
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = returnGenericCallbackType(methPrmTypes.get(i));
				if (checkCallbackType(paramType, callbackType)) {
					print("\"int*\"");
				} else {
					String paramTypeC = checkAndGetCplusArgClsType(methPrmTypes.get(i), methParams.get(i));
					print("\"" + paramTypeC + "\"");
				}
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(" };");
			// Generate array of parameter objects
			print("void* paramObj[] = { ");
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = returnGenericCallbackType(methPrmTypes.get(i));
				if (checkCallbackType(paramType, callbackType)) // Check if this has callback object
					print("&___paramCB" + i);
				else
					print("&" + getEnumParam(methPrmTypes.get(i), getSimpleIdentifier(methParams.get(i)), i));
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(" };");
		}
		// Check if this is "void"
		if (retType.equals("void")) {
			println("rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);");
		} else { // We do have a return value
			// Generate array of parameter types
			if (isStructClass(getGenericType(getSimpleArrayType(retType)))) {
				writeStructReturnCplusStub(getGenericType(getSimpleArrayType(retType)), retType, method, intDecl);
			} else {
			// Check if the return value NONPRIMITIVES
				if (isEnumClass(getSimpleArrayType(getGenericType(retType)))) {
					checkAndWriteEnumRetTypeCplusStub(retType, method, intDecl);
				} else {
					//if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES)
					if (isArrayOrList(retType,retType))
						println(checkAndGetCplusType(retType) + " retVal;");
					else {
						println(checkAndGetCplusType(retType) + " retVal = " + generateCplusInitializer(retType) + ";");
					}
					println("void* retObj = &retVal;");
					println("rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);");
					writeWaitForReturnValueCplus(method, intDecl, "rmiComm->getReturnValue(retType, retObj);");
					println("return retVal;");
				}
			}
		}
	}


	/**
	 * HELPER: writePropertiesCplusPermission() writes the properties of the stub class
	 */
	private void writePropertiesCplusPermission(String intface) {

		Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
		for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
			String newIntface = intMeth.getKey();
			int newObjectId = getNewIntfaceObjectId(newIntface);
			println("static set<int> set" + newObjectId + "Allowed;");
		}
	}	

	/**
	 * HELPER: writePropertiesCplusStub() writes the properties of the stub class
	 */
	private void writePropertiesCplusStub(String intface, String newIntface, boolean callbackExist, 
			Set<String> callbackClasses, Set<String> methods, InterfaceDecl intDecl) {

		println("IoTRMIComm *rmiComm;");
		// Get the object Id
		Integer objId = mapIntfaceObjId.get(intface);
		println("int objectId = " + objId + ";");
		println("// Synchronization variables");
		for (String method : methods) {
			// Generate AtomicBooleans for methods that have return values
			String returnType = intDecl.getMethodType(method);
			int methodNumId = intDecl.getMethodNumId(method);
			if (!returnType.equals("void")) {
				println("bool retValueReceived" + methodNumId + " = false;");
			}
		}
		println("\n");
	}


	/**
	 * HELPER: writeConstructorCplusStub() writes the constructor of the stub class
	 */
	private void writeConstructorCplusStub(String newStubClass, boolean callbackExist, 
			Set<String> callbackClasses, Set<String> methods, InterfaceDecl intDecl) {

		println(newStubClass + "::" + newStubClass +
			"(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult) {");
		println("rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev, _bResult);");
		// Register the AtomicBoolean variables
		for (String method : methods) {
			// Generate AtomicBooleans for methods that have return values
			String returnType = intDecl.getMethodType(method);
			int methodNumId = intDecl.getMethodNumId(method);
			if (!returnType.equals("void")) {
				println("rmiComm->registerStub(objectId, " + methodNumId + ", &retValueReceived" + methodNumId + ");");
			}
		}
		println("IoTRMIUtil::mapStub->insert(make_pair(objectId, this));");
		println("}\n");
	}


	/**
	 * HELPER: writeCallbackConstructorCplusStub() writes the callback constructor of the stub class
	 */
	private void writeCallbackConstructorCplusStub(String newStubClass, boolean callbackExist, 
			Set<String> callbackClasses, Set<String> methods, InterfaceDecl intDecl) {

		println(newStubClass + "::" + newStubClass + "(IoTRMIComm* _rmiComm, int _objectId) {");
		println("rmiComm = _rmiComm;");
		println("objectId = _objectId;");
		// Register the AtomicBoolean variables
		for (String method : methods) {
			// Generate AtomicBooleans for methods that have return values
			String returnType = intDecl.getMethodType(method);
			int methodNumId = intDecl.getMethodNumId(method);
			if (!returnType.equals("void")) {
				println("rmiComm->registerStub(objectId, " + methodNumId + ", &retValueReceived" + methodNumId + ");");
			}
		}
		println("}\n");
	}


	/**
	 * HELPER: writeDeconstructorCplusStub() writes the deconstructor of the stub class
	 */
	private void writeDeconstructorCplusStub(String newStubClass, boolean callbackExist, Set<String> callbackClasses) {

		println(newStubClass + "::~" + newStubClass + "() {");
		println("if (rmiComm != NULL) {");
		println("delete rmiComm;");
		println("rmiComm = NULL;");
		println("}");
		println("}");
		println("");
	}


	/**
	 * generateCPlusStubClassesHpp() generate stubs based on the methods list in C++ (.hpp file)
	 */
	public void generateCPlusStubClassesHpp() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {

			Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
			for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
				// Open a new file to write into
				String newIntface = intMeth.getKey();
				String newStubClass = newIntface + "_Stub";
				FileWriter fw = new FileWriter(path + "/" + newStubClass + ".hpp");
				pw = new PrintWriter(new BufferedWriter(fw));
				// Write file headers
				println("#ifndef _" + newStubClass.toUpperCase() + "_HPP__");
				println("#define _" + newStubClass.toUpperCase() + "_HPP__");
				println("#include <iostream>");
				// Find out if there are callback objects
				Set<String> methods = intMeth.getValue();
				DeclarationHandler decHandler = mapIntDeclHand.get(intface);
				InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
				Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
				boolean callbackExist = !callbackClasses.isEmpty();
				println("#include <thread>");
				println("#include <mutex>");
				List<String> stdIncludeClasses = getStandardCplusIncludeClasses();
				printIncludeStatements(stdIncludeClasses); println("");
				println("#include \"" + newIntface + ".hpp\""); println("");		
				println("using namespace std;"); println("");
				println("class " + newStubClass + " : public " + newIntface); println("{");
				println("private:\n");
				writePropertiesCplusStub(intface, newIntface, callbackExist, callbackClasses, methods, intDecl);
				println("public:\n");
				// Add default constructor and destructor
				println(newStubClass + "();");
				// Declarations
				println(newStubClass + "(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult);");
				println(newStubClass + "(IoTRMIComm* _rmiComm, int _objectId);");
				println("~" + newStubClass + "();");
				// Write methods
				writeMethodDeclCplusStub(methods, intDecl);
				print("}"); println(";");
				println("#endif");
				pw.close();
				System.out.println("IoTCompiler: Generated stub class " + newStubClass + ".hpp...");
			}
		}
	}


	/**
	 * writeStubExternalCFunctions() generate external functions for .so file
	 */
	public void writeStubExternalCFunctions(String newStubClass) throws IOException {

		println("extern \"C\" void* create" + newStubClass + "(void** params) {");
		println("// Args: int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult");
		println("return new " + newStubClass + "(*((int*) params[0]), *((int*) params[1]), ((string*) params[2])->c_str(), " + 
			"*((int*) params[3]), (bool*) params[4]);");
		println("}\n");
		println("extern \"C\" void destroy" + newStubClass + "(void* t) {");
		println(newStubClass + "* obj = (" + newStubClass + "*) t;");
		println("delete obj;");
		println("}\n");
		println("extern \"C\" void init" + newStubClass + "(void* t) {");
		//println(newStubClass + "* obj = (" + newStubClass + "*) t;");
		//println("obj->init();");
		//println("while(true);");
		println("}\n");
	}


	/**
	 * generateCPlusStubClassesCpp() generate stubs based on the methods list in C++ (.cpp file)
	 */
	public void generateCPlusStubClassesCpp() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {

			Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
			for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
				// Open a new file to write into
				String newIntface = intMeth.getKey();
				String newStubClass = newIntface + "_Stub";
				FileWriter fw = new FileWriter(path + "/" + newStubClass + ".cpp");
				pw = new PrintWriter(new BufferedWriter(fw));
				// Write file headers
				println("#include <iostream>");
				// Find out if there are callback objects
				Set<String> methods = intMeth.getValue();
				DeclarationHandler decHandler = mapIntDeclHand.get(intface);
				InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
				Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
				boolean callbackExist = !callbackClasses.isEmpty();
				println("#include \"" + newStubClass + ".hpp\""); println("");
				for(String str: callbackClasses) {
					if (intface.equals(mainClass))
						println("#include \"" + str + "_Skeleton.cpp\"\n");
					else
						println("#include \"" + str + "_Skeleton.hpp\"\n");
				}
				println("using namespace std;"); println("");
				// Add default constructor and destructor
				//println(newStubClass + "() { }"); println("");
				writeConstructorCplusStub(newStubClass, callbackExist, callbackClasses, methods, intDecl);
				writeCallbackConstructorCplusStub(newStubClass, callbackExist, callbackClasses, methods, intDecl);
				writeDeconstructorCplusStub(newStubClass, callbackExist, callbackClasses);
				// Write methods
				writeMethodCplusStub(methods, intDecl, callbackClasses, newStubClass);
				// Write external functions for .so file
				writeStubExternalCFunctions(newStubClass);
				// TODO: Remove this later
				if (intface.equals(mainClass)) {
					println("int main() {");
					println("return 0;");
					println("}");
				}
				pw.close();
				System.out.println("IoTCompiler: Generated stub class " + newStubClass + ".cpp...");
			}
		}
	}


	/**
	 * HELPER: writePropertiesCplusSkeleton() writes the properties of the skeleton class
	 */
	private void writePropertiesCplusSkeleton(String intface, boolean callbackExist, Set<String> callbackClasses) {

		println(intface + " *mainObj;");
		println("IoTRMIComm *rmiComm;");
		println("char* methodBytes;");
		println("int methodLen;");
		Integer objId = mapIntfaceObjId.get(intface);
		println("int objectId = " + objId + ";");
		// Keep track of object Ids of all stubs registered to this interface
		writePropertiesCplusPermission(intface);
		println("// Synchronization variables");
		println("bool methodReceived = false;");
		println("bool didAlreadyInitWaitInvoke = false;");
		println("\n");
	}


	/**
	 * HELPER: writeObjectIdCountInitializationCplus() writes the initialization of objIdCnt variable
	 */
	private void writeObjectIdCountInitializationCplus(String newSkelClass, boolean callbackExist) {

		if (callbackExist)
			println("int " + newSkelClass + "::objIdCnt = 0;");
	}


	/**
	 * HELPER: writePermissionInitializationCplus() writes the initialization of permission set
	 */
	private void writePermissionInitializationCplus(String intface, String newSkelClass, InterfaceDecl intDecl) {

		// Keep track of object Ids of all stubs registered to this interface
		Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
		for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
			String newIntface = intMeth.getKey();
			int newObjectId = getNewIntfaceObjectId(newIntface);
			print("set<int> " + newSkelClass + "::set" + newObjectId + "Allowed { ");
			Set<String> methodIds = intMeth.getValue();
			int i = 0;
			for (String methodId : methodIds) {
				int methodNumId = intDecl.getMethodNumId(methodId);
				print(Integer.toString(methodNumId));
				// Check if this is the last element (don't print a comma)
				if (i != methodIds.size() - 1) {
					print(", ");
				}
				i++;
			}
			println(" };");
		}	
	}


	/**
	 * HELPER: writeStructPermissionCplusSkeleton() writes permission for struct helper
	 */
	private void writeStructPermissionCplusSkeleton(Collection<String> methods, InterfaceDecl intDecl, String intface) {

		// Use this set to handle two same methodIds
		for (String method : methods) {
			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					String helperMethod = methodNumId + "struct" + i;
					int helperMethodNumId = intDecl.getHelperMethodNumId(helperMethod);
					// Iterate over interfaces to give permissions to
					Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
					for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
						String newIntface = intMeth.getKey();
						int newObjectId = getNewIntfaceObjectId(newIntface);
						println("set" + newObjectId + "Allowed.insert(" + helperMethodNumId + ");");
					}
				}
			}
		}
	}


	/**
	 * HELPER: writeConstructorCplusSkeleton() writes the constructor of the skeleton class
	 */
	private void writeConstructorCplusSkeleton(String newSkelClass, String intface, boolean callbackExist, InterfaceDecl intDecl, Collection<String> methods) {

		println(newSkelClass + "::" + newSkelClass + "(" + intface + " *_mainObj, int _portSend, int _portRecv) {");
		println("bool _bResult = false;");
		println("mainObj = _mainObj;");
		println("rmiComm = new IoTRMICommServer(_portSend, _portRecv, &_bResult);");
		println("IoTRMIUtil::mapSkel->insert(make_pair(_mainObj, this));");
		println("IoTRMIUtil::mapSkelId->insert(make_pair(_mainObj, objectId));");
		println("rmiComm->registerSkeleton(objectId, &methodReceived);");
		writeStructPermissionCplusSkeleton(methods, intDecl, intface);
		println("thread th1 (&" + newSkelClass + "::___waitRequestInvokeMethod, this, this);");
		println("th1.join();");
		println("}\n");
	}


	/**
	 * HELPER: writeCallbackConstructorCplusSkeleton() writes the callback constructor of the skeleton class
	 */
	private void writeCallbackConstructorCplusSkeleton(String newSkelClass, String intface, boolean callbackExist, InterfaceDecl intDecl, Collection<String> methods) {

		println(newSkelClass + "::" + newSkelClass + "(" + intface + " *_mainObj, IoTRMIComm *_rmiComm, int _objectId) {");
		println("bool _bResult = false;");
		println("mainObj = _mainObj;");
		println("rmiComm = _rmiComm;");
		println("objectId = _objectId;");
		println("rmiComm->registerSkeleton(objectId, &methodReceived);");
		writeStructPermissionCplusSkeleton(methods, intDecl, intface);
		println("}\n");
	}


	/**
	 * HELPER: writeDeconstructorCplusSkeleton() writes the deconstructor of the skeleton class
	 */
	private void writeDeconstructorCplusSkeleton(String newSkelClass, boolean callbackExist, Set<String> callbackClasses) {

		println(newSkelClass + "::~" + newSkelClass + "() {");
		println("if (rmiComm != NULL) {");
		println("delete rmiComm;");
		println("rmiComm = NULL;");
		println("}");
		println("}");
		println("");
	}


	/**
	 * HELPER: writeStdMethodBodyCplusSkeleton() writes the standard method body in the skeleton class
	 */
	private void writeStdMethodBodyCplusSkeleton(List<String> methParams, String methodId, String methodType) {

		if (methodType.equals("void"))
			print("mainObj->" + methodId + "(");
		else
			print("return mainObj->" + methodId + "(");
		for (int i = 0; i < methParams.size(); i++) {

			print(getSimpleIdentifier(methParams.get(i)));
			// Check if this is the last element (don't print a comma)
			if (i != methParams.size() - 1) {
				print(", ");
			}
		}
		println(");");
	}


	/**
	 * HELPER: writeMethodDeclCplusSkeleton() writes the method declaration of the skeleton class
	 */
	private void writeMethodDeclCplusSkeleton(Collection<String> methods, InterfaceDecl intDecl, 
			Set<String> callbackClasses) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			String methodId = intDecl.getMethodId(method);
			String methodType = checkAndGetCplusType(intDecl.getMethodType(method));
			print(methodType + " " + methodId + "(");
			boolean isCallbackMethod = false;
			String callbackType = null;
			for (int i = 0; i < methParams.size(); i++) {

				String origParamType = methPrmTypes.get(i);
				if (callbackClasses.contains(origParamType)) { // Check if this has callback object
					isCallbackMethod = true;
					callbackType = origParamType;	
				}
				String paramType = checkAndGetParamClass(methPrmTypes.get(i));
				String methPrmType = checkAndGetCplusType(paramType);
				String methParamComplete = checkAndGetCplusArray(methPrmType, methParams.get(i));
				print(methParamComplete);
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(");");
		}
	}


	/**
	 * HELPER: writeMethodCplusSkeleton() writes the method of the skeleton class
	 */
	private void writeMethodCplusSkeleton(Collection<String> methods, InterfaceDecl intDecl, String newSkelClass) {

		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			String methodId = intDecl.getMethodId(method);
			String methodType = checkAndGetCplusType(intDecl.getMethodType(method));
			print(methodType + " " + newSkelClass + "::" + methodId + "(");
			for (int i = 0; i < methParams.size(); i++) {

				String origParamType = methPrmTypes.get(i);
				String paramType = checkAndGetParamClass(methPrmTypes.get(i));
				String methPrmType = checkAndGetCplusType(paramType);
				String methParamComplete = checkAndGetCplusArray(methPrmType, methParams.get(i));
				print(methParamComplete);
				// Check if this is the last element (don't print a comma)
				if (i != methParams.size() - 1) {
					print(", ");
				}
			}
			println(") {");
			// Now, write the body of skeleton!
			writeStdMethodBodyCplusSkeleton(methParams, methodId, intDecl.getMethodType(method));
			println("}\n");
		}
	}


	/**
	 * HELPER: writeCallbackCplusNumStubs() writes the numStubs variable
	 */
	private void writeCallbackCplusNumStubs(List<String> methParams, List<String> methPrmTypes, Set<String> callbackType) {

		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			//if (callbackType.equals(paramType)) {
			if (checkCallbackType(paramType, callbackType)) // Check if this has callback object
				//println("int numStubs" + i + " = 0;");
				println("vector<int> numStubIdArray" + i + ";");
		}
	}


	/**
	 * HELPER: writeCallbackInstantiationCplusStubGeneration() writes the instantiation of callback stubs
	 */
	private void writeCallbackInstantiationCplusStubGeneration(String exchParamType, int counter) {

		println(exchParamType + "* newStub" + counter + " = NULL;");
		println("auto it" + counter + " = IoTRMIUtil::mapStub->find(objIdRecv" + counter + ");");
		println("if (it" + counter + " == IoTRMIUtil::mapStub->end()) {");
		println("newStub" + counter + " = new " + exchParamType + "_Stub(rmiComm, objIdRecv" + counter + ");");
		println("IoTRMIUtil::mapStub->insert(make_pair(objIdRecv" + counter + ", newStub" + counter + "));");
		println("rmiComm->setObjectIdCounter(objIdRecv" + counter + ");");
		println("rmiComm->decrementObjectIdCounter();");
		println("}");
		println("else {");
		println("newStub" + counter + " = (" + exchParamType + "_Stub*) it" + counter + "->second;");
		println("}");
	}


	/**
	 * HELPER: writeCallbackCplusStubGeneration() writes the callback stub generation part
	 */
	private void writeCallbackCplusStubGeneration(List<String> methParams, List<String> methPrmTypes, Set<String> callbackType) {

		// Iterate over callback objects
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			// Generate a loop if needed
			if (checkCallbackType(paramType, callbackType)) { // Check if this has callback object
				String exchParamType = checkAndGetParamClass(getGenericType(paramType));
				if (isArrayOrList(paramType, param)) {
					println("vector<" + exchParamType + "*> stub" + i + ";");
					println("for (int i = 0; i < numStubIdArray" + i + ".size(); i++) {");
					println("int objIdRecv" + i + " = numStubIdArray" + i + "[i];");
					writeCallbackInstantiationCplusStubGeneration(exchParamType, i);
					println("stub" + i + ".push_back(newStub" + i + ");");
					println("}");
				} else {
					println("int objIdRecv" + i + " = numStubIdArray" + i + "[0];");
					writeCallbackInstantiationCplusStubGeneration(exchParamType, i);
					println(exchParamType + "* stub" + i + " = newStub" + i + ";");
				}
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumTypeCplusSkeleton() writes the enum type (convert from enum to int)
	 */
	private void checkAndWriteEnumTypeCplusSkeleton(List<String> methParams, List<String> methPrmTypes) {

		// Iterate and find enum declarations
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isEnumClass(simpleType)) {
			// Check if this is enum type
				if (isArrayOrList(paramType, param)) {	// An array
					println("int len" + i + " = paramEnumInt" + i + ".size();");
					println("vector<" + simpleType + "> paramEnum" + i + "(len" + i + ");");
					println("for (int i=0; i < len" + i + "; i++) {");
					println("paramEnum" + i + "[i] = (" + simpleType + ") paramEnumInt" + i + "[i];");
					println("}");
				} else {	// Just one element
					println(simpleType + " paramEnum" + i + ";");
					println("paramEnum" + i + " = (" + simpleType + ") paramEnumInt" + i + "[0];");
				}
			}
		}
	}


	/**
	 * HELPER: checkAndWriteEnumRetTypeCplusSkeleton() writes the enum return type (convert from enum to int)
	 */
	private void checkAndWriteEnumRetTypeCplusSkeleton(String retType) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(getGenericType(retType));
		// Take the inner type of generic
		if (getParamCategory(retType) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(retType);
		if (isEnumClass(pureType)) {
		// Check if this is enum type
			// Enum decoder
			if (isArrayOrList(retType, retType)) {	// An array
				println("int retLen = retEnum.size();");
				println("vector<int> retEnumInt(retLen);");
				println("for (int i=0; i < retLen; i++) {");
				println("retEnumInt[i] = (int) retEnum[i];");
				println("}");
			} else {	// Just one element
				println("vector<int> retEnumInt(1);");
				println("retEnumInt[0] = (int) retEnum;");
			}
		}
	}


	/**
	 * HELPER: writeMethodInputParameters() writes the parameter variables for C++ skeleton
	 */
	private void writeMethodInputParameters(List<String> methParams, List<String> methPrmTypes, 
			Set<String> callbackClasses, String methodId) {

		print(methodId + "(");
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = returnGenericCallbackType(methPrmTypes.get(i));
			if (callbackClasses.contains(paramType))
				print("stub" + i);
			else if (isEnumClass(getGenericType(paramType)))	// Check if this is enum type
				print("paramEnum" + i);
			else if (isStructClass(getGenericType(paramType)))	// Struct type
				print("paramStruct" + i);
			else
				print(getSimpleIdentifier(methParams.get(i)));
			if (i != methParams.size() - 1) {
				print(", ");
			}
		}
		println(");");
	}


	/**
	 * HELPER: writeMethodHelperReturnCplusSkeleton() writes the return statement part in skeleton
	 */
	private void writeMethodHelperReturnCplusSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, boolean isCallbackMethod, Set<String> callbackType,
			String methodId, Set<String> callbackClasses) {

		println("rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);");
		if (isCallbackMethod)
			writeCallbackCplusStubGeneration(methParams, methPrmTypes, callbackType);
		checkAndWriteEnumTypeCplusSkeleton(methParams, methPrmTypes);
		writeStructMembersInitCplusSkeleton(intDecl, methParams, methPrmTypes, method);
		// Check if this is "void"
		String retType = intDecl.getMethodType(method);
		// Check if this is "void"
		if (retType.equals("void")) {
			writeMethodInputParameters(methParams, methPrmTypes, callbackClasses, methodId);
		} else { // We do have a return value
			if (isEnumClass(getSimpleArrayType(getGenericType(retType)))) // Enum type
				print(checkAndGetCplusType(retType) + " retEnum = ");
			else if (isStructClass(getSimpleArrayType(getGenericType(retType)))) // Struct type
				print(checkAndGetCplusType(retType) + " retStruct = ");
			else
				print(checkAndGetCplusType(retType) + " retVal = ");
			writeMethodInputParameters(methParams, methPrmTypes, callbackClasses, methodId);
			checkAndWriteEnumRetTypeCplusSkeleton(retType);
			if (isStructClass(getSimpleArrayType(getGenericType(retType)))) // Struct type
				writeStructReturnCplusSkeleton(getSimpleArrayType(getGenericType(retType)), retType);
			if (isEnumClass(getSimpleArrayType(getGenericType(retType)))) // Enum type
				println("void* retObj = &retEnumInt;");
			else
				if (!isStructClass(getSimpleArrayType(getGenericType(retType)))) // Struct type
					println("void* retObj = &retVal;");
			String retTypeC = checkAndGetCplusType(retType);
			if (isStructClass(getSimpleArrayType(getGenericType(retType)))) // Struct type
				println("rmiComm->sendReturnObj(retObj, retCls, numRetObj, localMethodBytes);");
			else
				println("rmiComm->sendReturnObj(retObj, \"" + checkAndGetCplusRetClsType(getEnumType(retType)) + "\", localMethodBytes);");
		}
	}


	/**
	 * HELPER: writeStdMethodHelperBodyCplusSkeleton() writes the standard method body helper in the skeleton class
	 */
	private void writeStdMethodHelperBodyCplusSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, String methodId, Set<String> callbackClasses) {

		// Generate array of parameter types
		boolean isCallbackMethod = false;
		//String callbackType = null;
		Set<String> callbackType = new HashSet<String>();
		print("string paramCls[] = { ");
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = returnGenericCallbackType(methPrmTypes.get(i));
			if (callbackClasses.contains(paramType)) {
				isCallbackMethod = true;
				//callbackType = paramType;
				callbackType.add(paramType);
				print("\"int*\"");
			} else {	// Generate normal classes if it's not a callback object
				String paramTypeC = checkAndGetCplusArgClsType(methPrmTypes.get(i), methParams.get(i));
				print("\"" + paramTypeC + "\"");
			}
			if (i != methParams.size() - 1) {
				print(", ");
			}
		}
		println(" };");
		println("int numParam = " + methParams.size() + ";");
		if (isCallbackMethod)
			writeCallbackCplusNumStubs(methParams, methPrmTypes, callbackType);
		// Generate parameters
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = returnGenericCallbackType(methPrmTypes.get(i));
			if (!callbackClasses.contains(paramType)) {
				String methParamType = methPrmTypes.get(i);
				if (isEnumClass(getSimpleArrayType(getGenericType(methParamType)))) {	
				// Check if this is enum type
					println("vector<int> paramEnumInt" + i + ";");
				} else {
					String methPrmType = checkAndGetCplusType(methParamType);
					String methParamComplete = checkAndGetCplusArray(methPrmType, methParams.get(i));
                    println(methParamComplete + ";");
				}
			}
		}
		// Generate array of parameter objects
		print("void* paramObj[] = { ");
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = returnGenericCallbackType(methPrmTypes.get(i));
			if (callbackClasses.contains(paramType))
				print("&numStubIdArray" + i);
			else if (isEnumClass(getGenericType(paramType)))	// Check if this is enum type
				print("&paramEnumInt" + i);
			else
				print("&" + getSimpleIdentifier(methParams.get(i)));
			if (i != methParams.size() - 1) {
				print(", ");
			}
		}
		println(" };");
		// Write the return value part
		writeMethodHelperReturnCplusSkeleton(intDecl, methParams, methPrmTypes, method, isCallbackMethod, 
			callbackType, methodId, callbackClasses);
	}


	/**
	 * HELPER: writeStructMembersCplusSkeleton() writes member parameters of struct
	 */
	private void writeStructMembersCplusSkeleton(String simpleType, String paramType, 
			String param, String method, InterfaceDecl intDecl, int iVar) {

		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		int methodNumId = intDecl.getMethodNumId(method);
		String counter = "struct" + methodNumId + "Size" + iVar;
		// Set up variables
		if (isArrayOrList(paramType, param)) {	// An array or list
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusType(memTypes.get(i));
				String prmType = checkAndGetCplusArrayType(prmTypeC, members.get(i));
				println(getSimpleType(getEnumType(prmType)) + " param" + iVar + i + "[" + counter + "];");
			}
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusType(memTypes.get(i));
				String prmType = checkAndGetCplusArrayType(prmTypeC, members.get(i));
				println(getSimpleType(getEnumType(prmType)) + " param" + iVar + i + ";");
			}
		}
		if (isArrayOrList(paramType, param)) {	// An array or list
			println("for(int i = 0; i < " + counter + "; i++) {");
		}
		if (isArrayOrList(paramType, param)) {	// An array or list
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("paramCls[pos] = \"" + prmTypeC + "\";");
				println("paramObj[pos++] = &param" + iVar + i + "[i];");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("paramCls[pos] = \"" + prmTypeC + "\";");
				println("paramObj[pos++] = &param" + iVar + i + ";");
			}
		}
	}


	/**
	 * HELPER: writeStructMembersInitCplusSkeleton() writes member parameters initialization of struct
	 */
	private void writeStructMembersInitCplusSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method) {

		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				int methodNumId = intDecl.getMethodNumId(method);
				String counter = "struct" + methodNumId + "Size" + i;
				// Declaration
				if (isArrayOrList(paramType, param)) {	// An array or list
					println("vector<" + simpleType + "> paramStruct" + i + "(" + counter + ");");
				} else
					println(simpleType + " paramStruct" + i + ";");
				// Initialize members
				StructDecl structDecl = getStructDecl(simpleType);
				List<String> members = structDecl.getMembers(simpleType);
				List<String> memTypes = structDecl.getMemberTypes(simpleType);
				if (isArrayOrList(paramType, param)) {	// An array or list
					println("for(int i = 0; i < " + counter + "; i++) {");
					for (int j = 0; j < members.size(); j++) {
						print("paramStruct" + i + "[i]." + getSimpleIdentifier(members.get(j)));
						println(" = param" + i + j + "[i];");
					}
					println("}");
				} else {	// Just one struct element
					for (int j = 0; j < members.size(); j++) {
						print("paramStruct" + i + "." + getSimpleIdentifier(members.get(j)));
						println(" = param" + i + j + ";");
					}
				}
			}
		}
	}


	/**
	 * HELPER: writeStructReturnCplusSkeleton() writes parameters of struct for return statement
	 */
	private void writeStructReturnCplusSkeleton(String simpleType, String retType) {

		// Minimum retLen is 1 if this is a single struct object
		if (isArrayOrList(retType, retType))
			println("int retLen = retStruct.size();");
		else	// Just single struct object
			println("int retLen = 1;");
		println("void* retLenObj = &retLen;");
		println("rmiComm->sendReturnObj(retLenObj, \"int\", localMethodBytes);");
		int numMem = getNumOfMembers(simpleType);
		println("int numRetObj = " + numMem + "*retLen;");
		println("string retCls[numRetObj];");
		println("void* retObj[numRetObj];");
		println("int retPos = 0;");
		// Get the struct declaration for this struct and generate initialization code
		StructDecl structDecl = getStructDecl(simpleType);
		List<String> memTypes = structDecl.getMemberTypes(simpleType);
		List<String> members = structDecl.getMembers(simpleType);
		if (isArrayOrList(retType, retType)) {	// An array or list
			println("for(int i = 0; i < retLen; i++) {");
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("retCls[retPos] = \"" + prmTypeC + "\";");
				print("retObj[retPos++] = &retStruct[i].");
				print(getEnumParam(memTypes.get(i), getSimpleIdentifier(members.get(i)), i));
				println(";");
			}
			println("}");
		} else {	// Just one struct element
			for (int i = 0; i < members.size(); i++) {
				String prmTypeC = checkAndGetCplusArgClsType(memTypes.get(i), members.get(i));
				println("retCls[retPos] = \"" + prmTypeC + "\";");
				print("retObj[retPos++] = &retStruct.");
				print(getEnumParam(memTypes.get(i), getSimpleIdentifier(members.get(i)), i));
				println(";");
			}
		}

	}


	/**
	 * HELPER: writeMethodHelperStructCplusSkeleton() writes the struct in skeleton
	 */
	private void writeMethodHelperStructCplusSkeleton(InterfaceDecl intDecl, List<String> methParams,
			List<String> methPrmTypes, String method, String methodId, Set<String> callbackClasses) {

		// Generate array of parameter objects
		boolean isCallbackMethod = false;
		Set<String> callbackType = new HashSet<String>();
		print("int numParam = ");
		writeLengthStructParamClassSkeleton(methParams, methPrmTypes, method, intDecl);
		println(";");
		println("string paramCls[numParam];");
		println("void* paramObj[numParam];");
		println("int pos = 0;");
		// Iterate again over the parameters
		for (int i = 0; i < methParams.size(); i++) {
			String paramType = methPrmTypes.get(i);
			String param = methParams.get(i);
			String simpleType = getGenericType(paramType);
			if (isStructClass(simpleType)) {
				writeStructMembersCplusSkeleton(simpleType, paramType, param, method, intDecl, i);
			} else {
				String prmType = returnGenericCallbackType(methPrmTypes.get(i));
				if (callbackClasses.contains(prmType)) {
					isCallbackMethod = true;
					//callbackType = prmType;
					callbackType.add(prmType);
					//println("int numStubs" + i + " = 0;");
					println("vector<int> numStubIdArray" + i + ";");
					println("paramCls[pos] = \"int*\";");
					println("paramObj[pos++] = &numStubIdArray" + i + ";");
				} else {	// Generate normal classes if it's not a callback object
					String paramTypeC = checkAndGetCplusType(methPrmTypes.get(i));
					if (isEnumClass(getGenericType(paramTypeC))) {	// Check if this is enum type
						println("vector<int> paramEnumInt" + i + ";");
					} else {
						String methParamComplete = checkAndGetCplusArray(paramTypeC, methParams.get(i));
						println(methParamComplete + ";");
					}
					String prmTypeC = checkAndGetCplusArgClsType(methPrmTypes.get(i), methParams.get(i));
					println("paramCls[pos] = \"" + prmTypeC + "\";");
					if (isEnumClass(getGenericType(paramType)))	// Check if this is enum type
						println("paramObj[pos++] = &paramEnumInt" + i + ";");
					else
						println("paramObj[pos++] = &" + getSimpleIdentifier(methParams.get(i)) + ";");
				}
			}
		}
		// Write the return value part
		writeMethodHelperReturnCplusSkeleton(intDecl, methParams, methPrmTypes, method, isCallbackMethod, 
			callbackType, methodId, callbackClasses);
	}


	/**
	 * HELPER: writeMethodHelperDeclCplusSkeleton() writes the method helper declarations of the skeleton class
	 */
	private void writeMethodHelperDeclCplusSkeleton(Collection<String> methods, InterfaceDecl intDecl, String newSkelClass) {

		// Use this set to handle two same methodIds
		Set<String> uniqueMethodIds = new HashSet<String>();
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			if (isStructPresent(methParams, methPrmTypes)) {	// Treat struct differently
				String methodId = intDecl.getMethodId(method);
				print("void ___");
				String helperMethod = methodId;
				if (uniqueMethodIds.contains(methodId))
					helperMethod = helperMethod + intDecl.getMethodNumId(method);
				else
					uniqueMethodIds.add(methodId);
				String retType = intDecl.getMethodType(method);
				print(helperMethod + "(");
				boolean begin = true;
				for (int i = 0; i < methParams.size(); i++) { // Print size variables
					String paramType = methPrmTypes.get(i);
					String param = methParams.get(i);
					String simpleType = getGenericType(paramType);
					if (isStructClass(simpleType)) {
						if (!begin)	// Generate comma for not the beginning variable
							print(", ");
						else
							begin = false;
						int methodNumId = intDecl.getMethodNumId(method);
						print("int struct" + methodNumId + "Size" + i);
					}
				}
				println(", " + newSkelClass + "* skel);");
			} else {
				String methodId = intDecl.getMethodId(method);
				print("void ___");
				String helperMethod = methodId;
				if (uniqueMethodIds.contains(methodId))
					helperMethod = helperMethod + intDecl.getMethodNumId(method);
				else
					uniqueMethodIds.add(methodId);
				// Check if this is "void"
				String retType = intDecl.getMethodType(method);
				println(helperMethod + "(" + newSkelClass + "* skel);");
			}
		}
		// Write method helper for structs
		writeMethodHelperStructDeclSetupCplusSkeleton(methods, intDecl, newSkelClass);
	}


	/**
	 * HELPER: writeMethodHelperStructDeclSetupCplusSkeleton() writes the struct method helper declaration in skeleton class
	 */
	private void writeMethodHelperStructDeclSetupCplusSkeleton(Collection<String> methods, 
			InterfaceDecl intDecl, String newSkelClass) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("int ___");
					String helperMethod = methodNumId + "struct" + i;
					println(helperMethod + "(" + newSkelClass + "* skel);");
				}
			}
		}
	}


	/**
	 * HELPER: writeMethodBytesCopy() writes the methodBytes copy part in C++ skeleton
	 */
	private void writeMethodBytesCopy() {

		println("char* localMethodBytes = new char[methodLen];");
		println("memcpy(localMethodBytes, skel->methodBytes, methodLen);");
		println("didGetMethodBytes.exchange(true);");
	}


	/**
	 * HELPER: writeMethodHelperCplusSkeleton() writes the method helper of the skeleton class
	 */
	private void writeMethodHelperCplusSkeleton(Collection<String> methods, InterfaceDecl intDecl, 
			Set<String> callbackClasses, String newSkelClass) {

		// Use this set to handle two same methodIds
		Set<String> uniqueMethodIds = new HashSet<String>();
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			if (isStructPresent(methParams, methPrmTypes)) {	// Treat struct differently
				String methodId = intDecl.getMethodId(method);
				print("void " + newSkelClass + "::___");
				String helperMethod = methodId;
				if (uniqueMethodIds.contains(methodId))
					helperMethod = helperMethod + intDecl.getMethodNumId(method);
				else
					uniqueMethodIds.add(methodId);
				String retType = intDecl.getMethodType(method);
				print(helperMethod + "(");
				boolean begin = true;
				for (int i = 0; i < methParams.size(); i++) { // Print size variables
					String paramType = methPrmTypes.get(i);
					String param = methParams.get(i);
					String simpleType = getGenericType(paramType);
					if (isStructClass(simpleType)) {
						if (!begin)	// Generate comma for not the beginning variable
							print(", ");
						else
							begin = false;
						int methodNumId = intDecl.getMethodNumId(method);
						print("int struct" + methodNumId + "Size" + i);
					}
				}
				println(", " + newSkelClass + "* skel) {");
				writeMethodBytesCopy();
				writeMethodHelperStructCplusSkeleton(intDecl, methParams, methPrmTypes, method, methodId, callbackClasses);
				println("delete[] localMethodBytes;");
				println("}\n");
			} else {
				String methodId = intDecl.getMethodId(method);
				print("void " + newSkelClass + "::___");
				String helperMethod = methodId;
				if (uniqueMethodIds.contains(methodId))
					helperMethod = helperMethod + intDecl.getMethodNumId(method);
				else
					uniqueMethodIds.add(methodId);
				// Check if this is "void"
				String retType = intDecl.getMethodType(method);
				println(helperMethod + "(" + newSkelClass + "* skel) {");
				writeMethodBytesCopy();
				// Now, write the helper body of skeleton!
				writeStdMethodHelperBodyCplusSkeleton(intDecl, methParams, methPrmTypes, method, methodId, callbackClasses);
				println("delete[] localMethodBytes;");
				println("}\n");
			}
		}
		// Write method helper for structs
		writeMethodHelperStructSetupCplusSkeleton(methods, intDecl, newSkelClass);
	}


	/**
	 * HELPER: writeMethodHelperStructSetupCplusSkeleton() writes the method helper of struct in skeleton class
	 */
	private void writeMethodHelperStructSetupCplusSkeleton(Collection<String> methods, 
			InterfaceDecl intDecl, String newSkelClass) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("int " + newSkelClass + "::___");
					String helperMethod = methodNumId + "struct" + i;
					println(helperMethod + "(" + newSkelClass + "* skel) {");
					// Now, write the helper body of skeleton!
					writeMethodBytesCopy();
					println("string paramCls[] = { \"int\" };");
					println("int numParam = 1;");
					println("int param0 = 0;");
					println("void* paramObj[] = { &param0 };");
					println("rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);");
					println("return param0;");
					println("delete[] localMethodBytes;");
					println("}\n");
				}
			}
		}
	}


	/**
	 * HELPER: writeMethodHelperStructSetupCplusCallbackSkeleton() writes the method helper of struct in skeleton class
	 */
	private void writeMethodHelperStructSetupCplusCallbackSkeleton(Collection<String> methods, 
			InterfaceDecl intDecl) {

		// Use this set to handle two same methodIds
		for (String method : methods) {

			List<String> methParams = intDecl.getMethodParams(method);
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			// Check for params with structs
			for (int i = 0; i < methParams.size(); i++) {
				String paramType = methPrmTypes.get(i);
				String param = methParams.get(i);
				String simpleType = getGenericType(paramType);
				if (isStructClass(simpleType)) {
					int methodNumId = intDecl.getMethodNumId(method);
					print("int ___");
					String helperMethod = methodNumId + "struct" + i;
					println(helperMethod + "(IoTRMIObject* rmiObj) {");
					// Now, write the helper body of skeleton!
					println("string paramCls[] = { \"int\" };");
					println("int numParam = 1;");
					println("int param0 = 0;");
					println("void* paramObj[] = { &param0 };");
					println("rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);");
					println("return param0;");
					println("}\n");
				}
			}
		}
	}


	/**
	 * HELPER: writeCplusMethodPermission() writes permission checks in skeleton
	 */
	private void writeCplusMethodPermission(String intface) {

		// Get all the different stubs
		Map<String,Set<String>> mapNewIntMethods = mapInt2NewInts.get(intface);
		for (Map.Entry<String,Set<String>> intMeth : mapNewIntMethods.entrySet()) {
			String newIntface = intMeth.getKey();
			int newObjectId = getNewIntfaceObjectId(newIntface);
			println("if (_objectId == objectId) {");
			println("if (set" + newObjectId + "Allowed.find(methodId) == set" + newObjectId + "Allowed.end()) {");
			println("cerr << \"Object with object Id: \" << _objectId << \"  is not allowed to access method: \" << methodId << endl;");
			println("return;");
			println("}");
			println("}");
			println("else {");
			println("continue;");
			println("}");
		}
	}


	/**
	 * HELPER: writeCplusWaitRequestInvokeMethod() writes the main loop of the skeleton class
	 */
	private void writeCplusWaitRequestInvokeMethod(Collection<String> methods, InterfaceDecl intDecl, 
			boolean callbackExist, String intface, String newSkelClass) {

		// Use this set to handle two same methodIds
		Set<String> uniqueMethodIds = new HashSet<String>();
		println("void " + newSkelClass + "::___waitRequestInvokeMethod(" + newSkelClass + "* skel) {");
		// Write variables here if we have callbacks or enums or structs
		writeCountVarStructSkeleton(methods, intDecl);
		println("skel->didAlreadyInitWaitInvoke = true;");
		println("while (true) {");
		println("if (!methodReceived) {");
		println("continue;");
		println("}");
		println("skel->methodBytes = skel->rmiComm->getMethodBytes();");
		println("skel->methodLen = skel->rmiComm->getMethodLength();");
		println("methodReceived = false;");
		println("int _objectId = skel->rmiComm->getObjectId(skel->methodBytes);");
		println("int methodId = skel->rmiComm->getMethodId(skel->methodBytes);");
		// Generate permission check
		writeCplusMethodPermission(intface);
		println("switch (methodId) {");
		// Print methods and method Ids
		for (String method : methods) {
			String methodId = intDecl.getMethodId(method);
			int methodNumId = intDecl.getMethodNumId(method);
			println("case " + methodNumId + ": {");
			print("thread th" + methodNumId + " (&" + newSkelClass + "::___");
			String helperMethod = methodId;
			if (uniqueMethodIds.contains(methodId))
				helperMethod = helperMethod + methodNumId;
			else
				uniqueMethodIds.add(methodId);
			print(helperMethod + ", std::ref(skel), ");
			boolean structExists = writeInputCountVarStructCplusSkeleton(method, intDecl);
			if (structExists)
				print(", ");
			println("skel);");
			println("th" + methodNumId + ".detach(); break;");
			println("}");
		}
		writeMethodCallStructCplusSkeleton(methods, intDecl);
		println("default: ");
		println("cerr << \"Method Id \" << methodId << \" not recognized!\" << endl;");
		println("return;");
		println("}");
		println("}");
		println("}\n");
	}


	/**
	 * generateCplusSkeletonClassHpp() generate skeletons based on the methods list in C++ (.hpp file)
	 */
	public void generateCplusSkeletonClassHpp() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Open a new file to write into
			String newSkelClass = intface + "_Skeleton";
			FileWriter fw = new FileWriter(path + "/" + newSkelClass + ".hpp");
			pw = new PrintWriter(new BufferedWriter(fw));
			// Write file headers
			println("#ifndef _" + newSkelClass.toUpperCase() + "_HPP__");
			println("#define _" + newSkelClass.toUpperCase() + "_HPP__");
			println("#include <iostream>");
			println("#include \"" + intface + ".hpp\"\n");
			// Pass in set of methods and get import classes
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
			List<String> methods = intDecl.getMethods();
			List<String> stdIncludeClasses = getStandardCplusIncludeClasses();
			printIncludeStatements(stdIncludeClasses); println("");
			println("using namespace std;\n");
			// Find out if there are callback objects
			Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
			boolean callbackExist = !callbackClasses.isEmpty();
			// Write class header
			println("class " + newSkelClass + " : public " + intface); println("{");
			println("private:\n");
			// Write properties
			writePropertiesCplusSkeleton(intface, callbackExist, callbackClasses);
			println("public:\n");
			// Write constructors
			println(newSkelClass + "();");
			println(newSkelClass + "(" + intface + "*_mainObj, int _portSend, int _portRecv);");
			println(newSkelClass + "(" + intface + "*_mainObj, IoTRMIComm *rmiComm, int _objectId);");
			// Write deconstructor
			println("~" + newSkelClass + "();");
			// Write method declarations
			println("bool didInitWaitInvoke();");
			writeMethodDeclCplusSkeleton(methods, intDecl, callbackClasses);
			// Write method helper declarations
			writeMethodHelperDeclCplusSkeleton(methods, intDecl, newSkelClass);
			// Write waitRequestInvokeMethod() declaration - main loop
			println("void ___waitRequestInvokeMethod(" + newSkelClass + "* skel);");
			println("};");
			writePermissionInitializationCplus(intface, newSkelClass, intDecl);
			println("#endif");
			pw.close();
			System.out.println("IoTCompiler: Generated skeleton class " + newSkelClass + ".hpp...");
		}
	}

	/**
	 * HELPER: writeReturnDidAlreadyInitWaitInvoke() writes the function to return didAlreadyInitWaitInvoke
	 */
	private void writeReturnDidAlreadyInitWaitInvoke(String newSkelClass) {

		println("bool " + newSkelClass + "::didInitWaitInvoke() {");
		println("return didAlreadyInitWaitInvoke;");
		println("}\n");
	}


	/**
	 * writeSkelExternalCFunctions() generate external functions for .so file
	 */
	public void writeSkelExternalCFunctions(String newSkelClass, String intface) throws IOException {

		println("extern \"C\" void* create" + newSkelClass + "(void** params) {");
		println("// Args: *_mainObj, int _portSend, int _portRecv");
		println("return new " + newSkelClass + "((" + intface + "*) params[0], *((int*) params[1]), *((int*) params[2]));");
		println("}\n");
		println("extern \"C\" void destroy" + newSkelClass + "(void* t) {");
		println(newSkelClass + "* obj = (" + newSkelClass + "*) t;");
		println("delete obj;");
		println("}\n");
		println("extern \"C\" void init" + newSkelClass + "(void* t) {");
		//println(newSkelClass + "* obj = (" + newSkelClass + "*) t;");
		//println("obj->init();");
		//println("while(true);");
		println("}\n");
	}


	/**
	 * generateCplusSkeletonClassCpp() generate skeletons based on the methods list in C++ (.cpp file)
	 */
	public void generateCplusSkeletonClassCpp() throws IOException {

		// Create a new directory
		String path = createDirectories(dir, subdir);
		for (String intface : mapIntfacePTH.keySet()) {
			// Open a new file to write into
			String newSkelClass = intface + "_Skeleton";
			FileWriter fw = new FileWriter(path + "/" + newSkelClass + ".cpp");
			pw = new PrintWriter(new BufferedWriter(fw));
			// Write file headers
			println("#include <iostream>");
			println("#include \"" + newSkelClass + ".hpp\"\n");
			// Pass in set of methods and get import classes
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			InterfaceDecl intDecl = (InterfaceDecl) decHandler.getInterfaceDecl(intface);
			List<String> methods = intDecl.getMethods();
			// Find out if there are callback objects
			Set<String> callbackClasses = getCallbackClasses(methods, intDecl);
			boolean callbackExist = !callbackClasses.isEmpty();
			for(String str: callbackClasses) {
				if (intface.equals(mainClass))
					println("#include \"" + getStubInterface(str) + "_Stub.cpp\"\n");
				else
					println("#include \"" + getStubInterface(str) + "_Stub.hpp\"\n");
			}
			println("using namespace std;\n");
			// Write constructor
			writeConstructorCplusSkeleton(newSkelClass, intface, callbackExist, intDecl, methods);
			// Write callback constructor
			writeCallbackConstructorCplusSkeleton(newSkelClass, intface, callbackExist, intDecl, methods);
			// Write deconstructor
			writeDeconstructorCplusSkeleton(newSkelClass, callbackExist, callbackClasses);
			// Write didInitWaitInvoke() to return bool
			writeReturnDidAlreadyInitWaitInvoke(newSkelClass);
			// Write methods
			writeMethodCplusSkeleton(methods, intDecl, newSkelClass);
			// Write method helper
			writeMethodHelperCplusSkeleton(methods, intDecl, callbackClasses, newSkelClass);
			// Write waitRequestInvokeMethod() - main loop
			writeCplusWaitRequestInvokeMethod(methods, intDecl, callbackExist, intface, newSkelClass);
			// Write external functions for .so file
			writeSkelExternalCFunctions(newSkelClass, intface);
			// TODO: Remove this later
			if (intface.equals(mainClass)) {
				println("int main() {");
				println("return 0;");
				println("}");
			}
			pw.close();
			System.out.println("IoTCompiler: Generated skeleton class " + newSkelClass + ".cpp...");
		}
	}


	/**
	 * generateInitializer() generate initializer based on type
	 */
	public String generateCplusInitializer(String type) {

		// Generate dummy returns for now
		if (type.equals("short")||
			type.equals("int") 	||
			type.equals("long") ||
			type.equals("float")||
			type.equals("double")) {

			return "0";
		} else if ( type.equals("String") ||
					type.equals("string")) {
  
			return "\"\"";
		} else if ( type.equals("char") ||
					type.equals("byte")) {

			return "\' \'";
		} else if ( type.equals("boolean")) {

			return "false";
		} else {
			return "NULL";
		}
	}


	/**
	 * setDirectory() sets a new directory for stub files
	 */
	public void setDirectory(String _subdir) {

		subdir = _subdir;
	}


	/**
	 * printUsage() prints the usage of this compiler
	 */
	public static void printUsage() {

		System.out.println();
		System.out.println("Sentinel interface and stub compiler version 1.0");
		System.out.println("Copyright (c) 2015-2016 University of California, Irvine - Programming Language Group.");
		System.out.println("All rights reserved.");
		System.out.println("Usage:");
		System.out.println("\tjava IoTCompiler -help / --help / -h\n");
		System.out.println("\t\tDisplay this help texts\n\n");
		System.out.println("\tjava IoTCompiler [<main-policy-file> <req-policy-file>]");
		System.out.println("\tjava IoTCompiler [<main-policy-file> <req-policy-file>] [options]\n");
		System.out.println("\t\tTake one or more pairs of main-req policy files, and generate Java and/or C++ files\n");
		System.out.println("Options:");
		System.out.println("\t-java\t<directory>\tGenerate Java stub files");
		System.out.println("\t-cplus\t<directory>\tGenerate C++ stub files");
		System.out.println();
	}


	/**
	 * parseFile() prepares Lexer and Parser objects, then parses the file
	 */
	public static ParseNode parseFile(String file) {

		ParseNode pn = null;
		try {
			ComplexSymbolFactory csf = new ComplexSymbolFactory();
			ScannerBuffer lexer = 
				new ScannerBuffer(new Lexer(new BufferedReader(new FileReader(file)),csf));
			Parser parse = new Parser(lexer,csf);
			pn = (ParseNode) parse.parse().value;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("IoTCompiler: ERROR parsing policy file or wrong command line option: " + file + "\n");
		}

		return pn;
	}


	/**================
	 * Basic helper functions
	 **================
	 */
	boolean newline=true;
	int tablevel=0;

	private void print(String str) {
		if (newline) {
			int tab=tablevel;
			if (str.equals("}"))
				tab--;
			for(int i=0; i<tab; i++)
				pw.print("\t");
		}
		pw.print(str);
		updatetabbing(str);
		newline=false;
	}


	/**
	 * This function converts Java to C++ type for compilation
	 */
	private String convertType(String type) {

		if (mapPrimitives.containsKey(type))
			return mapPrimitives.get(type);
		else
			return type;
	}


	/**
	 * A collection of methods with print-to-file functionality
	 */
	private void println(String str) {
		if (newline) {
			int tab = tablevel;
			if (str.contains("}") && !str.contains("{"))
				tab--;
			for(int i=0; i<tab; i++)
				pw.print("\t");
		}
		pw.println(str);
		updatetabbing(str);
		newline = true;
	}


	private void updatetabbing(String str) {

		tablevel+=count(str,'{')-count(str,'}');
	}


	private int count(String str, char key) {
		char[] array = str.toCharArray();
		int count = 0;
		for(int i=0; i<array.length; i++) {
			if (array[i] == key)
				count++;
		}
		return count;
	}


	private void createDirectory(String dirName) {

		File file = new File(dirName);
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println("IoTCompiler: Directory " + dirName + " has been created!");
			} else {
				System.out.println("IoTCompiler: Failed to create directory " + dirName + "!");
			}
		} else {
			System.out.println("IoTCompiler: Directory " + dirName + " exists...");
		}
	}


	// Create a directory and possibly a sub directory
	private String createDirectories(String dir, String subdir) {

		String path = dir;
		createDirectory(path);
		if (subdir != null) {
			path = path + "/" + subdir;
			createDirectory(path);
		}
		return path;
	}


	// Inserting array members into a Map object
	// that maps arrKey to arrVal objects
	private void arraysToMap(Map map, Object[] arrKey, Object[] arrVal) {

		for(int i = 0; i < arrKey.length; i++) {

			map.put(arrKey[i], arrVal[i]);
		}
	}


	// Check and find object Id for new interface in mapNewIntfaceObjId (callbacks)
	// Throw an error if the new interface is not found!
	// Basically the compiler needs to parse the policy (and requires) files for callback class first
	private int getNewIntfaceObjectId(String newIntface) {

//		if (!mapNewIntfaceObjId.containsKey(newIntface)) {
//			throw new Error("IoTCompiler: Need to parse policy and requires files for callback class first! " +
//							"Please place the two files for callback class in front...\n");
//			return -1;
//		} else {
			int retObjId = mapNewIntfaceObjId.get(newIntface);
			return retObjId;
//		}
	}


	// Return parameter category, i.e. PRIMITIVES, NONPRIMITIVES, USERDEFINED, ENUM, or STRUCT
	private ParamCategory getParamCategory(String paramType) {

		if (mapPrimitives.containsKey(paramType)) {
			return ParamCategory.PRIMITIVES;
		// We can either use mapNonPrimitivesJava or mapNonPrimitivesCplus here
		} else if (mapNonPrimitivesJava.containsKey(getSimpleType(paramType))) {
			return ParamCategory.NONPRIMITIVES;
		} else if (isEnumClass(paramType)) {
			return ParamCategory.ENUM;
		} else if (isStructClass(paramType)) {
			return ParamCategory.STRUCT;
		} else
			return ParamCategory.USERDEFINED;
	}


	// Return full class name for non-primitives to generate Java import statements
	// e.g. java.util.Set for Set
	private String getNonPrimitiveJavaClass(String paramNonPrimitives) {

		return mapNonPrimitivesJava.get(paramNonPrimitives);
	}


	// Return full class name for non-primitives to generate Cplus include statements
	// e.g. #include <set> for Set
	private String getNonPrimitiveCplusClass(String paramNonPrimitives) {

		return mapNonPrimitivesCplus.get(paramNonPrimitives);
	}


	// Get simple types, e.g. HashSet for HashSet<...>
	// Basically strip off the "<...>"
	private String getSimpleType(String paramType) {

		// Check if this is generics
		if(paramType.contains("<")) {
			String[] type = paramType.split("<");
			return type[0];
		} else
			return paramType;
	}


	// Generate a set of standard classes for import statements
	private List<String> getStandardJavaIntfaceImportClasses() {

		List<String> importClasses = new ArrayList<String>();
		// Add the standard list first
		importClasses.add("java.util.List");
		importClasses.add("java.util.ArrayList");

		return importClasses;
	}


	// Generate a set of standard classes for import statements
	private List<String> getStandardJavaImportClasses() {

		List<String> importClasses = new ArrayList<String>();
		// Add the standard list first
		importClasses.add("java.io.IOException");
		importClasses.add("java.util.List");
		importClasses.add("java.util.ArrayList");
		importClasses.add("java.util.Arrays");
		importClasses.add("java.util.Map");
		importClasses.add("java.util.HashMap");
		importClasses.add("java.util.concurrent.atomic.AtomicBoolean");
		importClasses.add("iotrmi.Java.IoTRMIComm");
		importClasses.add("iotrmi.Java.IoTRMICommClient");
		importClasses.add("iotrmi.Java.IoTRMICommServer");
		importClasses.add("iotrmi.Java.IoTRMIUtil");

		return importClasses;
	}


	// Generate a set of standard classes for import statements
	private List<String> getStandardCplusIncludeClasses() {

		List<String> importClasses = new ArrayList<String>();
		// Add the standard list first
		importClasses.add("<vector>");
		importClasses.add("<set>");
		//importClasses.add("\"IoTRMICall.hpp\"");
		//importClasses.add("\"IoTRMIObject.hpp\"");
		importClasses.add("\"IoTRMIComm.hpp\"");
		importClasses.add("\"IoTRMICommClient.hpp\"");
		importClasses.add("\"IoTRMICommServer.hpp\"");

		return importClasses;
	}


	// Combine all classes for import statements
	private List<String> getAllLibClasses(Collection<String> stdLibClasses, Collection<String> libClasses) {

		List<String> allLibClasses = new ArrayList<String>(stdLibClasses);
		// Iterate over the list of import classes
		for (String str : libClasses) {
			if (!allLibClasses.contains(str)) {
				allLibClasses.add(str);
			}
		}
		return allLibClasses;
	}



	// Generate a set of classes for import statements
	private Set<String> getImportClasses(Collection<String> methods, InterfaceDecl intDecl) {

		Set<String> importClasses = new HashSet<String>();
		for (String method : methods) {
			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			for (String paramType : methPrmTypes) {

				String simpleType = getSimpleType(paramType);
				if (getParamCategory(simpleType) == ParamCategory.NONPRIMITIVES) {
					importClasses.add(getNonPrimitiveJavaClass(simpleType));
				}
			}
		}
		return importClasses;
	}


	// Handle and return the correct enum declaration
	// In Java, if we declare enum in Camera interface, then it becomes "Camera.<enum>"
	private String getEnumParamDecl(String type, InterfaceDecl intDecl) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(type);
		// Take the inner type of generic
		if (getParamCategory(type) == ParamCategory.NONPRIMITIVES)
			pureType = getTypeOfGeneric(type)[0];
		if (isEnumClass(pureType)) {
			String enumType = intDecl.getInterface() + "." + type;
			return enumType;
		} else
			return type;
	}


	// Handle and return the correct type
	private String getEnumParam(String type, String param, int i) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(type);
		// Take the inner type of generic
		if (getParamCategory(type) == ParamCategory.NONPRIMITIVES)
			pureType = getTypeOfGeneric(type)[0];
		if (isEnumClass(pureType)) {
			String enumParam = "paramEnum" + i;
			return enumParam;
		} else
			return param;
	}


	// Handle and return the correct enum declaration translate into int[]
	private String getEnumType(String type) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(type);
		// Take the inner type of generic
		if (getParamCategory(type) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(type);
		if (isEnumClass(pureType)) {
			String enumType = "int[]";
			return enumType;
		} else
			return type;
	}

	// Handle and return the correct enum declaration translate into int* for C
	private String getEnumCplusClsType(String type) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(type);
		// Take the inner type of generic
		if (getParamCategory(type) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(type);
		if (isEnumClass(pureType)) {
			String enumType = "int*";
			return enumType;
		} else
			return type;
	}


	// Handle and return the correct struct declaration
	private String getStructType(String type) {

		// Strips off array "[]" for return type
		String pureType = getSimpleArrayType(type);
		// Take the inner type of generic
		if (getParamCategory(type) == ParamCategory.NONPRIMITIVES)
			pureType = getGenericType(type);
		if (isStructClass(pureType)) {
			String structType = "int";
			return structType;
		} else
			return type;
	}


	// Check if this an enum declaration
	private boolean isEnumClass(String type) {

		// Just iterate over the set of interfaces
		for (String intface : mapIntfacePTH.keySet()) {
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			EnumDecl enumDecl = (EnumDecl) decHandler.getEnumDecl(intface);
			Set<String> setEnumDecl = enumDecl.getEnumDeclarations();
			if (setEnumDecl.contains(type))
				return true;
		}
		return false;
	}


	// Check if this an struct declaration
	private boolean isStructClass(String type) {

		// Just iterate over the set of interfaces
		for (String intface : mapIntfacePTH.keySet()) {
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			StructDecl structDecl = (StructDecl) decHandler.getStructDecl(intface);
			List<String> listStructDecl = structDecl.getStructTypes();
			if (listStructDecl.contains(type))
				return true;
		}
		return false;
	}


	// Return a struct declaration
	private StructDecl getStructDecl(String type) {

		// Just iterate over the set of interfaces
		for (String intface : mapIntfacePTH.keySet()) {
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			StructDecl structDecl = (StructDecl) decHandler.getStructDecl(intface);
			List<String> listStructDecl = structDecl.getStructTypes();
			if (listStructDecl.contains(type))
				return structDecl;
		}
		return null;
	}


	// Return number of members (-1 if not found)
	private int getNumOfMembers(String type) {

		// Just iterate over the set of interfaces
		for (String intface : mapIntfacePTH.keySet()) {
			DeclarationHandler decHandler = mapIntDeclHand.get(intface);
			StructDecl structDecl = (StructDecl) decHandler.getStructDecl(intface);
			List<String> listStructDecl = structDecl.getStructTypes();
			if (listStructDecl.contains(type))
				return structDecl.getNumOfMembers(type);
		}
		return -1;
	}


	// Generate a set of classes for include statements
	private Set<String> getIncludeClasses(Collection<String> methods, InterfaceDecl intDecl, String intface, boolean needExchange) {

		Set<String> includeClasses = new HashSet<String>();
		for (String method : methods) {

			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			List<String> methParams = intDecl.getMethodParams(method);
			for (int i = 0; i < methPrmTypes.size(); i++) {

				String genericType = getGenericType(methPrmTypes.get(i));
				String simpleType = getSimpleType(methPrmTypes.get(i));
				String param = methParams.get(i);
				if (getParamCategory(simpleType) == ParamCategory.NONPRIMITIVES) {
					includeClasses.add("<" + getNonPrimitiveCplusClass(simpleType) + ">");
				//} else if (getParamCategory(simpleType) == ParamCategory.USERDEFINED) {
				}
				if (getParamCategory(getSimpleArrayType(genericType)) == ParamCategory.USERDEFINED) {
					// For original interface, we need it exchanged... not for stub interfaces
					if (needExchange) {
						//includeClasses.add("\"" + exchangeParamType(simpleType) + ".hpp\"");
						includeClasses.add("\"" + exchangeParamType(getSimpleArrayType(genericType)) + ".hpp\"");
					} else {
						//includeClasses.add("\"" + simpleType + ".hpp\"");
						includeClasses.add("\"" + getSimpleArrayType(genericType) + ".hpp\"");
					}
				}
				if (getParamCategory(getSimpleArrayType(genericType)) == ParamCategory.ENUM) {
					includeClasses.add("\"" + genericType + ".hpp\"");
				}
				if (getParamCategory(getSimpleArrayType(genericType)) == ParamCategory.STRUCT) {
					includeClasses.add("\"" + genericType + ".hpp\"");
				}
				if (param.contains("[]")) {
				// Check if this is array for C++; translate into vector
					includeClasses.add("<vector>");
				}
			}
		}
		return includeClasses;
	}


	// Generate a set of callback classes
	private Set<String> getCallbackClasses(Collection<String> methods, InterfaceDecl intDecl) {

		Set<String> callbackClasses = new HashSet<String>();
		for (String method : methods) {

			List<String> methPrmTypes = intDecl.getMethodParamTypes(method);
			List<String> methParams = intDecl.getMethodParams(method);
			for (int i = 0; i < methPrmTypes.size(); i++) {

				String type = methPrmTypes.get(i);
				if (getParamCategory(type) == ParamCategory.USERDEFINED) {
					callbackClasses.add(type);
				} else if (getParamCategory(type) == ParamCategory.NONPRIMITIVES) {
				// Can be a List<...> of callback objects ...
					String genericType = getTypeOfGeneric(type)[0];
					if (getParamCategory(type) == ParamCategory.USERDEFINED) {
						callbackClasses.add(type);
					}
				}
			}
		}
		return callbackClasses;
	}


	// Print import statements into file
	private void printImportStatements(Collection<String> importClasses) {

		for(String cls : importClasses) {
			println("import " + cls + ";");
		}
	}


	// Print include statements into file
	private void printIncludeStatements(Collection<String> includeClasses) {

		for(String cls : includeClasses) {
			println("#include " + cls);
		}
	}


	// Get the C++ version of a non-primitive type
	// e.g. set for Set and map for Map
	// Input nonPrimitiveType has to be generics in format
	private String[] getTypeOfGeneric(String nonPrimitiveType) {

		// Handle <, >, and , for 2-type generic/template
		String[] substr = nonPrimitiveType.split("<")[1].split(">")[0].split(",");
		return substr;
	}


	// Gets generic type inside "<" and ">"
	private String getGenericType(String type) {

		// Handle <, >, and , for 2-type generic/template
		if (getParamCategory(type) == ParamCategory.NONPRIMITIVES) {
			String[] substr = type.split("<")[1].split(">")[0].split(",");
			return substr[0];
		} else
			return type;
	}


	// This helper function strips off array declaration, e.g. int[] becomes int
	private String getSimpleArrayType(String type) {

		// Handle [ for array declaration
		String substr = type;
		if (type.contains("[]")) {
			substr = type.split("\\[\\]")[0];
		}
		return substr;
	}


	// This helper function strips off array declaration, e.g. D[] becomes D
	private String getSimpleIdentifier(String ident) {

		// Handle [ for array declaration
		String substr = ident;
		if (ident.contains("[]")) {
			substr = ident.split("\\[\\]")[0];
		}
		return substr;
	}


	// Checks and gets type in C++
	private String checkAndGetCplusType(String paramType) {

		if (getParamCategory(paramType) == ParamCategory.PRIMITIVES) {
			return convertType(paramType);
		} else if (getParamCategory(paramType) == ParamCategory.NONPRIMITIVES) {

			// Check for generic/template format
			if (paramType.contains("<") && paramType.contains(">")) {

				String genericClass = getSimpleType(paramType);
				String genericType = getGenericType(paramType);
				String cplusTemplate = null;
				cplusTemplate = getNonPrimitiveCplusClass(genericClass);
				if(getParamCategory(getGenericType(paramType)) == ParamCategory.USERDEFINED) {
					cplusTemplate = cplusTemplate + "<" + genericType + "*>";
				} else {
					cplusTemplate = cplusTemplate + "<" + convertType(genericType) + ">";
				}
				return cplusTemplate;
			} else
				return getNonPrimitiveCplusClass(paramType);
		} else if(paramType.contains("[]")) {	// Array type (used for return type only)
			String cArray = "vector<" + convertType(getSimpleArrayType(paramType)) + ">";
			return cArray;
		} else if(getParamCategory(paramType) == ParamCategory.USERDEFINED) {
			return paramType + "*";
		} else
			// Just return it as is if it's not non-primitives
			return paramType;
	}


	// Detect array declaration, e.g. int A[],
	// 		then generate "int A[]" in C++ as "vector<int> A"
	private String checkAndGetCplusArray(String paramType, String param) {

		String paramComplete = null;
		// Check for array declaration
		if (param.contains("[]")) {
			paramComplete = "vector<" + paramType + "> " + param.replace("[]","");
		} else
			// Just return it as is if it's not an array
			paramComplete = paramType + " " + param;

		return paramComplete;
	}
	

	// Detect array declaration, e.g. int A[],
	// 		then generate "int A[]" in C++ as "vector<int> A"
	// This method just returns the type
	private String checkAndGetCplusArrayType(String paramType) {

		String paramTypeRet = null;
		// Check for array declaration
		if (paramType.contains("[]")) {
			String type = paramType.split("\\[\\]")[0];
			paramTypeRet = checkAndGetCplusType(type) + "[]";
		} else if (paramType.contains("vector")) {
			// Just return it as is if it's not an array
			String type = paramType.split("<")[1].split(">")[0];
			paramTypeRet = checkAndGetCplusType(type) + "[]";
		} else
			paramTypeRet = paramType;

		return paramTypeRet;
	}
	
	
	// Detect array declaration, e.g. int A[],
	// 		then generate "int A[]" in C++ as "vector<int> A"
	// This method just returns the type
	private String checkAndGetCplusArrayType(String paramType, String param) {

		String paramTypeRet = null;
		// Check for array declaration
		if (param.contains("[]")) {
			paramTypeRet = checkAndGetCplusType(paramType) + "[]";
		} else if (paramType.contains("vector")) {
			// Just return it as is if it's not an array
			String type = paramType.split("<")[1].split(">")[0];
			paramTypeRet = checkAndGetCplusType(type) + "[]";
		} else
			paramTypeRet = paramType;

		return paramTypeRet;
	}


	// Return the class type for class resolution (for return value)
	// - Check and return C++ array class, e.g. int A[] into int*
	// - Check and return C++ vector class, e.g. List<Integer> A into vector<int>
	private String checkAndGetCplusRetClsType(String paramType) {

		String paramTypeRet = null;
		// Check for array declaration
		if (paramType.contains("[]")) {
			String type = paramType.split("\\[\\]")[0];
			paramTypeRet = getSimpleArrayType(type) + "*";
		} else if (paramType.contains("<") && paramType.contains(">")) {
			// Just return it as is if it's not an array
			String type = paramType.split("<")[1].split(">")[0];
			paramTypeRet = "vector<" + getGenericType(type) + ">";
		} else
			paramTypeRet = paramType;

		return paramTypeRet;
	}


	// Return the class type for class resolution (for method arguments)
	// - Check and return C++ array class, e.g. int A[] into int*
	// - Check and return C++ vector class, e.g. List<Integer> A into vector<int>
	private String checkAndGetCplusArgClsType(String paramType, String param) {

		String paramTypeRet = getEnumCplusClsType(paramType);
		if (!paramTypeRet.equals(paramType)) 
		// Just return if it is an enum type
		// Type will still be the same if it's not an enum type
			return paramTypeRet;

		// Check for array declaration
		if (param.contains("[]")) {
			paramTypeRet = getSimpleArrayType(paramType) + "*";
		} else if (paramType.contains("<") && paramType.contains(">")) {
			// Just return it as is if it's not an array
			String type = paramType.split("<")[1].split(">")[0];
			paramTypeRet = "vector<" + getGenericType(type) + ">";
		} else
			paramTypeRet = paramType;

		return paramTypeRet;
	}


	// Detect array declaration, e.g. int A[],
	// 		then generate type "int[]"
	private String checkAndGetArray(String paramType, String param) {

		String paramTypeRet = null;
		// Check for array declaration
		if (param.contains("[]")) {
			paramTypeRet = paramType + "[]";
		} else
			// Just return it as is if it's not an array
			paramTypeRet = paramType;

		return paramTypeRet;
	}


	// Is array or list?
	private boolean isArrayOrList(String paramType, String param) {

		// Check for array declaration
		if (isArray(param))
			return true;
		else if (isList(paramType))
			return true;
		else
			return false;
	}


	// Is array? 
	// For return type we use retType as input parameter
	private boolean isArray(String param) {

		// Check for array declaration
		if (param.contains("[]"))
			return true;
		else
			return false;
	}


	// Is list?
	private boolean isList(String paramType) {

		// Check for array declaration
		if (paramType.contains("List"))
			return true;
		else
			return false;
	}


	// Get the right type for a callback object
	private String checkAndGetParamClass(String paramType) {

		// Check if this is generics
		if(getParamCategory(paramType) == ParamCategory.USERDEFINED) {
			return exchangeParamType(paramType);
		} else if (isList(paramType) &&
				(getParamCategory(getGenericType(paramType)) == ParamCategory.USERDEFINED)) {
			return "List<" + exchangeParamType(getGenericType(paramType)) + ">";
		} else
			return paramType;
	}


	// Returns the other interface for type-checking purposes for USERDEFINED
	//		classes based on the information provided in multiple policy files
	// e.g. return CameraWithXXX instead of Camera
	private String exchangeParamType(String intface) {

		// Param type that's passed is the interface name we need to look for
		//		in the map of interfaces, based on available policy files.
		DeclarationHandler decHandler = mapIntDeclHand.get(intface);
		if (decHandler != null) {
		// We've found the required interface policy files
			RequiresDecl reqDecl = (RequiresDecl) decHandler.getRequiresDecl(intface);
			Set<String> setExchInt = reqDecl.getInterfaces();
			if (setExchInt.size() == 1) {
				Iterator iter = setExchInt.iterator();
				return (String) iter.next();
			} else {
				throw new Error("IoTCompiler: Ambiguous stub interfaces: " + setExchInt.toString() + 
					". Only one new interface can be declared if the object " + intface +
					" needs to be passed in as an input parameter!\n");
			}
		} else {
		// NULL value - this means policy files missing
			throw new Error("IoTCompiler: Parameter type lookup failed for " + intface +
				"... Please provide the necessary policy files for user-defined types." +
				" If this is an array please type the brackets after the variable name," +
				" e.g. \"String str[]\", not \"String[] str\"." +
				" If this is a Collections (Java) / STL (C++) type, this compiler only" +
				" supports List/ArrayList (Java) or list (C++).\n");
		}
	}


	public static void main(String[] args) throws Exception {

		// If there is no argument or just "--help" or "-h", then invoke printUsage()
		if ((args[0].equals("-help") ||
			 args[0].equals("--help")||
			 args[0].equals("-h"))   ||
			(args.length == 0)) {

			IoTCompiler.printUsage();

		} else if (args.length > 1) {

			IoTCompiler comp = new IoTCompiler();
			int i = 0;				
			do {
				// Parse main policy file
				ParseNode pnPol = IoTCompiler.parseFile(args[i]);
				// Parse "requires" policy file
				ParseNode pnReq = IoTCompiler.parseFile(args[i+1]);
				// Get interface name
				String intface = ParseTreeHandler.getOrigIntface(pnPol);
				comp.setDataStructures(intface, pnPol, pnReq);
				comp.getMethodsForIntface(intface);
				i = i + 2;
			// 1) Check if this is the last option before "-java" or "-cplus"
			// 2) Check if this is really the last option (no "-java" or "-cplus")
			} while(!args[i].equals("-java") &&
					!args[i].equals("-cplus") &&
					(i < args.length));

			// Generate everything if we don't see "-java" or "-cplus"
			if (i == args.length) {
				comp.generateEnumJava();
				comp.generateStructJava();
				comp.generateJavaLocalInterfaces();
				comp.generateJavaInterfaces();
				comp.generateJavaStubClasses();
				comp.generateJavaSkeletonClass();
				comp.generateEnumCplus();
				comp.generateStructCplus();
				comp.generateCplusLocalInterfaces();
				comp.generateCPlusInterfaces();
				comp.generateCPlusStubClassesHpp();
				comp.generateCPlusStubClassesCpp();
				comp.generateCplusSkeletonClassHpp();
				comp.generateCplusSkeletonClassCpp();
			} else {
			// Check other options
				while(i < args.length) {
					// Error checking
					if (!args[i].equals("-java") &&
						!args[i].equals("-cplus")) {
						throw new Error("IoTCompiler: ERROR - unrecognized command line option: " + args[i] + "\n");
					} else {
						if (i + 1 < args.length) {
							comp.setDirectory(args[i+1]);
						} else
							throw new Error("IoTCompiler: ERROR - please provide <directory> after option: " + args[i] + "\n");

						if (args[i].equals("-java")) {
							comp.generateEnumJava();
							comp.generateStructJava();
							comp.generateJavaLocalInterfaces();
							comp.generateJavaInterfaces();
							comp.generateJavaStubClasses();
							comp.generateJavaSkeletonClass();
						} else {
							comp.generateEnumCplus();
							comp.generateStructCplus();
							comp.generateCplusLocalInterfaces();
							comp.generateCPlusInterfaces();
							comp.generateCPlusStubClassesHpp();
							comp.generateCPlusStubClassesCpp();
							comp.generateCplusSkeletonClassHpp();
							comp.generateCplusSkeletonClassCpp();
						}
					}
					i = i + 2;
				}
			}
		} else {
		// Need to at least have exactly 2 parameters, i.e. main policy file and requires file
			IoTCompiler.printUsage();
			throw new Error("IoTCompiler: At least two arguments (main and requires policy files) have to be provided!\n");
		}
	}
}


