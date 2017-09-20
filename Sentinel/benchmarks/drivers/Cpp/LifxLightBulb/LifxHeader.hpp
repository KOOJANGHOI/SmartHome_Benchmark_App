#ifndef _LIFXHEADER_HPP__
#define _LIFXHEADER_HPP__
#include <iostream>

class LifxHeader {

	private:
		// Frame variables
		int size;
		int origin;
		bool tagged;
		bool addressable;
		int protocol;
		int64_t source;

		// Frame address variables
		char macAddress[8];
		bool ack_required;
		bool res_required;
		int sequence;

		// Protocol header
		int type;

	public:

		LifxHeader() {
			origin = 0;
			addressable = true;
			protocol =1024;
		}


		~LifxHeader() {
		}


		void setSize(int _size) {
			if (_size < 0) {
				cerr << "Header: size cannot be less than 0" << endl;
				exit(1);
			} else if (_size > 65535) {
				cerr << "Header: size too large" << endl;
				exit(1);
			}
			size = _size;
		}


		void setOrigin(int _origin) {
			if (_origin < 0) {
				cerr << "Header: origin cannot be less than 0" << endl;
				exit(1);
			} else if (_origin > 3) {
				cerr << "Header: origin too large" << endl;
				exit(1);
			}
			origin = _origin;
		}


		void setTagged(bool _tagged) {
			tagged = _tagged;
		}


		void setAddressable(bool _addressable) {
			addressable = _addressable;
		}


		void setProtocol(int _protocol) {
			if (_protocol < 0) {
				cerr << "Header: protocol cannot be less than 0" << endl;
				exit(1);
			} else if (_protocol > 4095) {
				cerr << "Header: protocol too large" << endl;
				exit(1);
			}
			protocol = _protocol;
		}


		void setSource(int64_t _source) {
			if (_source < 0) {
				cerr << "Header: source cannot be less than 0" << endl;
				exit(1);
			} else if (_source > 4294967295) {
				cerr << "Header: source too large" << endl;
				exit(1);
			}
			source = _source;
		}


		void setSequence(int _sequence) {
			if (_sequence < 0) {
				cerr << "Header: sequence cannot be less than 0" << endl;
				exit(1);
			} else if (_sequence > 255) {
				cerr << "Header: sequence too large" << endl;
				exit(1);
			}
			sequence = _sequence;
		}


		void setType(int _type) {
			if (_type < 0) {
				cerr << "Header: type cannot be less than 0" << endl;
				exit(1);
			} else if (_type > 65535) {
				cerr << "Header: type too large" << endl;
				exit(1);
			}
			type = _type;
		}


		void setAck_required(bool _ack_required) {
			ack_required = _ack_required;
		}


		void setRes_required(bool _res_required) {
			res_required = _res_required;
		}


		void setMacAddress(char _macAddress[8]) {
			strcpy(macAddress, _macAddress);
		}


		int getSize() {
			return size;
		}


		int getOrigin() {
			return origin;
		}


		bool getTagged() {
			return tagged;
		}


		bool getAddressable() {
			return addressable;
		}


		int getProtocol() {
			return protocol;
		}


		int64_t getSource() {
			return source;
		}


		int64_t getSequence() {
			return sequence;
		}


		int getType() {
			return type;
		}


		char* getHeaderBytes(char headerBytes[36]) {

			//char headerBytes[36];
			headerBytes[0] = (char)(size & 0xFF);
			headerBytes[1] = (char)((size >> 8) & 0xFF);


			headerBytes[2] = (char)(protocol & 0xFF);
			headerBytes[3] = (char)((protocol >> 8) & 0x0F);

			headerBytes[3] |= (char)((origin & 0x03) << 6);

			if (tagged) {
				headerBytes[3] |= (1 << 5);
			}

			if (addressable) {
				headerBytes[3] |= (1 << 4);
			}

			headerBytes[4] = (char)((source >> 0) & 0xFF);
			headerBytes[5] = (char)((source >> 8) & 0xFF);
			headerBytes[6] = (char)((source >> 16) & 0xFF);
			headerBytes[7] = (char)((source >> 24) & 0xFF);

			// fix in a bit
			headerBytes[8] = macAddress[0];
			headerBytes[9] = macAddress[1];
			headerBytes[10] = macAddress[2];
			headerBytes[11] = macAddress[3];
			headerBytes[12] = macAddress[4];
			headerBytes[13] = macAddress[5];
			headerBytes[14] = macAddress[6];
			headerBytes[15] = macAddress[7];

			// Reserved and set to 0
			headerBytes[16] = 0;
			headerBytes[17] = 0;
			headerBytes[18] = 0;
			headerBytes[19] = 0;
			headerBytes[20] = 0;
			headerBytes[21] = 0;

			if (ack_required) {
				headerBytes[22] = (1 << 1);
			}

			if (res_required) {
				headerBytes[22] |= (1);
			}

			headerBytes[23] = (char)(sequence & 0xFF);

			// Reserved and set to 0
			headerBytes[24] = 0;
			headerBytes[25] = 0;
			headerBytes[26] = 0;
			headerBytes[27] = 0;
			headerBytes[28] = 0;
			headerBytes[29] = 0;
			headerBytes[30] = 0;
			headerBytes[31] = 0;

			headerBytes[32] = (char)((type >> 0) & 0xFF);
			headerBytes[33] = (char)((type >> 8) & 0xFF);

			// Reserved and set to 0
			headerBytes[34] = 0;
			headerBytes[35] = 0;

			return headerBytes;
		}


	void setFromBytes(char dataBytes[36]) {

		size = dataBytes[0] & 0xFF;
		size |= ((dataBytes[1] & 0xFF) << 8);
		size &= 0xFFFF;

		origin = (dataBytes[3] >> 6) & 0x03;
		tagged = ((dataBytes[3] >> 5) & 0x01) == 1;
		addressable = ((dataBytes[3] >> 4) & 0x01) == 1;


		protocol = (dataBytes[3] & 0x0F) << 8;
		protocol |= dataBytes[2];
		protocol &= 0x0FFF;

		source = (dataBytes[7] & 0xFFl) << 24;
		source |= ((dataBytes[6] & 0xFFl) << 16);
		source |= ((dataBytes[5] & 0xFFl) << 8);
		source |= ((dataBytes[4] & 0xFFl));

		macAddress[0] = dataBytes[8];
		macAddress[1] = dataBytes[9];
		macAddress[2] = dataBytes[10];
		macAddress[3] = dataBytes[11];
		macAddress[4] = dataBytes[12];
		macAddress[5] = dataBytes[13];
		macAddress[6] = dataBytes[14];
		macAddress[7] = dataBytes[15];

		ack_required = (dataBytes[22] & 0x02) == 0x02;
		res_required = (dataBytes[22] & 0x01) == 0x01;

		sequence = (dataBytes[23] & 0xFF);

		type = ((dataBytes[33] & 0xFF) << 8);
		type |= (dataBytes[32] & 0xFF);	
	}
};
#endif
