
/** Class IoTZigbeeAddress is a wrapper class to pass
 *  IoTSet of device addresses from master to slave
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-12
 */
public class IoTZigbeeAddress {

	/**
	 * IoTZigbeeAddress class properties
	 */
	private final String zigbeeAddress;
	private final byte[] zigbeeAddressByteArray;

	/**
	 * Class constructor
	 *
	 * @param   zAddress  Zigbee long address
	 */
	public IoTZigbeeAddress(String zAddress) {
		zigbeeAddress = zAddress;
		// convert to byte array
		zigbeeAddressByteArray = new byte[zAddress.length() / 2];
		for (int i = 0; i < zAddress.length(); i += 2) {
			zigbeeAddressByteArray[i / 2] = (byte) ((Character.digit(zAddress.charAt(i), 16) << 4)
			+ Character.digit(zAddress.charAt(i + 1), 16));
		}
	}

	/**
	 * getAddress() method that returns the zigbee address as a human readable String
	 *
	 * @return String
	 */
	public String getAddress() {
		return zigbeeAddress;
	}

	/**
	 * getAddressBytes() method that returns the zigbee address as a byte array
	 *
	 * @return byte[]
	 */
	public byte[] getAddressBytes() {
		return zigbeeAddressByteArray;
	}
}
