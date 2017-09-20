package iotcode.LifxLightBulb;

import java.security.InvalidParameterException;

public class LifxHeader {
	// Frame Variables
	private int size;
	private int origin;
	private boolean tagged;
	private boolean addressable;
	private int protocol;
	private long source;

	//Frame Adress Variables
	private byte[] macAddress = new byte[8];
	private boolean ack_required;
	private boolean res_required;
	private int sequence;

	//Protocol Header
	private int type;

	public LifxHeader() {
		// needed values as per spec
		origin = 0;
		addressable = true;
		protocol = 1024;
	}

	public void setSize(int _size) {
		if (_size < 0) {
			throw new InvalidParameterException("Header: size cannot be less than 0");
		} else if (_size > 65535) {
			throw new InvalidParameterException("Header: size to large");
		}
		size = _size;
	}

	public void setOrigin(int _origin) {
		if (_origin < 0) {
			throw new InvalidParameterException("Header: origin cannot be less than 0");
		} else if (_origin > 3) {
			throw new InvalidParameterException("Header: origin to large");
		}

		origin = _origin;
	}

	public void setTagged(boolean _tagged) {
		tagged = _tagged;
	}

	public void setAddressable(boolean _addressable) {
		addressable = _addressable;
	}

	public void setProtocol(int _protocol) {
		if (_protocol < 0) {
			throw new InvalidParameterException("Header: protocol cannot be less than 0");
		} else if (_protocol > 4095) {
			throw new InvalidParameterException("Header: protocol to large");
		}

		protocol = _protocol;
	}

	public void setSource(long _source) {
		if (_source < 0) {
			throw new InvalidParameterException("Header: source cannot be less than 0");
		} else if (_source > (long)4294967295l) {
			throw new InvalidParameterException("Header: source to large");
		}
		source = _source;
	}

	public void setSequence(int _sequence) {
		if (_sequence < 0) {
			throw new InvalidParameterException("Header: sequence cannot be less than 0");
		} else if (_sequence > 255) {
			throw new InvalidParameterException("Header: sequence to large");
		}
		sequence = _sequence;
	}

	public void setType(int _type) {
		if (_type < 0) {
			throw new InvalidParameterException("Header: type cannot be less than 0");
		} else if (_type > 65535) {
			throw new InvalidParameterException("Header: type to large");
		}
		type = _type;
	}

	public void setAck_required(boolean _ack_required) {
		ack_required = _ack_required;
	}

	public void setRes_required(boolean _res_required) {
		res_required = _res_required;
	}

	public void setMacAddress(byte[] _macAddress) {
		macAddress = _macAddress;
	}

	public int getSize() {
		return size;
	}

	public int getOrigin() {
		return origin;
	}

	public boolean getTagged() {
		return tagged;
	}

	public boolean getAddressable() {
		return addressable;
	}

	public int getProtocol() {
		return protocol;
	}

	public long getSource() {
		return source;
	}

	public int getSequence() {
		return sequence;
	}

	public int getType() {
		return type;
	}

	public byte[] getMacAddress() {
		return macAddress;
	}

	public boolean getAck_required() {
		return ack_required;
	}

	public boolean getRes_required() {
		return res_required;
	}

	public byte[] getHeaderBytes() {
		byte[] headerBytes = new byte[36];
		headerBytes[0] = (byte)(size & 0xFF);
		headerBytes[1] = (byte)((size >> 8) & 0xFF);


		headerBytes[2] = (byte)(protocol & 0xFF);
		headerBytes[3] = (byte)((protocol >> 8) & 0x0F);

		headerBytes[3] |= (byte)((origin & 0x03) << 6);

		if (tagged) {
			headerBytes[3] |= (1 << 5);
		}

		if (addressable) {
			headerBytes[3] |= (1 << 4);
		}

		headerBytes[4] = (byte)((source >> 0) & 0xFF);
		headerBytes[5] = (byte)((source >> 8) & 0xFF);
		headerBytes[6] = (byte)((source >> 16) & 0xFF);
		headerBytes[7] = (byte)((source >> 24) & 0xFF);


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
		// headerBytes[16] = 0;
		// headerBytes[17] = 0;
		// headerBytes[18] = 0;
		// headerBytes[19] = 0;
		// headerBytes[20] = 0;
		// headerBytes[21] = 0;

		if (ack_required) {
			headerBytes[22] = (1 << 1);
		}

		if (res_required) {
			headerBytes[22] |= (1);
		}

		headerBytes[23] = (byte)(sequence & 0xFF);

		// Reserved and set to 0
		//headerBytes[24] = 0;
		//headerBytes[25] = 0;
		//headerBytes[26] = 0;
		//headerBytes[27] = 0;
		//headerBytes[28] = 0;
		//headerBytes[29] = 0;
		//headerBytes[30] = 0;
		//headerBytes[31] = 0;

		headerBytes[32] = (byte)((type >> 0) & 0xFF);
		headerBytes[33] = (byte)((type >> 8) & 0xFF);

		// Reserved and set to 0
		//headerBytes[34] = 0;
		//headerBytes[35] = 0;

		return headerBytes;
	}

	public void setFromBytes(byte[] dataBytes) {
		if (dataBytes.length != 36) {
			throw new InvalidParameterException("Header: invalid number of bytes");
		}

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
}
