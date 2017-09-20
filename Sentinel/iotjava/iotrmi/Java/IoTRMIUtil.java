package iotrmi.Java;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/** Class IoTRMI provides utility services.
 *  <p>
 *  It provides miscellaneous (data type/value) translations.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-04
 */
public final class IoTRMIUtil {

	/**
	 * Class Properties
	 */
	private Map<String,String> mapPrimitives;
	private Map<String,Integer> mapPrimitiveSizes;
	private Map<String,String> mapNonPrimitives;

	/**
	 * Class Constants
	 */
	public final static int OBJECT_ID_LEN = 4; 		// 4 bytes = 32 bits
	public final static int METHOD_ID_LEN = 4; 		// 4 bytes = 32 bits
	public final static int PACKET_TYPE_LEN = 4; 	// 4 bytes = 32 bits
	public final static int PARAM_LEN = 4; 			// 4 bytes = 32 bits (4-byte field that stores the length of the param)
	public final static int RETURN_LEN = 4; 		// 4 bytes = 32 bits (4-byte field that stores the length of the return object)
	public final static int RET_VAL_TYPE = -1; 		// Packet type of return value
	public final static int METHOD_TYPE = 1; 		// Packet type of method

	public final static int SHT_LEN = 2;
	public final static int INT_LEN = 4;
	public final static int LNG_LEN = 8;
	public final static int FLT_LEN = 4;
	public final static int DBL_LEN = 8;
	public final static int CHR_LEN = 2;
	public final static int BYT_LEN = 1;
	public final static int BOL_LEN = 1;

	/**
	 * Public static data structure to keep track of multiple skeletons and stubs
	 */
	public static Map<Integer,Object> mapStub = new HashMap<Integer,Object>();		// Map object to its stub ID
	public static Map<Object,Object> mapSkel = new HashMap<Object,Object>();		// Map object to its skeleton
	public static Map<Object,Integer> mapSkelId = new HashMap<Object,Integer>();	// Map object to its skeleton ID


	/**
	 * Constructors
	 */
	public IoTRMIUtil() {

		mapPrimitives = new HashMap<String,String>();
			IoTRMITypes.arraysToMap(mapPrimitives, 
				IoTRMITypes.primitivesJava, IoTRMITypes.primitivesCplus);
		mapPrimitiveSizes = new HashMap<String,Integer>();
			IoTRMITypes.arraysToMap(mapPrimitiveSizes, 
				IoTRMITypes.primitivesJava, IoTRMITypes.primitivesSizes);
		mapNonPrimitives = new HashMap<String,String>();
			IoTRMITypes.arraysToMap(mapNonPrimitives, 
				IoTRMITypes.nonPrimitivesJava, IoTRMITypes.nonPrimitivesCplus);
	}

	
	/*public static void initRMICall(int port, String address, int rev) throws IOException {
		rmiCall = new IoTRMICall(port, address, rev);
	}
	
	public static void initRMICall(int localPort, int port, String address, int rev) throws IOException {
		rmiCall = new IoTRMICall(localPort, port, address, rev);
	}
	
	public static void initRMIObject(int port) throws IOException, ClassNotFoundException, 
			InstantiationException, IllegalAccessException {
		rmiObj = new IoTRMIObject(port);
	}*/
	

	/**
	 * getHashCodeBytes() gets hash value (in bytes) from method name
	 */
	public static byte[] getHashCodeBytes(String string) {

		int hash = string.hashCode();
		byte[] hashBytes = ByteBuffer.allocate(4).putInt(hash).array();
		return hashBytes;
	}


	/**================
	 * Helper methods
	 **================
	 */
	/**
	 * translateType() try to translate a type
	 * <p>
	 * It returns the original type when fails.
	 */
	public String translateType(String type) {

		if (mapPrimitives.containsKey(type))
			return mapPrimitives.get(type);
		else if (mapNonPrimitives.containsKey(type))
			return mapNonPrimitives.get(type);
		else
			return type;
	}


	/**
	 * getTypeSize() gets the size of a type
	 *
	 */
	public int getTypeSize(String type) {

		if (mapPrimitiveSizes.containsKey(type))
			return mapPrimitiveSizes.get(type);
		else
			return -1; // Size is unknown (variable length)
	}
	

	/**
	 * getTypeSize() gets the size of a type
	 *
	 */
	public static int getTypeSize(Class<?> type) {

		int size = 0;
		if (type == byte.class) {
			size = BYT_LEN;
		} else if (type == Byte.class) {
			size = BYT_LEN;
		} else if (type == short.class) {
			size = SHT_LEN;
		} else if (type == Short.class) {
			size = SHT_LEN;
		} else if (	type == int.class) {
			size = INT_LEN;
		} else if (	type == Integer.class) {
			size = INT_LEN;
		} else if (	type == long.class) {
			size = LNG_LEN;
		} else if (	type == Long.class) {
			size = LNG_LEN;
		} else if (	type == float.class) {
			size = FLT_LEN;
		} else if (	type == Float.class) {
			size = FLT_LEN;
		} else if (	type == double.class) {
			size = DBL_LEN;
		} else if ( type == Double.class) {
			size = DBL_LEN;
		} else if (	type == boolean.class) {
			size = BOL_LEN;
		} else if (	type == Boolean.class) {
			size = BOL_LEN;
		} else if (	type == char.class) {
			size = CHR_LEN;
		} else if (	type == Character[].class) {
			size = CHR_LEN;
		} else if (type == String[].class) {
			size = -1;
		} else
			throw new Error("IoTRMIUtil: Unrecognizable type: " + type.getName());

		return size;
	}

	
	/**
	 * getParamObject() converts byte array of certain object type into Object
	 */
	public static Object getParamObject(Class<?> type, Class<?> genTypeVal, byte[] paramBytes) {
		
		Object retObj = null;
		if (type == byte.class ||
			type == Byte.class) {
			retObj = (Object) paramBytes[0];
		} else if (	type == short.class ||
					type == Short.class) {
			retObj = (Object) byteArrayToShort(paramBytes);
		} else if (	type == int.class ||
					type == Integer.class) {
			retObj = (Object) byteArrayToInt(paramBytes);
		} else if (	type == long.class ||
					type == Long.class) {
			retObj = (Object) byteArrayToLong(paramBytes);
		} else if (	type == float.class ||
					type == Float.class) {
			retObj = (Object) byteArrayToFloat(paramBytes);
		} else if (	type == double.class ||
					type == Double.class) {
			retObj = (Object) byteArrayToDouble(paramBytes);
		} else if (	type == boolean.class ||
					type == Boolean.class) {
			retObj = (Object) byteArrayToBoolean(paramBytes);
		} else if (	type == char.class ||
					type == Character.class) {
			retObj = (Object) byteArrayToChar(paramBytes);
		} else if (type == String.class) {
			retObj = (Object) byteArrayToString(paramBytes);
		// Array
		} else if (type.isArray()) {
			retObj = getParamObjectArray(type, paramBytes);
		// List
		} else if (type == List.class) {
			retObj = getParamListObject(genTypeVal, paramBytes);
		} else
			throw new Error("IoTRMIUtil: Unrecognizable type: " + type.getName());
		
		return retObj;
	}


	/**
	 * getParamObjectArray() converts byte array of certain object type into array of Objects
	 */
	public static Object getParamObjectArray(Class<?> type, byte[] paramBytes) {
		
		Object retObj = null;
		if ((type == byte[].class)	||
			(type == byte.class)) {
			retObj = (Object) paramBytes;
		} else if ( (type == Byte[].class) ||
					(type == Byte.class)) {
			retObj = (Object) byteArrayToByteArray(paramBytes);
		} else if ( (type == short[].class) ||
					(type == short.class)) {
			retObj = (Object) byteArrayToShtArray(paramBytes);
		} else if ( (type == Short[].class) ||
					(type == Short.class)) {
			retObj = (Object) byteArrayToShortArray(paramBytes);
		} else if (	(type == int[].class) ||
					(type == int.class)) {
			retObj = (Object) byteArrayToIntArray(paramBytes);
		} else if (	(type == Integer[].class) ||
					(type == Integer.class)) {
			retObj = (Object) byteArrayToIntegerArray(paramBytes);
		} else if (	(type == long[].class) ||
					(type == long.class)) {
			retObj = (Object) byteArrayToLngArray(paramBytes);
		} else if (	(type == Long[].class) ||
					(type == Long.class)) {
			retObj = (Object) byteArrayToLongArray(paramBytes);
		} else if (	(type == float[].class) ||
					(type == float.class)) {
			retObj = (Object) byteArrayToFltArray(paramBytes);
		} else if (	(type == Float[].class) ||
					(type == Float.class)) {
			retObj = (Object) byteArrayToFloatArray(paramBytes);
		} else if (	(type == double[].class) ||
					(type == double.class)) {
			retObj = (Object) byteArrayToDblArray(paramBytes);
		} else if ( (type == Double[].class) ||
					(type == Double.class)) {
			retObj = (Object) byteArrayToDoubleArray(paramBytes);
		} else if (	(type == boolean[].class) || 
					(type == boolean.class)) {
			retObj = (Object) byteArrayToBolArray(paramBytes);
		} else if (	(type == Boolean[].class) ||
					(type == Boolean.class)) {
			retObj = (Object) byteArrayToBooleanArray(paramBytes);
		} else if (	(type == char[].class) ||
					(type == char.class)) {
			retObj = (Object) byteArrayToChrArray(paramBytes);
		} else if (	(type == Character[].class) ||
					(type == Character.class)) {
			retObj = (Object) byteArrayToCharacterArray(paramBytes);
		} else if ( (type == String[].class) ||
					(type == String.class)) {
			retObj = (Object) byteArrayToStringArray(paramBytes);
		} else
			throw new Error("IoTRMIUtil: Unrecognizable type: " + type.getName());
		
		return retObj;
	}


	/**
	 * getObjectBytes() converts an object into byte array
	 */
	public static byte[] getObjectBytes(Object obj) {
		
		byte[] retObjBytes = null;
		if (obj instanceof Byte) {
			retObjBytes = new byte[] { (byte) obj };
		} else if (obj instanceof Short) {
			retObjBytes = shortToByteArray((short) obj);
		} else if (obj instanceof Integer) {
			retObjBytes = intToByteArray((int) obj);
		} else if (obj instanceof Long) {
			retObjBytes = longToByteArray((long) obj);
		} else if (obj instanceof Float) {
			retObjBytes = floatToByteArray((float) obj);
		} else if (obj instanceof Double) {
			retObjBytes = doubleToByteArray((double) obj);
		} else if (obj instanceof Character) {
			retObjBytes = charToByteArray((char) obj);
		} else if (obj instanceof Boolean) {
			retObjBytes = booleanToByteArray((boolean) obj);
		} else if (obj instanceof String) {
			retObjBytes = stringToByteArray((String) obj);
		// Arrays
		} else if (obj.getClass().isArray()) {
			retObjBytes = getArrayObjectBytes(obj);
		// List and its implementations
		} else if (obj instanceof List<?>) {
			retObjBytes = listToByteArray((List<?>) obj);
		} else
			throw new Error("IoTRMIUtil: Unrecognizable object: " + obj.getClass());

		return retObjBytes;
	}


	/**
	 * getArrayObjectBytes() converts array of objects into bytes array
	 */
	public static byte[] getArrayObjectBytes(Object obj) {

		byte[] retObjBytes = null;
		if (obj instanceof byte[]) {
			retObjBytes = (byte[]) obj;
		} else if (obj instanceof Byte[]) {
			retObjBytes = arrByteToByteArray((Byte[]) obj);
		} else if (obj instanceof short[]) {
			retObjBytes = arrShortToByteArray((short[]) obj);
		} else if (obj instanceof Short[]) {
			retObjBytes = arrShortToByteArray((Short[]) obj);
		} else if (obj instanceof int[]) {
			retObjBytes = arrIntToByteArray((int[]) obj);
		} else if (obj instanceof Integer[]) {
			retObjBytes = arrIntToByteArray((Integer[]) obj);
		} else if (obj instanceof long[]) {
			retObjBytes = arrLongToByteArray((long[]) obj);
		} else if (obj instanceof Long[]) {
			retObjBytes = arrLongToByteArray((Long[]) obj);
		} else if (obj instanceof float[]) {
			retObjBytes = arrFloatToByteArray((float[]) obj);
		} else if (obj instanceof Float[]) {
			retObjBytes = arrFloatToByteArray((Float[]) obj);
		} else if (obj instanceof double[]) {
			retObjBytes = arrDoubleToByteArray((double[]) obj);
		} else if (obj instanceof Double[]) {
			retObjBytes = arrDoubleToByteArray((Double[]) obj);
		} else if (obj instanceof char[]) {
			retObjBytes = arrCharToByteArray((char[]) obj);
		} else if (obj instanceof Character[]) {
			retObjBytes = arrCharToByteArray((Character[]) obj);
		} else if (obj instanceof boolean[]) {
			retObjBytes = arrBooleanToByteArray((boolean[]) obj);
		} else if (obj instanceof Boolean[]) {
			retObjBytes = arrBooleanToByteArray((Boolean[]) obj);
		} else if (obj instanceof String[]) {
			retObjBytes = arrStringToByteArray((String[]) obj);
		} else
			throw new Error("IoTRMIUtil: Unrecognizable object: " + obj.getClass());

		return retObjBytes;	
	}


	public static byte[] listToByteArray(List<?> list) {

		// Find out the class of the type
		Iterator<?> it = list.iterator();
		Object[] arrObj = null;
		Object obj = it.next();

		if (obj instanceof Byte) {
			arrObj = list.toArray(new Byte[list.size()]);
		} else if (obj instanceof Short) {
			arrObj = list.toArray(new Short[list.size()]);
		} else if (obj instanceof Integer) {
			arrObj = list.toArray(new Integer[list.size()]);
		} else if (obj instanceof Long) {
			arrObj = list.toArray(new Long[list.size()]);
		} else if (obj instanceof Float) {
			arrObj = list.toArray(new Float[list.size()]);
		} else if (obj instanceof Double) {
			arrObj = list.toArray(new Double[list.size()]);
		} else if (obj instanceof Character) {
			arrObj = list.toArray(new Character[list.size()]);
		} else if (obj instanceof Boolean) {
			arrObj = list.toArray(new Boolean[list.size()]);
		} else if (obj instanceof String) {
			arrObj = list.toArray(new String[list.size()]);
		} else
			throw new Error("IoTRMIUtil: Unrecognizable object: " + obj.getClass());

		byte[] arrObjBytes = getArrayObjectBytes(arrObj);
		return arrObjBytes;
	}


	// Get a List object from bytes
	public static Object getParamListObject(Class<?> genericType, byte[] paramBytes) {

		List<Object> retList = new ArrayList<Object>();
		Object retObj = null;
		if (genericType == Byte.class) {
			Byte[] retArr = byteArrayToByteArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Short.class) {
			Short[] retArr = byteArrayToShortArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Integer.class) {
			Integer[] retArr = byteArrayToIntegerArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Long.class) {
			Long[] retArr = byteArrayToLongArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Float.class) {
			Float[] retArr = byteArrayToFloatArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Double.class) {
			Double[] retArr = byteArrayToDoubleArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Boolean.class) {
			Boolean[] retArr = byteArrayToBooleanArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == Character.class) {
			Character[] retArr = byteArrayToCharacterArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else if (genericType == String.class) {
			String[] retArr = byteArrayToStringArray(paramBytes);
			Collections.addAll(retList, retArr);
		} else
			throw new Error("IoTRMIUtil: Unrecognizable object: " + genericType.getSimpleName());

		return retList;
	}


	/**
	 * Converters to byte array
	 */
	// Single variables	
	public static byte[] shortToByteArray(short s) {

		ByteBuffer bb = ByteBuffer.allocate(SHT_LEN);
		bb.putShort(s);

		return bb.array();
	}


	public static byte[] intToByteArray(int i) {

		ByteBuffer bb = ByteBuffer.allocate(INT_LEN);
		bb.putInt(i);

		return bb.array();
	}


	public static byte[] longToByteArray(long l) {

		ByteBuffer bb = ByteBuffer.allocate(LNG_LEN);
		bb.putLong(l);

		return bb.array();
	}


	public static byte[] floatToByteArray(float f) {

		ByteBuffer bb = ByteBuffer.allocate(FLT_LEN);
		bb.putFloat(f);

		return bb.array();
	}


	public static byte[] doubleToByteArray(double d) {

		ByteBuffer bb = ByteBuffer.allocate(DBL_LEN);
		bb.putDouble(d);

		return bb.array();
	}


	public static byte[] charToByteArray(char c) {

		ByteBuffer bb = ByteBuffer.allocate(CHR_LEN);
		bb.putChar(c);

		return bb.array();
	}


	public static byte[] booleanToByteArray(boolean b) {

		ByteBuffer bb = ByteBuffer.allocate(BOL_LEN);
		if (b)
			bb.put((byte)1);
		else
			bb.put((byte)0);

		return bb.array();
	}


	public static byte[] stringToByteArray(String str) {

		return str.getBytes();
	}


	// Arrays
	public static byte[] arrByteToByteArray(Byte[] arrByte) {

		byte[] arrByt = new byte[arrByte.length];
		for(int i = 0; i < arrByte.length; i++) {
			arrByt[i] = arrByte[i];
		}

		return arrByt;
	}


	public static byte[] arrShortToByteArray(short[] arrShort) {

		ByteBuffer bb = ByteBuffer.allocate(SHT_LEN * arrShort.length);
		for(short s : arrShort) {
			bb.putShort(s);
		}

		return bb.array();
	}


	public static byte[] arrShortToByteArray(Short[] arrShort) {

		ByteBuffer bb = ByteBuffer.allocate(SHT_LEN * arrShort.length);
		for(Short s : arrShort) {
			bb.putShort(s);
		}

		return bb.array();
	}


	public static byte[] arrIntToByteArray(int[] arrInt) {

		ByteBuffer bb = ByteBuffer.allocate(INT_LEN * arrInt.length);
		for(int i : arrInt) {
			bb.putInt(i);
		}

		return bb.array();
	}


	public static byte[] arrIntToByteArray(Integer[] arrInt) {

		ByteBuffer bb = ByteBuffer.allocate(INT_LEN * arrInt.length);
		for(Integer i : arrInt) {
			bb.putInt(i);
		}

		return bb.array();
	}


	public static byte[] arrLongToByteArray(long[] arrLong) {

		ByteBuffer bb = ByteBuffer.allocate(LNG_LEN * arrLong.length);
		for(long l : arrLong) {
			bb.putLong(l);
		}

		return bb.array();
	}


	public static byte[] arrLongToByteArray(Long[] arrLong) {

		ByteBuffer bb = ByteBuffer.allocate(LNG_LEN * arrLong.length);
		for(Long l : arrLong) {
			bb.putLong(l);
		}

		return bb.array();
	}


	public static byte[] arrFloatToByteArray(float[] arrFloat) {

		ByteBuffer bb = ByteBuffer.allocate(FLT_LEN * arrFloat.length);
		for(float f : arrFloat) {
			bb.putFloat(f);
		}

		return bb.array();
	}


	public static byte[] arrFloatToByteArray(Float[] arrFloat) {

		ByteBuffer bb = ByteBuffer.allocate(FLT_LEN * arrFloat.length);
		for(Float f : arrFloat) {
			bb.putFloat(f);
		}

		return bb.array();
	}


	public static byte[] arrDoubleToByteArray(double[] arrDouble) {

		ByteBuffer bb = ByteBuffer.allocate(DBL_LEN * arrDouble.length);
		for(double d : arrDouble) {
			bb.putDouble(d);
		}

		return bb.array();
	}


	public static byte[] arrDoubleToByteArray(Double[] arrDouble) {

		ByteBuffer bb = ByteBuffer.allocate(DBL_LEN * arrDouble.length);
		for(Double d : arrDouble) {
			bb.putDouble(d);
		}

		return bb.array();
	}


	public static byte[] arrCharToByteArray(char[] arrChar) {

		ByteBuffer bb = ByteBuffer.allocate(CHR_LEN * arrChar.length);
		for(char c : arrChar) {
			bb.putChar(c);
		}

		return bb.array();
	}


	public static byte[] arrCharToByteArray(Character[] arrChar) {

		ByteBuffer bb = ByteBuffer.allocate(CHR_LEN * arrChar.length);
		for(Character c : arrChar) {
			bb.putChar(c);
		}

		return bb.array();
	}


	public static byte[] arrBooleanToByteArray(boolean[] arrBool) {

		ByteBuffer bb = ByteBuffer.allocate(BOL_LEN * arrBool.length);
		for(boolean b : arrBool) {
			if (b)
				bb.put((byte)1);
			else
				bb.put((byte)0);
		}

		return bb.array();
	}


	public static byte[] arrBooleanToByteArray(Boolean[] arrBool) {

		ByteBuffer bb = ByteBuffer.allocate(BOL_LEN * arrBool.length);
		for(Boolean b : arrBool) {
			if (b)
				bb.put((byte)1);
			else
				bb.put((byte)0);
		}

		return bb.array();
	}


	public static byte[] arrStringToByteArray(String[] arrString) {

		// Format of bytes: | array length | length #1 | string #1 | length #2 | string #2 | ...
		// Prepare array of bytes
		int arrLen = INT_LEN;	// First allocation for array length
		for (int i = 0; i < arrString.length; i++) {
			arrLen = arrLen + INT_LEN + arrString[i].length();
		}	
		byte[] arrStrBytes = new byte[arrLen];
		// Copy bytes
		int pos = 0;
		byte[] strArrLenBytes = intToByteArray(arrString.length);
		System.arraycopy(strArrLenBytes, 0, arrStrBytes, pos, INT_LEN);
		pos = pos + INT_LEN;
		for (String str : arrString) {

			// Copy string length
			int strLen = str.length();
			byte[] strLenBytes = intToByteArray(strLen);
			System.arraycopy(strLenBytes, 0, arrStrBytes, pos, INT_LEN);
			pos = pos + INT_LEN;
			// Copy string
			byte[] strBytes = stringToByteArray(str);
			System.arraycopy(strBytes, 0, arrStrBytes, pos, strLen);
			pos = pos + strLen;
		}

		return arrStrBytes;
	}


	/**
	 * Converters from byte array
	 */
	// Single variables	
	public static short byteArrayToShort(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getShort();
	}


	public static int byteArrayToInt(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getInt();
	}


	public static long byteArrayToLong(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getLong();
	}


	public static float byteArrayToFloat(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getFloat();
	}


	public static double byteArrayToDouble(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getDouble();
	}


	public static char byteArrayToChar(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getChar();
	}


	public static boolean byteArrayToBoolean(byte[] bytes) {

		Byte boolValByte = ByteBuffer.wrap(bytes).get();
		short boolVal = boolValByte.shortValue();
		if (boolVal == 1)
			return true;
		else
			return false;
	}


    public static String byteArrayToString(byte[] bytes) {
        return new String(bytes);
    }


	// Arrays
	public static Byte[] byteArrayToByteArray(byte[] arrByt) {

		Byte[] arrByte = new Byte[arrByt.length];
		for(int i = 0; i < arrByt.length; i++) {
			arrByte[i] = arrByt[i];
		}

		return arrByte;
	}
	
	
	public static short[] byteArrayToShtArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[SHT_LEN];
		// Prepare array
		int arrLen = bytes.length / SHT_LEN;
		short[] arr = new short[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * SHT_LEN;
			System.arraycopy(bytes, offset, elmt, 0, SHT_LEN);		
			arr[i] = byteArrayToShort(elmt);
		}

		return arr;
	}


	public static Short[] byteArrayToShortArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[SHT_LEN];
		// Prepare array
		int arrLen = bytes.length / SHT_LEN;
		Short[] arr = new Short[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * SHT_LEN;
			System.arraycopy(bytes, offset, elmt, 0, SHT_LEN);		
			arr[i] = byteArrayToShort(elmt);
		}

		return arr;
	}


	public static int[] byteArrayToIntArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[INT_LEN];
		// Prepare array
		int arrLen = bytes.length / INT_LEN;
		int[] arr = new int[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * INT_LEN;
			System.arraycopy(bytes, offset, elmt, 0, INT_LEN);		
			arr[i] = byteArrayToInt(elmt);
		}

		return arr;
	}


	public static Integer[] byteArrayToIntegerArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[INT_LEN];
		// Prepare array
		int arrLen = bytes.length / INT_LEN;
		Integer[] arr = new Integer[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * INT_LEN;
			System.arraycopy(bytes, offset, elmt, 0, INT_LEN);
			arr[i] = byteArrayToInt(elmt);
		}

		return arr;
	}


	public static long[] byteArrayToLngArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[LNG_LEN];
		// Prepare array
		int arrLen = bytes.length / LNG_LEN;
		long[] arr = new long[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * LNG_LEN;
			System.arraycopy(bytes, offset, elmt, 0, LNG_LEN);		
			arr[i] = byteArrayToLong(elmt);
		}

		return arr;
	}


	public static Long[] byteArrayToLongArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[LNG_LEN];
		// Prepare array
		int arrLen = bytes.length / LNG_LEN;
		Long[] arr = new Long[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * LNG_LEN;
			System.arraycopy(bytes, offset, elmt, 0, LNG_LEN);
			arr[i] = byteArrayToLong(elmt);
		}

		return arr;
	}


	public static float[] byteArrayToFltArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[FLT_LEN];
		// Prepare array
		int arrLen = bytes.length / FLT_LEN;
		float[] arr = new float[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * FLT_LEN;
			System.arraycopy(bytes, offset, elmt, 0, FLT_LEN);		
			arr[i] = byteArrayToFloat(elmt);
		}

		return arr;
	}


	public static Float[] byteArrayToFloatArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[FLT_LEN];
		// Prepare array
		int arrLen = bytes.length / FLT_LEN;
		Float[] arr = new Float[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * FLT_LEN;
			System.arraycopy(bytes, offset, elmt, 0, FLT_LEN);
			arr[i] = byteArrayToFloat(elmt);
		}

		return arr;
	}


	public static double[] byteArrayToDblArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[DBL_LEN];
		// Prepare array
		int arrLen = bytes.length / DBL_LEN;
		double[] arr = new double[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * DBL_LEN;
			System.arraycopy(bytes, offset, elmt, 0, DBL_LEN);
			arr[i] = byteArrayToDouble(elmt);
		}

		return arr;
	}


	public static Double[] byteArrayToDoubleArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[DBL_LEN];
		// Prepare array
		int arrLen = bytes.length / DBL_LEN;
		Double[] arr = new Double[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * DBL_LEN;
			System.arraycopy(bytes, offset, elmt, 0, DBL_LEN);
			arr[i] = byteArrayToDouble(elmt);
		}

		return arr;
	}


	public static char[] byteArrayToChrArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[CHR_LEN];
		// Prepare array
		int arrLen = bytes.length / CHR_LEN;
		char[] arr = new char[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * CHR_LEN;
			System.arraycopy(bytes, offset, elmt, 0, CHR_LEN);
			arr[i] = byteArrayToChar(elmt);
		}

		return arr;
	}


	public static Character[] byteArrayToCharacterArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[CHR_LEN];
		// Prepare array
		int arrLen = bytes.length / CHR_LEN;
		Character[] arr = new Character[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * CHR_LEN;
			System.arraycopy(bytes, offset, elmt, 0, CHR_LEN);
			arr[i] = byteArrayToChar(elmt);
		}

		return arr;
	}


	public static boolean[] byteArrayToBolArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[BOL_LEN];
		// Prepare array
		int arrLen = bytes.length / BOL_LEN;
		boolean[] arr = new boolean[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * BOL_LEN;
			System.arraycopy(bytes, offset, elmt, 0, BOL_LEN);
			arr[i] = byteArrayToBoolean(elmt);
		}

		return arr;
	}


	public static Boolean[] byteArrayToBooleanArray(byte[] bytes) {

		// Single element bytes
		byte[] elmt = new byte[BOL_LEN];
		// Prepare array
		int arrLen = bytes.length / BOL_LEN;
		Boolean[] arr = new Boolean[arrLen];
		for(int i = 0; i < arrLen; i++) {
			int offset = i * BOL_LEN;
			System.arraycopy(bytes, offset, elmt, 0, BOL_LEN);
			arr[i] = byteArrayToBoolean(elmt);
		}

		return arr;
	}


	public static String[] byteArrayToStringArray(byte[] bytes) {

		// Format of bytes: | array length | length #1 | string #1 | length #2 | string #2 | ...
		// Get string array length
		int pos = 0;
		byte[] strArrLenBytes = new byte[INT_LEN];
		System.arraycopy(bytes, pos, strArrLenBytes, 0, INT_LEN);
		int strArrLen = byteArrayToInt(strArrLenBytes);
		pos = pos + INT_LEN;
		// Prepare string array
		String[] strArray = new String[strArrLen];
		// Extract array of strings
		for(int i = 0; i < strArrLen; i++) {

			// Extract string length
			byte[] strLenBytes = new byte[INT_LEN];
			System.arraycopy(bytes, pos, strLenBytes, 0, INT_LEN);
			int strLen = byteArrayToInt(strLenBytes);
			pos = pos + INT_LEN;
			// Extract string
			byte[] strBytes = new byte[strLen];
			System.arraycopy(bytes, pos, strBytes, 0, strLen);
			pos = pos + strLen;
			strArray[i] = byteArrayToString(strBytes);
		}

		return strArray;
	}


	/**
	 * toByteArray() gets Object and return its byte array
	 * <p>
	 * Adapted from http://www.java2s.com/
	 * 		@see <a href="http://www.java2s.com/Code/Java/File-Input-
	 * 		Output/Convertobjecttobytearrayandconvertbytearraytoobject.htm"</a>
	 */
    // toByteArray and toObject are taken from: http://tinyurl.com/69h8l7x
    public static byte[] toByteArray(Object obj) throws IOException {

        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {

            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {

            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }


	/**
	 * toObject() gets byte array and return its Object
	 * <p>
	 * Adapted from http://www.java2s.com/
	 * 		@see <a href="http://www.java2s.com/Code/Java/File-Input-
	 * 		Output/Convertobjecttobytearrayandconvertbytearraytoobject.htm"</a>
	 */
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {

        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {

            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {

            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }
}
