
import java.util.List;

/** Zigbee Message Zcl Read Attributes Response.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-19
 */
public class IoTZigbeeMessageZclReadAttributesResponse extends IoTZigbeeMessage {

	static class Attribute {

		// private variables
		private int attributeId;
		private int dataType;
		private boolean successOrFail;
		private byte[] data;

		/**
		 * Constructor
		 */
		public Attribute(int _attributeId, int _dataType, boolean _successOrFail, byte[] _data) {
			attributeId = _attributeId;
			dataType = _dataType;
			successOrFail = _successOrFail;
			data = _data;
		}


		/**
		 * getAttributeId() method that returns attribute id
		 *
		 * @return int
		 */
		public int getAttributeId() {
			return attributeId;
		}


		/**
		 * getDataType() method that returns attribute data type
		 *
		 * @return int
		 */
		public int getDataType() {
			return dataType;
		}


		/**
		 * getSuccessOrFail() method is if the configure for this attribute failed or succeeded
		 *
		 * @return boolean
		 */
		public boolean getSuccessOrFail() {
			return successOrFail;
		}


		/**
		 * getData() method that returns attribute data
		 *
		 * @return byte[]
		 */
		public byte[] getData() {
			return data;
		}
	}

	// private variables
	private int clusterId;
	private int profileId;
	private List <Attribute> attributes;

	/**
	 * Constructor
	 */
	public IoTZigbeeMessageZclReadAttributesResponse(int _packetId, int _clusterId, int _profileId, List <Attribute> _attributes) {
		super(_packetId);

		clusterId = _clusterId;
		profileId = _profileId;
		attributes = _attributes;
	}

	/**
	 * getClusterId() method that returns the cluster id
	 *
	 * @return int
	 */
	public int getClusterId() {
		return clusterId;
	}

	/**
	 * getProfileId() method that returns the profile id
	 *
	 * @return int
	 */
	public int getProfileId() {
		return profileId;
	}

	/**
	 * getAttributes() method that returns all attributes data
	 *
	 * @return List <Attribute>
	 */
	public List <Attribute> getAttributes() {
		return attributes;
	}
}
