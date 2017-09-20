package iotinstaller;

/** A class that construct table properties (field, data type, and length)
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */

public final class TableProperty {

	/**
	 * TableProperty properties
	 */
	private String strField;
	private String strType;
	private String strLength;

	public TableProperty() {
		strField = "";
		strType = "";
		strLength = "";
	}

	public void setField(String str) {
		strField = str;
	}

	public void setType(String str) {
		strType = str;
	}

	public void setLength(String str) {
		strLength = str;
	}

	public String getField() {
		return strField;
	}

	public String getType() {
		return strType;
	}

	public String getLength() {
		return strLength;
	}
}
