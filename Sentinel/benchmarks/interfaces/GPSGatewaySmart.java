package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface GPSGatewaySmart {

	public void setNewRoomIDAvailable(boolean bValue);
	public void setNewRingStatusAvailable(boolean bValue);
	public void stop();
	public void start();
	public void init();
	public int getRoomID();
	public void registerCallback(GPSGatewayCallback _callbackTo);
	public boolean getRingStatus();
}
