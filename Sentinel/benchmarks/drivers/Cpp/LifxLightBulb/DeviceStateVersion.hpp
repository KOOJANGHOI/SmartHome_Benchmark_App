#ifndef _DEVICESTATEVERSION_HPP__
#define _DEVICESTATEVERSION_HPP__
#include <iostream>

class DeviceStateVersion {
	private:
		int64_t vender;
		int64_t product;
		int64_t version;

	public:

		DeviceStateVersion(int64_t _vender, int64_t _product, int64_t _version) {

			vender = _vender;
			product = _product;
			version = _version;
		}


		~DeviceStateVersion() {
		}


		int64_t getVender() {
			return vender;
		}


		int64_t getProduct() {
			return product;
		}


		int64_t getVersion() {
			return version;
		}
};
#endif
