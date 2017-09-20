/** Zigbee Message Zcl Change Switch Response
 *
 * @author      Yuting Tan <ytan5 @ uci.edu>
 * @version     1.0
 * @since       2017-2-28
 */
public class IoTZigbeeMessageZclChangeSwitchResponse extends IoTZigbeeMessage {

	private boolean SuccessOrFail=false;
	private int clusterId;
	private int profileId;
	private int status;

	public IoTZigbeeMessageZclChangeSwitchResponse(int _packetId, int _clusterId, int _profileId, int _status, boolean _SuccessOrFail){
		super(_packetId);

		clusterId = _clusterId;
		profileId = _profileId;
		status = _status;
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

	public int getStatus(){
		return status;
	}
}
