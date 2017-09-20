package iotcode.LifxLightBulb;

public class LightState {
	private final BulbColor color;
	private final int power;
	private final String label;

	public LightState(BulbColor _color, int _power, String _label) {
		color = _color;
		power = _power;
		label = _label;
	}

	public BulbColor getColor() {
		return color;
	}

	public int getPower() {
		return power;
	}

	public String getLabel() {
		return label;
	}
}
