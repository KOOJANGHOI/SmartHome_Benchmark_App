package iotruntime.master;

// Java basic packages
import java.util.*;
import java.io.*;

/** Class CRuntimeInstrumenterMaster helps instrument C++ code.
 *  This class basically reads a C++ config file that has information
 *  about fields of IoTSet and IoTRelation.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2017-19-01
 */
public final class CRuntimeInstrumenterMaster {

	/**
	 * CRuntimeInstrumenterMaster class constants
	 */
	private static final String STR_IOT_SET_TYPE = "IoTSet";
	private static final String STR_IOT_RELATION_TYPE = "IoTRelation";
	private static final String STR_FIELD_NUMBER = "FIELD_NUMBER";
	private static final String STR_FIELD = "FIELD_";
	private static final String STR_FIELD_CLASS = "FIELD_CLASS_";
	// For IoTRelation second object class
	private static final String STR_FIELD_CLASS_REL = "FIELD_CLASS_REL_";
	private static final String STR_FIELD_TYPE = "FIELD_TYPE_";
	private static final String STR_CONFIG_EXTENSION = ".config";
	private static final String STR_CONFIG_FILE_PATH = "mysql/";

	/**
	 *  CRuntimeInstrumenterMaster class properties
	 */
	private HashMap<String,Object> hmObj;
	private String strObjectID;
	private boolean bVerbose;
	private String strObjectConfigFile;

	/**
	 *  Constructor
	 */
	public CRuntimeInstrumenterMaster(String strConfigFile, String strObjID, boolean _bVerbose) {

		hmObj = new HashMap<String,Object>();
		strObjectID = strObjID;
		bVerbose = _bVerbose;
		strObjectConfigFile = strConfigFile;
	}


	/**
	 * A method to parse information from a config file
	 *
	 * @param	strCfgFileName	Config file name
	 * @param	strCfgField		Config file field name
	 * @return	String
	 */
	private String parseConfigFile(String strCfgFileName, String strCfgField) {
		// Parse configuration file
		Properties prop = new Properties();
		File file = new File(strCfgFileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();
		} catch (IOException ex) {
			System.out.println("CRuntimeInstrumenterMaster: Error reading config file: " + strCfgFileName + 
				". Please make sure it contains field information!");
			ex.printStackTrace();
		}
		System.out.println("CRuntimeInstrumenterMaster: Reading " + strCfgField +
			" from config file: " + strCfgFileName + " with value: " + 
			prop.getProperty(strCfgField, null));
		// NULL is returned if the property isn't found
		return prop.getProperty(strCfgField, null);
	}


	/**
	 * A method to parse field information
	 *
	 * @return	void
	 */
	private void getFieldInfo() {

		// Parse the config file and look for field information
		String strFieldNum = parseConfigFile(strObjectConfigFile, STR_FIELD_NUMBER);
		int iNumOfField = 0;
		if (strFieldNum != null)
			iNumOfField = Integer.parseInt(strFieldNum);
		else
			throw new Error("CRuntimeInstrumenterMaster: Number of fields information not found!");
		for (int iFieldCounter=0; iFieldCounter<iNumOfField; iFieldCounter++) {
			String strFieldKey = STR_FIELD + iFieldCounter;	// Start from 0
			String strFieldClassKey = STR_FIELD_CLASS + iFieldCounter;
			String strFieldTypeKey = STR_FIELD_TYPE + iFieldCounter;
			String strField = parseConfigFile(strObjectConfigFile, strFieldKey);
			String strFieldClass = parseConfigFile(strObjectConfigFile, strFieldClassKey);
			String strFieldType = parseConfigFile(strObjectConfigFile, strFieldTypeKey);
			// Check if this is a Set class, then process it
			if (strFieldType.equals(STR_IOT_SET_TYPE)) {
				RuntimeOutput.print("CRuntimeInstrumenterMaster: IoTSet is detected!", bVerbose);
				SetInstrumenter setInstrument = new
					SetInstrumenter(strFieldClass, STR_CONFIG_FILE_PATH + strField + STR_CONFIG_EXTENSION, strObjectID, bVerbose);
				hmObj.put(strField, setInstrument);
			// Check if this is a Relation class, then process it
			} else if (strFieldType.equals(STR_IOT_RELATION_TYPE)) {
				RuntimeOutput.print("CRuntimeInstrumenterMaster: IoTRelation is detected!", bVerbose);
				String strFieldClassRelKey = STR_FIELD_CLASS_REL + iFieldCounter;
				String strFieldClassRel = parseConfigFile(strObjectConfigFile, strFieldClassRelKey);
				RelationInstrumenter relInstrument = new
					RelationInstrumenter(strFieldClass, strFieldClassRel, STR_CONFIG_FILE_PATH + strField + STR_CONFIG_EXTENSION, bVerbose);
				hmObj.put(strField, relInstrument);
			} else
				throw new Error("CRuntimeInstrumenterMaster: " + strFieldType + " not recognized!");
		}
	}


	/**
	 * A method that returns HashMap hmObj
	 *
	 * @return         HashMap<String,Object>
	 */
	public HashMap<String,Object> getFieldObjects() {

		getFieldInfo();
		return hmObj;
	}

}


