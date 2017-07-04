package com.ericsson.appiot.demo.lwm2m.gps;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Simple wrapper for using linux gpspipe.
 * @author Joakim Hellberg
 *
 */
public class GPSPipe implements Runnable, GpsSource {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	private boolean stop = false;
	private Thread runningThread;
	
	private Vector<GPSPipeListener> listeners = new Vector<GPSPipeListener>(8);
	
	public void addListener(GPSPipeListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(GPSPipeListener listener)
	{
		listeners.remove(listener);
	}
	
	public void start()
	{
		stop = false;
		runningThread = new Thread(this);
		runningThread.start();
	}
	
	public void stop() {
		stop = true;
		runningThread.interrupt();
	}
	
	
	public void run() 
	{
		while (!stop) {
			GPSReading reading = readGPS();
			if(reading != null) {
				for(int i = 0; i < listeners.size(); i++) {
					if(listeners.elementAt(i) != null) {
						listeners.get(i).onReading(reading);
					}
				}
			}
			
			try {
				Thread.sleep(10000L);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	private GPSReading readGPS() {		
		GPSReading result = null;
		try {
			String command = "gpspipe -w -n 10";
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
			    	if(tag != null) { 
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
