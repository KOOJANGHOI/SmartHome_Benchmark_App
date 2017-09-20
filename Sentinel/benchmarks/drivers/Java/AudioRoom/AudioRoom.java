package iotcode.AudioRoom;

import iotcode.interfaces.Room;

/** AudioRoom holds room ID that tells which room it is
 *  in association with speakers
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-29
 */
public class AudioRoom implements Room {

	/**
	 *  AudioRoom class properties
	 */
	private int iRoomID;

	public AudioRoom(int _iRoomID) {
		this.iRoomID = _iRoomID;
		System.out.println("AudioRoom ID: " + this.iRoomID);
	}

	public int getRoomID() {
		return this.iRoomID;
	}
}
