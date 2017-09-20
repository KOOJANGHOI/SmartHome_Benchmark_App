package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface GPSGateway {
	public void init();
	public void start();
	public void stop();
	public int getRoomID();
	public boolean getRingStatus();
	public void setNewRoomIDAvailable(boolean bValue);
	public void setNewRingStatusAvailable(boolean bValue);
	public void registerCallback(GPSGatewaySmartCallback _callbackTo);
}
