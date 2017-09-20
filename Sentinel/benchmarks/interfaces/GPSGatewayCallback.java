package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface GPSGatewayCallback {
	public void newRoomIDRetrieved(int _roomIdentifier);
	public void newRingStatusRetrieved(boolean _ringStatus);
}
