package iotcode.interfaces;

public interface CameraSmartCallback {

	public void newCameraFrameAvailable(byte latestFrame[], long timeStamp);
	//public void newCameraFrameAvailable(Camera _camera);
}
