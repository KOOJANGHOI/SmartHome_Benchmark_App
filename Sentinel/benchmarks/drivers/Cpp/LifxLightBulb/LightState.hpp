#ifndef _LIGHTSTATE_HPP__
#define _LIGHTSTATE_HPP__
#include <iostream>

#include "BulbColor.hpp"

class LightState {
	private:
		BulbColor* color;
		int power;
		string label;

	public:

		LightState(BulbColor* _color, int _power, string _label) {

			color = _color;
			power = _power;
			label = _label;
		}


		~LightState() {
		}


		BulbColor* getColor() {
			return color;
		}


		int getPower() {
			return power;
		}


		string getLabel() {
			return label;
		}
};
#endif
