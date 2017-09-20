/** Zigbee Message Zcl Write Attributes Response.
 *
 * @author      changwoo Lee <changwl2 @ uci.edu>
 * @version     1.0
 * @since       2016-10-18
 */
public class IoTZigbeeMessageZclWriteAttributesResponse extends IoTZigbeeMessage {

	private boolean SuccessOrFail=false;
	private int clusterId;
	private int profileId;

	public IoTZigbeeMessageZclWriteAttributesResponse(int _packetId, int _clusterId, int _profileId, boolean _SuccessOrFail){
		super(_packetId);

		clusterId = _clusterId;
		profileId = _profileId;
		SuccessOrFail = _SuccessOrFail;
	}
	public boolean getSuccessOrFail(){
		return SuccessOrFail;
	}

	public int getClusterId() {
		return clusterId;
	}

	public int getProfileId() {
		return profileId;
	}
}
