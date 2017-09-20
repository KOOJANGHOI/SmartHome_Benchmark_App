package iotcode.interfaces;

public interface CameraCallback {
	public void newCameraFrameAvailable(byte latestFrame[], long timeStamp);
	//public void newCameraFrameAvailable(CameraSmart _camera);
}
