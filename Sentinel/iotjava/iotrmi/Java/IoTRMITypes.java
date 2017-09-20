package iotrmi.Java;

/** Class IoTRMITypes is a class that provides type translations.
 *  <p>
 *  It stores C++ and Java types.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-03
 */

import java.util.HashMap;
import java.util.Map;

public final class IoTRMITypes {

	/**
	 * Primitive data types in Java
	 */
	public final static String[] primitivesJava = new String[] {

		"byte",			// 1 byte
		"Byte",			// 1 byte
		"short",		// 2 bytes
		"Short",		// 2 bytes
		"int",			// 4 bytes
		"Integer",		// 4 bytes
		"long",			// 8 bytes
		"Long",			// 8 bytes
		"float",		// 4 bytes
		"Float",		// 4 bytes
		"double",		// 8 bytes
		"Double",		// 8 bytes
		"boolean",		// 1 bytes
		"Boolean",		// 1 bytes
		"char",			// 2 bytes
		"Character",	// 2 bytes
		"string",		// indefinite
		"String",		// indefinite
		"void"			// 0 byte
	};


	/**
	 * Primitive data types in C++ to map the primitives list
	 */
	public final static String[] primitivesCplus = new String[] {

		"char",			// 1 byte
		"char",			// 1 byte
		"short",		// 2 bytes
		"short",		// 2 bytes
		"int",			// 4 bytes
		"int",			// 4 bytes
		"int64_t",		// 8 bytes
		"int64_t",		// 8 bytes
		"float",		// 4 bytes
		"float",		// 4 bytes
		"double",		// 8 bytes
		"double",		// 8 bytes
		"bool",			// 1 byte
		"bool",			// 1 byte
		"char",			// 2 bytes - C++ is made to follow Java convention
		"char",			// 2 bytes - 	i.e. 2 bytes for a char
		"string",		// indefinite
		"string",		// indefinite
		"void"			// 0 byte
	};


	/**
	 * Primitive sizes in Java - Long is 8 bytes and char is 2 bytes
	 */
	public final static Integer[] primitivesSizes = new Integer[] {

		1, 1, 2, 2, 4, 4, 8, 8, 4, 4, 8, 8, 1, 1, 2, 2, -1, -1, 0
	};


	/**
	 * Non-primitive Java data types
	 */
	public final static String[] nonPrimitivesJava = new String[] {

		//"Set",
		//"HashSet",
		//"Map",
		//"HashMap",
		"List",
		"ArrayList"
	};


	/**
	 * Non-primitive Java libraries based on the list above
	 */
	public final static String[] nonPrimitiveJavaLibs = new String[] {

		//"java.util.Set",
		//"java.util.HashSet",
		//"java.util.Map",
		//"java.util.HashMap",
		"java.util.List",
		"java.util.ArrayList"
	};


	/**
	 * Non-primitive C++ data types
	 */
	public final static String[] nonPrimitivesCplus = new String[] {

		//"set",
		//"unordered_set",
		//"map",
		//"unordered_map",
		//"list",
		//"list"
		"vector",
		"vector"
	};


	/**================
	 * Helper functions
	 **================
	 */
	// Inserting array members into a Map object
	// that maps arrKey to arrVal objects
	public static void arraysToMap(Map<String,String> map, String[] arrKey, String[] arrVal) {

		for(int i = 0; i < arrKey.length; i++) {

			map.put(arrKey[i], arrVal[i]);
		}
	}

	// Inserting array members into a Map object
	// that maps arrKey to arrVal objects
	public static void arraysToMap(Map<String,Integer> map, String[] arrKey, Integer[] arrVal) {

		for(int i = 0; i < arrKey.length; i++) {

			map.put(arrKey[i], arrVal[i]);
		}
	}

	// Inserting array members into a Map object
	// that maps arrKey to arrVal objects
	public static void arraysToMap(Map<Object,Object> map, Object[] arrKey, Object[] arrVal) {

		for(int i = 0; i < arrKey.length; i++) {

			map.put(arrKey[i], arrVal[i]);
		}
	}
}
