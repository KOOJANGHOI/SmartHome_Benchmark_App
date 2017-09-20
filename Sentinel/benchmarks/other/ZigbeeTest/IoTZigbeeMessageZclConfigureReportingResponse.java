
import java.util.List;

/** Zigbee Message Zcl Configure Reporting Response.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-19
 */
public class IoTZigbeeMessageZclConfigureReportingResponse extends IoTZigbeeMessage {

	static public class Attribute {

		// private variables
		private int attributeId;
		private boolean successOrFail;
		private boolean isReport;

		/**
		 * Constructor
		 */
		public Attribute(int _attributeId, boolean _successOrFail, boolean _isReport) {
			attributeId = _attributeId;
			successOrFail = _successOrFail;
			isReport = _isReport;
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
		 * getIsReport() method that gets if the direction is report of receive
		 *
		 * @return boolean
		 */
		public boolean getIsReport() {
			return isReport;
		}

		/**
		 * getSuccessOrFail() method is if the configure for this attribute failed or succeeded
		 *
		 * @return boolean
		 */
		public boolean getSuccessOrFail() {
			return successOrFail;
		}
	}

	// private variables
	private int clusterId;
	private int profileId;
	private boolean allSuccess;
	private List <Attribute> attributes;

	/**
	 * Constructor
	 */
	public IoTZigbeeMessageZclConfigureReportingResponse(int _packetId, int _clusterId, int _profileId, boolean _allSuccess, List <Attribute> _attributes) {
		super(_packetId);

		clusterId = _clusterId;
		profileId = _profileId;
		allSuccess = _allSuccess;
		attributes = _attributes;
	}

	/**
	 * getAllSuccess() method that returns if all the configurations succeeded
	 *
	 * @return boolean
	 */
	public boolean getAllSuccess() {
		return allSuccess;
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
	 * getAttributes() method that returns if all attributes if one of there was a failure to configure
	 *
	 * @return List <Attribute>
	 */
	public List <Attribute> getAttributes() {
		return attributes;
	}
}
