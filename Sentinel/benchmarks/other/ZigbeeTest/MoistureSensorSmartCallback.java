import java.util.List;
import java.util.ArrayList;

public interface MoistureSensorSmartCallback {

	public void newReadingAvailable(int sensorId, float moisture, long timeStampOfLastReading);
}
