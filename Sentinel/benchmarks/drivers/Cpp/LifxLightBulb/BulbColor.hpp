#ifndef _BULBCOLOR_HPP__
#define _BULBCOLOR_HPP__
#include <iostream>

class BulbColor {

	private:
		int hue;
		int saturation;
		int brightness;
		int kelvin;

	public:

		BulbColor(int _hue, int _saturation, int _brightness, int _kelvin) {

			if ((_hue > 65535) || (_hue < 0)) {
				cerr << "BulbColor: Invalid parameter value for _hue (0-65535)" << endl;
				exit(1);
			}

			if ((_saturation > 65535) || (_saturation < 0)) {
				cerr << "BulbColor: Invalid parameter value for _saturation (0-65535)" << endl;
				exit(1);
			}

			if ((_brightness > 65535) || (_brightness < 0)) {
				cerr << "BulbColor: Invalid parameter value for _brightness (0-65535)" << endl;
				exit(1);
			}

			if ((_kelvin > 65535) || (_kelvin < 0)) {
				cerr << "BulbColor: Invalid parameter value for _kelvin (0-65535)" << endl;
				exit(1);
			}

			hue = _hue;
			saturation = _saturation;
			brightness = _brightness;
			kelvin = _kelvin;
		}


		BulbColor(char* data) {
			hue = ((data[1] & 0xFF) << 8);
			hue |= (data[0] & 0xFF);

			saturation = ((data[3] & 0xFF) << 8);
			saturation |= (data[2] & 0xFF);

			brightness = ((data[5] & 0xFF) << 8);
			brightness |= (data[4] & 0xFF);

			kelvin = ((data[7] & 0xFF) << 8);
			kelvin |= (data[6] & 0xFF);
		}


		~BulbColor() {
		}


		int getHue() {
			return hue;
		}


		int getSaturation() {
			return saturation;
		}


		int getBrightness() {
			return brightness;
		}


		int getKelvin() {
			return kelvin;
		}
};
#endif
