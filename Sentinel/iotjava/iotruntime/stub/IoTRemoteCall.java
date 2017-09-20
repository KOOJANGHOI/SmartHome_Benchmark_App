package iotruntime.stub;

// Java libraries
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.lang.Class;
import java.lang.reflect.*;

// Java JSON - from Maven
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Arrays;


/** IoTRemoteCall class that takes JSON packets and instrument
 *  interfaces used in the code via reflection
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-14
 */
public class IoTRemoteCall {

	/**
	 * IoTRemoteCall class properties
	 */
	final private Class _interface;
	final private Object _callback;
	final private int iPort;
	final private String strAddress;
	private static final Logger logger = Logger.getLogger(IoTRemoteCall.class.getName());

	/**
	 * IoTRemoteCall class constants
	 */
	private final String USER_AGENT = "Mozilla/5.0";
	private final String PASSWORD = "password";
	private final String KEYEXT = ".jks";
	private final String KEYTYPE = "SunX509";
	private final String KEYINSTANCE = "JKS";

	/**
	 * Constructor
	 */
	public IoTRemoteCall(Class _interface, Object _callback, int _iPort, String _strAddress) {

		this._interface = _interface;
		this._callback = _callback;
		this.iPort = _iPort;
		this.strAddress = _strAddress;
		startHttpsServer();
	}

	/**
	 * Get Objects from a HTTP request
	 */
	private void startHttpsServer() {
		// Run a separate thread as the HTTP server
		IncomingMessageHandler imh=new IncomingMessageHandler(_interface, _callback);
    
		try {
			HttpsServer server = HttpsServer.create(new InetSocketAddress(iPort), 0);
			SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] password = PASSWORD.toCharArray();
            KeyStore ks = KeyStore.getInstance(KEYINSTANCE);
            FileInputStream fis = new FileInputStream(strAddress + KEYEXT);
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEYTYPE);
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KEYTYPE);
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

			// Context name is according to method name, e.g. getRingStatus
			Class<?> inter=_interface;
			for (Method m:inter.getDeclaredMethods()) {
				server.createContext("/" + m.getName(), imh);
			}
			server.setExecutor(null); // creates a default executor
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * HTTP server handler
	 */	
	class IncomingMessageHandler implements HttpHandler {
		Class _interface;
		Object _callback;
    
		public IncomingMessageHandler(Class _interface, Object _callback) {
			this._interface=_interface;
			this._callback=_callback;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			BufferedReader brIn = new BufferedReader(new InputStreamReader(t.getRequestBody(), "utf-8"));
      
			String uri = t.getRequestURI().getPath();
			String requestMethod = t.getRequestMethod();
			StringBuffer sbResponse=null;
			if (requestMethod.equalsIgnoreCase("POST")) {
				try {
					String strInputLine;
					sbResponse = new StringBuffer();
					while ((strInputLine = brIn.readLine()) != null) {
						sbResponse.append(strInputLine);
					}
					brIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
      		System.out.println(uri);
			try {
				String strJSONString = sbResponse.toString();
				System.out.println(strJSONString);
				Class[][] cr = new Class[1][];
				Object[] params = decodeJSONArray(strJSONString,cr);

				Class<?> c_intrf = _interface;
				Class[] c_params = cr[0];
      
				Method m = c_intrf.getMethod(uri.substring(1), c_params);
				Object response = m.invoke(_callback, params);
				JSONObject json_r = encodeObject(response);
      
				// Write a response
				String strResponse = json_r.toString();
				t.sendResponseHeaders(200, strResponse.length());
				OutputStream os = t.getResponseBody();
				os.write(strResponse.getBytes());
				os.close();
			} catch (Exception e) {
				 e.printStackTrace();
				logger.log(Level.WARNING, "Exception occur", e.getMessage());
			}
		}
	}

	/** ==========================
	 * Helper functions
	 *  ==========================/

	/**
	 * Encode JSON String
	 */
	private static String encodeJSONArray(Object[] array) {

		try {
			// Prepare JSON String as a JSON Object
			JSONObject jsonObj = new JSONObject();
			JSONArray jsonArr=new JSONArray();
			jsonObj.put("params", jsonArr);
      
			// Get the method name and get the array of parameters
			for(int i=0;i<array.length;i++) {
				JSONObject obj=encodeObject(array[i]);
				jsonArr.put(i, obj);
			}
			return jsonObj.toString();

		} catch (JSONException ex) {
			ex.printStackTrace();
			//throw new Error("IoTRemoteCall: Exiting");
			logger.log(Level.WARNING, "package format error", ex.getMessage());
			return null;
		}
	}

	/**
	 * Decode JSON String
	 */
	private static Object[] decodeJSONArray(String jsonstring, Class[][] carr) {
		try {
			// Prepare JSON String as a JSON Object
			JSONObject jsonObj = new JSONObject(jsonstring);
			JSONArray jsonArr = jsonObj.getJSONArray("params");
			Object[] retval = new Object[jsonArr.length()];
			if (carr != null)
				carr[0] = new Class[retval.length];
      
			// Get the method name and get the array of parameters
			for(int i=0;i<jsonArr.length();i++) {
				JSONObject obj = jsonArr.getJSONObject(i);
				Class rc[] = new Class[1];
				retval[i]=decodeObject(obj,rc);
				if (carr!=null)
					carr[0][i]=rc[0];
			}
			return retval;
		} catch (JSONException ex) {
			ex.printStackTrace();
			//throw new Error("IoTRemoteCall: Exiting");
			logger.log(Level.WARNING, "package format error", ex.getMessage());
			return null;
		}
	}

	/**
	 * Encode object to JSON
	 */
	private static JSONObject encodeObject(Object o) {

		try {
			if (o instanceof String	||
				o instanceof Boolean||
				o instanceof Integer||
				o instanceof Long 	||
				o instanceof Double ||
				o instanceof Byte	||
				o instanceof Float 	||
				o instanceof Short 	||
				o instanceof Character) {

				JSONObject jo = new JSONObject();
				Class<?> cl = o.getClass();
				jo.put("type",cl.getName());
				jo.put("value",o);
				return jo;
			}
			JSONObject jo = new JSONObject();
			Class<?> cl = o.getClass();
			jo.put("type", cl.getName());

			JSONArray ja = new JSONArray();
			jo.put("fields",ja);
      
			Field[] fields = cl.getFields();
			for(int i=0;i<fields.length;i++) {
				Field f = fields[i];
				Object fo = f.get(o);

				JSONObject jfo = new JSONObject();
				ja.put(i, jfo);
				jfo.put("name", f.getName());
				jfo.put("value", encodeObject(fo));
			}
			return jo;
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "package format errors", e.getMessage());
			return null;
			//throw new Error("IoTRemoteCall: Exiting");
		}
	}

	/**
	 * Decode object from JSON
	 */
  private static Object decodeObject(JSONObject jsonObj, Class[] tarr) {

		try {
			String type = jsonObj.getString("type");
			if (type.equals("java.lang.Integer")	||
				type.equals("java.lang.Boolean")	||
				type.equals("java.lang.Long")		||
				type.equals("java.lang.Character")	||
				type.equals("java.lang.String")		||
				type.equals("java.lang.Float")		||
				type.equals("java.lang.Double")		||
				type.equals("java.lang.Byte")		||
				type.equals("java.lang.Short")) {

				Class<?> c_type=Class.forName(type);
				if (tarr != null)
					tarr[0] = c_type;
				// TODO: Find a better JSON package later and remove this handler
				// There is a stupid problem with JSON that it strips off the decimal part
				// of the JSON object with the type double when we invoke JSONObject.toString()
				if (type.equals("java.lang.Float") || type.equals("java.lang.Double")) {
					Double temp = Double.parseDouble(jsonObj.get("value").toString());
					return temp;
				} else {
					return jsonObj.get("value");
				}
			} else if (type.equals("int")) {
				if (tarr != null)
					tarr[0] = int.class;
				return jsonObj.get("value");
			} else if (type.equals("long")) {
				if (tarr != null)
					tarr[0] = long.class;
				return jsonObj.get("value");
			} else if (type.equals("short")) {
				if (tarr != null)
					tarr[0] = short.class;
				return jsonObj.get("value");
			} else if (type.equals("char")) {
				if (tarr != null)
					tarr[0] = char.class;
				return jsonObj.get("value");
			} else if (type.equals("byte")) {
				if (tarr != null)
					tarr[0] = byte.class;
				return jsonObj.get("value");
			} else if (type.equals("boolean")) {
				if (tarr != null)
					tarr[0] = boolean.class;
				return jsonObj.get("value");
			} else if (type.equals("double")) {
				if (tarr != null)
					tarr[0] = double.class;
				return jsonObj.get("value");
			} else if (type.equals("float")) {
				if (tarr != null)
					tarr[0] = float.class;
				return jsonObj.get("value");
			}
      
			Class<?> c_type = Class.forName(type);
			if (tarr != null)
				tarr[0] = c_type;
			Object o = c_type.newInstance();
			JSONArray arr = jsonObj.getJSONArray("fields");
			for(int i=0;i<arr.length();i++) {
				JSONObject fld = arr.getJSONObject(i);
				String field = fld.getString("name");
				JSONObject obj = fld.getJSONObject("value");
				Object fldo = decodeObject(obj,null);
				Field fobj = c_type.getDeclaredField(field);
				fobj.set(o, fldo);
			}
			return o;
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.WARNING, "package format error", e.getMessage());
			return null;
			//throw new Error("IoTRemoteCall: Exiting");
		}
	}
  
	interface foo {
		int add(int a, int b);
		int setRoomID(Integer id);
		boolean getRingStatus(Boolean status);
		String  getIrrigationInfo(Double inchesPerWeek, Integer weatherZipCode, 
			Integer daysToWaterOn, Double inchesPerMinute);
	}

	static class Fooimpl implements foo {

		int iRoomID;
		boolean bRing;
		int A;
		int B;
		double inchesPerWeek;
		int weatherZipCode;
		int daysToWaterOn;
		double inchesPerMinute;

		public Fooimpl() {
			iRoomID = 0;
			bRing = false;
			A = 0;
			B = 0;
		}

		public int add(int a, int b) {
			System.out.println("a="+a+" b="+b);
			A = a;
			B = b;
			System.out.println("A: " + getA());
			System.out.println("B: " + getB());
			return a+b;
		}
		public int setRoomID(Integer id) {
			System.out.println("Phone in room : " + id);
			iRoomID = id;
			return id;
		}
		public boolean getRingStatus(Boolean status) {
			System.out.println("Phone rings? " + status);
			bRing = status;
			return status;
		}
		public String getIrrigationInfo(Double inchesPerWeek, Integer weatherZipCode,
			               Integer daysToWaterOn, Double inchesPerMinute) {
			this.inchesPerWeek = inchesPerWeek;
			this.weatherZipCode = weatherZipCode;
			this.daysToWaterOn = daysToWaterOn;
			this.inchesPerMinute = inchesPerMinute;
			System.out.println("get Info");
			return "info sent";
		}	
		public void printIDStatus() {
			System.out.println("Phone in room : " + iRoomID);
			System.out.println("Phone rings? " + bRing);
			System.out.println("A: " + A);
			System.out.println("B: " + B);
		}
		public boolean getStatus() {
			return bRing;
		}
		public int getA() {
			return A;
		}
		public int getB() {
			return B;
		}
	}

  
  
	public static void main(String[] args) throws Exception {

    	Fooimpl fooimp = new Fooimpl();
		//IoTRemoteCall iotremcall = new IoTRemoteCall(foo.class, new Fooimpl(), 8000);
		new Thread() {
			public void run() {
				IoTRemoteCall iotremcall = new IoTRemoteCall(foo.class, fooimp, 8000, "192.168.2.244");	
			}
		}.start();
		System.out.println("server has started!");

		//while(true) {
		//	if (fooimp.getA() > 0) {
		//		fooimp.printIDStatus();
		//	} else {
		//		System.out.println("No change!");
		//		Thread.sleep(10000);
		//	}
		//}

	}
}
