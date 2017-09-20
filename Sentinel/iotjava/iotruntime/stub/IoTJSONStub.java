package iotruntime.stub;

import iotruntime.slave.IoTDeviceAddress;

/** IoTJSONStub abstract class that all the stubs are going
 *  to implement
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-20
 */
public abstract class IoTJSONStub {

	protected IoTDeviceAddress iotDevAddress;

	/**
	 * Class constructor
	 *
	 * @param   iotdevAddress	IoTDeviceAddress object
	 */
	public IoTJSONStub(IoTDeviceAddress _iotDevAddress) {

		this.iotDevAddress = _iotDevAddress;
	}

	public abstract void registerCallback(Object objCallback);
}

