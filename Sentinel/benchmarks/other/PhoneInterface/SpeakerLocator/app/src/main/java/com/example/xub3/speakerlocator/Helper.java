package com.example.xub3.speakerlocator;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


/**
 * Created by xub3 on 4/14/16.
 */
public class Helper {
	private static final int Driver_port = 8000;
	private static final String Tag = "CallReceiver";
	private static final String KEYEXT = ".pem";
	HttpClient httpclient;
	//Set up
	//Set up
	void setConnection(String destIP) {

		httpclient = createClient(destIP);
	}

	HttpClient createClient(String destIP) {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.DEFAULT_CONTENT_CHARSET);
		params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30 * 1000);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30 * 1000);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), Driver_port));
		schReg.register(new Scheme("https", newSslSocketFactory(destIP), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

		return new DefaultHttpClient(conMgr, params);
	}

	private SSLSocketFactory newSslSocketFactory(String destIP) {
		try {
			// Load CAs from an InputStream
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream caInput = new
					BufferedInputStream(MainActivity.context.getAssets().open(destIP + KEYEXT));
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);
				System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
			} finally {
				caInput.close();
			}
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
			SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
			return socketFactory;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	//Make http request
	public void  makeRequest(String destIP, Object contentStr, String methodName) {
		String url = "https://"+ destIP+":" + Driver_port + "/"+methodName;
		System.out.println("URL: " + url);

		InputStream inputStream = null;
		String result = "";
			StringBuilder sb = new StringBuilder();
			try {
				HttpPost httpPost = new HttpPost(url);
				JSONArray params = new JSONArray();

				JSONObject content = new JSONObject();
				JSONObject parent = new JSONObject();
				content.put("type", contentStr.getClass().getName());
				content.put("value", contentStr);
				params.put(0,content);
				parent.put("params", params);
				StringEntity se = new StringEntity(parent.toString());
				httpPost.setEntity(se);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				HttpResponse httpResponse = httpclient.execute(httpPost);
				// 9. receive response as inputStream
				inputStream = httpResponse.getEntity().getContent();

				// 10. convert inputstream to string
				if(inputStream != null)
					result = convertInputStreamToString(inputStream);
				else
					result = "Did not work!";
				Log.v(Tag, result);
			} catch (Exception ex) {
				if (ex.getMessage() != null) {
					Log.v(Tag, ex.getMessage());
				}
				ex.printStackTrace();
			}
		}
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}
}

