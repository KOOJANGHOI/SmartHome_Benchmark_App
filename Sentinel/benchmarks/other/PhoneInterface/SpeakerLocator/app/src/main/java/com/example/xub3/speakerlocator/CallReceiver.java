package com.example.xub3.speakerlocator;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.io.*;


/**
 * Created by xub3 on 4/14/16.
 */
public class CallReceiver extends PhonecallReceiver {




	/*
	** Interrupt the playing music when phone is ringing
	*/
	@Override
	protected void onIncomingCallReceived(Context ctx, String number, Date start) {
		String driverIP = ctx.getResources().getString(R.string.gateway_ip);
		new MakeRequestTask(driverIP, true, "setRingStatus").start();
	}

	@Override
	protected void onIncomingCallAnswered(Context ctx, String number, Date start) {

	}
	/*
	** Resume the music after the phone call
	*/
	@Override
	protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
		String driverIP = ctx.getResources().getString(R.string.gateway_ip);
		new MakeRequestTask(driverIP, false, "setRingStatus").start();
	}
    /*
        ** Interrupt the music when starting an outgoing call
    */
	@Override
	protected void onOutgoingCallStarted(Context ctx, String number, Date start) {

        String driverIP = ctx.getResources().getString(R.string.gateway_ip);
        new MakeRequestTask(driverIP, true, "setRingStatus").start();
	}
    /*
        ** Resume the music when finishing calls
    */
	@Override
	protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        String driverIP = ctx.getResources().getString(R.string.gateway_ip);
		new MakeRequestTask(driverIP, false, "setRingStatus").start();
	}

	@Override
	protected void onMissedCall(Context ctx, String number, Date start) {
		String driverIP = ctx.getResources().getString(R.string.gateway_ip);
		new MakeRequestTask(driverIP, false, "setRingStatus").start();
	}


}
class MakeRequestTask implements Runnable {

	private Helper helper = MainActivity.helper;
	private String driverIP;
	private boolean status;
	private String methodName;
	private Thread t;
	MakeRequestTask(String ip, boolean ring, String name) {
		driverIP = ip;
		status = ring;
		methodName = name;
	}

	@Override
	public void run() {
		helper.makeRequest(driverIP, status, methodName);
	}
	public void start() {
		if (t == null) {
			t = new Thread(this, "makeRequest");
		}
		t.start();
	}
}
