package iotruntime.master;

// Java standard libraries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Class ObjectAddressInitHandler is a class that maintains
 *  a data structure that preserves a collection information
 *  for creation and re-initialization of driver object's IoTSet
 *  that usually contains IoTDeviceAddress, IoTZigbeeAddress,
 *  or IoTAddress. These are read from the database when we
 *  instrument the fields for policy generation.
 *  
 *  +------------+-----------------------------+
 *  | FIELD_NAME | ARRAYLIST OF arrFieldValues |
 *  +------------+-----------------------------+
 *  | XXXXXXXXXX | #1 | XXXXX                  |
 *  |            | #2 | XXXXX                  |
 *  |            | #3 | XXXXX                  |
 *  |            | ...                         |
 *  |            |                             |
 *  |            |                             |
 *  |            |                             |
 *  +------------+-----------------------------+
 *  | XXXXXXXXXX | #1 | XXXXX                  |
 *  |            | #2 | XXXXX                  |
 *  |            | #3 | XXXXX                  |
 *  |            | ...                         |
 *  |            |                             |
 *  |            |                             |
 *  |            |                             |
 *  +------------+-----------------------------+
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-06-24
 */
public final class ObjectAddressInitHandler {


	/**
	 * ObjectInitHandler class properties
	 */
	private Map<String, List<Object[]>> mapFieldToValuesList;
	private boolean bVerbose;


	/**
	 * Empty constructor
	 */
	public ObjectAddressInitHandler(boolean _bVerbose) {

		mapFieldToValuesList = new HashMap<String, List<Object[]>>();
		bVerbose = _bVerbose;
		RuntimeOutput.print("ObjectAddressInitHandler: Creating a new ObjectAddressInitHandler object!", bVerbose);
	}

	/**
	 * Method addField()
	 * <p>
	 * Add a new field
	 *
	 * @param   strFieldAndObjectID  	String field name + object ID
	 * @param   arrFieldValues			Array field values object
	 * @return  void
	 */
	public void addField(String strFieldAndObjectID, Object[] arrFieldValues) {


		// Add a new list if this is a new field+object ID
		if (!mapFieldToValuesList.containsKey(strFieldAndObjectID)) {
			mapFieldToValuesList.put(strFieldAndObjectID, new ArrayList<Object[]>());
		}
		List<Object[]> listField = mapFieldToValuesList.get(strFieldAndObjectID);
		listField.add(arrFieldValues);
	}

	/**
	 * Method getField()
	 * <p>
	 * Get list of fields
	 *
	 * @param   strFieldAndObjectID  	String field name + object ID
	 * @return  void
	 */
	public List<Object[]> getFields(String strFieldAndObjectID) {

		return mapFieldToValuesList.get(strFieldAndObjectID);
	}

}
