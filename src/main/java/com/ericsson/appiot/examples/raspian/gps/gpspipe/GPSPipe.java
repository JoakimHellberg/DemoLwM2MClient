package com.ericsson.appiot.examples.raspian.gps.gpspipe;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GPSPipe implements Runnable {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	private static boolean go = true;
	
	private static boolean running = false;
	
	private static Vector<GPSPipeListener> _listeners = new Vector<GPSPipeListener>(8);
	
	public static void addListener(GPSPipeListener listener)
	{
		_listeners.add(listener);
	}
	
	public static void removeListener(GPSPipeListener listener)
	{
		_listeners.remove(listener);
	}
	

	
	public static void start()
	{
		GPSPipe program = new GPSPipe();
		Thread t = new Thread(program);
		t.start();
	}
	
	public void run() 
	{
		while (go) {
			running = true;
			
			GPSReading reading = readGPS();
			if(reading != null) {
				for(int i = 0; i < _listeners.size(); i++) {
					if(_listeners.elementAt(i) != null) {
						_listeners.get(i).onReading(reading);
					}
				}
			}
			
			try {
				Thread.sleep(10000L);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		running = false;		
	}

	private GPSReading readGPS() {		
		GPSReading result = null;
		try {
			String command = "gpspipe -w -n 10";
			//logger.fine("executing command on OS: " + command);
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			int exitCode = p.exitValue();
			logger.fine("gpspipe -w -n 10 process exited with status code: " + exitCode);			

			if(exitCode != 0) {
		        BufferedReader output = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		        String info = output.readLine();
				logger.severe("Failed to execute gpspipe -w -n 10: " + info);
				return null;
			}

			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String info = output.readLine();
			while(info != null) {
				
				logger.fine(info);		
		    	JsonElement element = new JsonParser().parse(info);
		    	JsonObject obj = element.getAsJsonObject();
		    	String classType = obj.get("class").getAsString();
		    	if(classType != null && classType.equals("TPV")) {
		    		
			    	String tag = obj.get("tag").getAsString();
			    	if(tag != null) { // && (tag.equals("GLL") || classType.equals("0x0106"))) {
			    		result = new GPSReading();
			    		
				    	float lat = obj.get("lat").getAsFloat();
				    	float lon = obj.get("lon").getAsFloat();
				    	float alt = obj.get("alt").getAsFloat();
				    	float epx = obj.get("epx").getAsFloat();
				    	float epy = obj.get("epy").getAsFloat();
				    	float speed = obj.get("speed").getAsFloat();
				    	float climb = obj.get("climb").getAsFloat();
				    	String time = obj.get("time").getAsString();
				    	
				    	result.setLat(lat);
				    	result.setLon(lon);
				    	result.setAlt(alt);
				    	result.setEpx(epx);
				    	result.setEpy(epy);
				    	result.setSpeed(speed);
				    	result.setClimb(climb);
				    	result.setTime(time);
				    	return result;
			    	}
		    	}
				info = output.readLine();
			}        
        } catch (Exception e) {
          	 logger.log(Level.WARNING, "Failed to retreive GPS data." + e.getMessage(), e);
        }
		return result;
	}
}
