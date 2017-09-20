import java.util.List;
import java.util.ArrayList;

public interface MoistureSensorCallback {
	public void newReadingAvailable(int sensorId, float moisture, long timeStampOfLastReading);
}
