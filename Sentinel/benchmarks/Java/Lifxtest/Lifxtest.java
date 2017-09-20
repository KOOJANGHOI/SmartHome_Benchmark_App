package Lifxtest;

import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTRelation;
import iotruntime.slave.IoTDeviceAddress;
import iotruntime.IoTUDP;

import java.io.IOException;
import java.net.*;
import iotcode.interfaces.LightBulbTest;
import iotcode.annotation.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import iotcode.interfaces.LightBulbTest;

public class Lifxtest {

	@config private IoTSet<LightBulbTest> lifx_light_bulb;

	public void init() throws InterruptedException {

		for(LightBulbTest lifx : lifx_light_bulb.values()) {
			//Thread thread = new Thread(new Runnable() {
			//	public void run() {
					lifx.init();
			//	}
			//});
			//thread.start();
			Thread.sleep(1000);

			for (int i = 0; i < 5; i++) {
				lifx.turnOff();
				System.out.println("Turning off!");
				Thread.sleep(1000);
				lifx.turnOn();
				System.out.println("Turning on!");
				Thread.sleep(1000);
			}


			for (int i = 2500; i < 9000; i += 100) {
				System.out.println("Adjusting Temp: " + Integer.toString(i));
				lifx.setTemperature(i);
				Thread.sleep(100);
			}

			for (int i = 9000; i > 2500; i -= 100) {
				System.out.println("Adjusting Temp: " + Integer.toString(i));
				lifx.setTemperature(i);
				Thread.sleep(100);
			}

			for (int i = 100; i > 0; i -= 10) {
				System.out.println("Adjusting Brightness: " + Integer.toString(i));
				lifx.setColor(lifx.getHue(), lifx.getSaturation(), i);
				Thread.sleep(500);
			}

			for (int i = 0; i < 100; i += 10) {
				System.out.println("Adjusting Brightness: " + Integer.toString(i));
				lifx.setColor(lifx.getHue(), lifx.getSaturation(), i);
				Thread.sleep(500);
			}

			//thread.join();
		}
	}
}
