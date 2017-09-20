package com.example.xub3.speakerlocator;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;


public class MainActivity extends AppCompatActivity implements PositionListener {

	static Helper helper = new Helper();
	private TextView tv;
	private Button button;
	private PositionManager positionManager;
	private String curr_Loc = "";
	private static String roomIDbuffer = "0";
	protected static Context context;
	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//System.out.println("Status: " + Environment.getExternalStorageState());
		initializePositioning();
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		tv = (TextView) findViewById(R.id.hellotext);
		if (helper.httpclient == null) {
			helper.setConnection(getApplicationContext().getResources().getString(R.string.gateway_ip));
		}
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});


		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void initializePositioning() {

		int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					this,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
		File file = new File(Environment.getExternalStorageDirectory(), "positioningPersistence.xml");
		try {
			positionManager = new PositionManager(file);
			Log.d("positionManager", "initialized");
			System.out.println("PositionManager: Initialized successfully!");
		} catch (PositioningPersistenceException e) {
			e.printStackTrace();
		}

		List<String> keyWhiteList = new ArrayList<String>();
		keyWhiteList.add(getApplicationContext().getResources().getString(R.string.mac_1).toLowerCase());
		keyWhiteList.add(getApplicationContext().getResources().getString(R.string.mac_2).toLowerCase());
		keyWhiteList.add(getApplicationContext().getResources().getString(R.string.mac_3).toLowerCase());
		keyWhiteList.add(getApplicationContext().getResources().getString(R.string.mac_4).toLowerCase());
		keyWhiteList.add(getApplicationContext().getResources().getString(R.string.mac_5).toLowerCase());
		keyWhiteList.add(getApplicationContext().getResources().getString(R.string.mac_6).toLowerCase());

		Technology wifiTechnology = new WifiTechnology(this, "WIFI", keyWhiteList);
		CompassTechnology compassTechnology = new CompassTechnology(this, "compass", 80
		);
		try {
			//positionManager.addTechnology(compassTechnology);
			positionManager.addTechnology(wifiTechnology);
		} catch (PositioningException e) {
			e.printStackTrace();
		}
		positionManager.registerPositionListener(this);
		final EditText mapName = (EditText) findViewById(R.id.mapname_et);
		Button mapBtn = (Button) findViewById(R.id.map_btn);
		Button startBtn = (Button) findViewById(R.id.start_btn);
		Button stopBtn = (Button) findViewById(R.id.stop_btn);

		mapBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				positionManager.map(mapName.getText().toString());
			}
		});
		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				positionManager.startPositioning(2000);
			}
		});
		stopBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				positionManager.stopPositioning();
			}
		});
	}

	@Override
	public void positionReceived(final PositionInformation positionInformation) {

		System.out.println("This is called! Single!");
		String positioningText = positionInformation.getName();
		if (!positioningText.equals(curr_Loc)) {
			final String room_id;
			// we need to handle a situation where
			// positionInformation.getName() returns ""
			if (positionInformation.getName().equals("")) {
				room_id = roomIDbuffer;
			} else {
				room_id = positionInformation.getName();
				roomIDbuffer = room_id;
			}
			new Thread() {
				public void run() {
					String driver_IP = getApplicationContext().getResources().getString(R.string.gateway_ip);
					helper.makeRequest(driver_IP, Integer.parseInt(room_id), "setRoomID");
					curr_Loc = room_id;
					System.out.println("room changed to " + room_id);
				}
			}.start();
			curr_Loc = positioningText;
		}


		// Do nothing
	}

	@Override
	public void positionReceived(final List<PositionInformation> positionInformation) {

		System.out.println("This is called! List!");
		String positioningText = "";
		int count = 0;
		for (int i = 0; i < positionInformation.size(); i++) {
			if (!positionInformation.get(i).getName().equals(curr_Loc)) {
				count += 1;
				if (count > positionInformation.size() / 2) {
					final String room_id;
					// we need to handle a situation where
					// positionInformation.getName() returns ""
					if (positionInformation.get(i).getName().equals("")) {
						room_id = roomIDbuffer;
					} else {
						room_id = positionInformation.get(i).getName();
						roomIDbuffer = room_id;
					}
					new Thread() {
						public void run() {
							String driver_IP = getApplicationContext().getResources().getString(R.string.gateway_ip);
							helper.makeRequest(driver_IP, Integer.parseInt(room_id), "setRoomID");
							curr_Loc = room_id;
							System.out.println("room changed to " + room_id);
						}
					}.start();
					positioningText = curr_Loc;
					break;
				}
			}
		}


	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.example.xub3.speakerlocator/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.example.xub3.speakerlocator/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}
}

