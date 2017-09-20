package iotruntime.messages;

/** Class IoTCommCode is a place to keep all the necessary
 *  enumerations for communication
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-19
 */

// Enumeration of master-slave communication codes
public enum IoTCommCode {

	ACKNOWLEDGED,
	CREATE_DRIVER_OBJECT,
	CREATE_OBJECT,
	CREATE_MAIN_OBJECT,
	CREATE_NEW_IOTSET,
	CREATE_NEW_IOTRELATION,
	END_TRANSFER,
	END_SESSION,
	GET_ADD_IOTSET_OBJECT,
	GET_DEVICE_IOTSET_OBJECT,
	GET_IOTSET_OBJECT,
	GET_IOTRELATION_FIRST_OBJECT,
	GET_IOTRELATION_SECOND_OBJECT,
	GET_ZB_DEV_IOTSET_OBJECT,
	INVOKE_INIT_METHOD,
	REINITIALIZE_IOTSET_FIELD,
	REINITIALIZE_IOTRELATION_FIELD,
	TRANSFER_FILE,
}

