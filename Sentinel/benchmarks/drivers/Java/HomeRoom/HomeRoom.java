package iotcode.HomeRoom;

import iotcode.interfaces.Room;

/** AudioRoom holds room ID that tells which room it is
 *  in association with speakers
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-29
 */
public class HomeRoom implements Room {

	/**
	 *  AudioRoom class properties
	 */
	private int iRoomID;

	public HomeRoom(int _iRoomID) {
		this.iRoomID = _iRoomID;
		System.out.println("AudioRoom ID: " + this.iRoomID);
	}

	public int getRoomID() {
		return this.iRoomID;
	}
}
