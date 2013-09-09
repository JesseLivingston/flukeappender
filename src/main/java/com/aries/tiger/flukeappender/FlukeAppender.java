package com.aries.tiger.flukeappender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class FlukeAppender extends AppenderSkeleton {

	String serverUrl;
	String transitName;
	String transitPassword;
	String recipient;
	HttpClient client ;
	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		
		if(this.layout != null){
			
			//new Thread(new MessageSender(client, errorHandler, layout.format(event))).start();
			sendMessage(layout.format(event));
		}
	}
	
	@Override
	public void activateOptions(){
		if(serverUrl == null || serverUrl.isEmpty()){
			errorHandler.error("No server URL assigned!!!");
		}
		if(recipient == null || recipient.isEmpty()){
			errorHandler.error("No recipient assigned!!!");
		}
		if(transitName == null || transitName.isEmpty()){
			errorHandler.error("No transit name assigned!!!");
		}
		if(transitPassword == null || transitPassword.isEmpty()){
			errorHandler.error("No transit password assigned!!!");
		}
		
		//create a HttpClient
		client = initHttpClient();
		
		if(login(transitName, transitPassword)){
			errorHandler.error("Login failed, please check your transit name and password!!!");
		}
	}
	
	HttpClient initHttpClient(){
		HttpClient client = new DefaultHttpClient();
		ClientConnectionManager manager = client.getConnectionManager();
		HttpParams params = client.getParams();
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);
		return client;
	}
	
	boolean login(String transitName, String transitPassword){
		if(serverUrl == null || serverUrl.isEmpty()){
			return false;
		}
		HttpPost post = new HttpPost(serverUrl);
		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("username", transitName));
			pairs.add(new BasicNameValuePair("password", transitPassword));
			pairs.add(new BasicNameValuePair("action", "login"));
			pairs.add(new BasicNameValuePair("clienttype", "gtalk"));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			HttpResponse res = client.execute(post);
			InputStream is = res.getEntity().getContent();
			String result = parseResponse(is);
			if(result.equalsIgnoreCase("already login") || result.equalsIgnoreCase("login ok")){
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	void sendMessage(String message){
		HttpPost post = new HttpPost(serverUrl);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", transitName));
		pairs.add(new BasicNameValuePair("password", transitPassword));
		pairs.add(new BasicNameValuePair("action", "sendMessage"));
		pairs.add(new BasicNameValuePair("clienttype", "gtalk"));
		pairs.add(new BasicNameValuePair("receiver", recipient));
		pairs.add(new BasicNameValuePair("message", message));
		try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// important
			post.releaseConnection();
		}
	}
	
	String parseResponse(InputStream is){
		if(is == null){
			return "";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String line = "";
		try {
			while((line = reader.readLine()) != null){
				sb.append(line).append("\r\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getTransitName() {
		return transitName;
	}

	public void setTransitName(String transitName) {
		this.transitName = transitName;
	}

	public String getTransitPassword() {
		return transitPassword;
	}

	public void setTransitPassword(String transitPassword) {
		this.transitPassword = transitPassword;
	}
}

