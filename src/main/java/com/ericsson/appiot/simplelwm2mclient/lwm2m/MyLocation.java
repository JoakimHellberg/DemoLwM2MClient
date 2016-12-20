package com.ericsson.appiot.simplelwm2mclient.lwm2m;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.util.Hex;

import com.ericsson.appiot.examples.raspian.gps.gpspipe.GPSPipeListener;
import com.ericsson.appiot.examples.raspian.gps.gpspipe.GPSReading;

public class MyLocation extends BaseInstanceEnabler implements GPSPipeListener {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	
    private float latitude = 0.0f;
    private float longitude = 0.0f;
    private float altitude = 0.0f;
    private float uncertainty = 0.0f;
    private String velocity = "";
    private Date timestamp = new Date();

    public MyLocation() {
        timestamp = new Date();
    }
    
    
    private class FakeGPSSensor implements Runnable {

    	List<GPSReading> readings;
    	public FakeGPSSensor(List<GPSReading> readings) {
    		this.readings = readings;
    	}
		
		public void run() {
			while(true) {
				for(GPSReading reading : readings) {
					onReading(reading);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}	
		}
    }
    
    
    public void startFakeGPS() {
    	try {
        	InputStream is = MyLocation.class.getClassLoader().getResourceAsStream("dummyroute.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) {
				result.append(line);
			}
			
        	String route = result.toString();
    		StringTokenizer st = new StringTokenizer(route, " ");
    		List<GPSReading> readings = new Vector<GPSReading>();
    		
    		TimeZone tz = TimeZone.getTimeZone("UTC");
    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    		df.setTimeZone(tz);
    		
    		while(st.hasMoreTokens()) {
    			String position = st.nextToken();
    			String[] coords = position.split(",");
    			float lon = Float.parseFloat(coords[0]);
    			float lat = Float.parseFloat(coords[1]);
    			
    			GPSReading reading = new GPSReading();
    			reading.setLon(lon);
    			reading.setLat(lat);
    			reading.setTime(df.format(new Date()));
    			readings.add(reading);
    		}
    		
    		FakeGPSSensor fake = new FakeGPSSensor(readings);
    		Thread t = new Thread(fake);
    		t.start();
    		
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    }

    @Override
    public ReadResponse read(int resourceid) {
    	logger.log(Level.FINEST, "Read on Location Resource " + resourceid);
        switch (resourceid) {
        case 0:
            return ReadResponse.success(resourceid, getLatitude());
        case 1:
            return ReadResponse.success(resourceid, getLongitude());
        case 2:
            return ReadResponse.success(resourceid, getAltitude());
        case 3:
            return ReadResponse.success(resourceid, getUncertainty());
        case 4:
            return ReadResponse.success(resourceid, getVelocity());
        case 5:
            return ReadResponse.success(resourceid, getTimestamp());
        default:
            return super.read(resourceid);
        }
    }

    public String getLatitude() {
        return Float.toString(latitude);
    }

    public String getLongitude() {
        return Float.toString(longitude);
    }

    public String getAltitude() {
    	return Float.toString(altitude);
    }
    
    public String getUncertainty() {
    	return Float.toString(uncertainty);
    }
    
    public String getVelocity() {
    	return Hex.encodeHexString(velocity.getBytes());
    }
    
    public Date getTimestamp() {
        return timestamp;
    }

	
	public void onReading(GPSReading reading) {
		
		logger.log(Level.FINEST, "new Reading, lon:" + reading.getLon() + " lat:" + reading.getLat());
		List<Integer> resourcesChanged = new Vector<Integer>();

		if(reading.getLat() != latitude) {
			latitude = reading.getLat();
			resourcesChanged.add(0);
		}
		
		if(reading.getLon() != longitude) {
			longitude = reading.getLon();
			resourcesChanged.add(1);
		}

		if(reading.getAlt() != altitude) {
			altitude = reading.getAlt();
			resourcesChanged.add(2);
		}
		
		float uncertaintyFromReading = Math.max(reading.getEpx(), reading.getEpy());
		if(uncertaintyFromReading != uncertainty) {
			uncertainty = uncertaintyFromReading;
			resourcesChanged.add(3);
		}
		
		String newVelocity = Float.toHexString(reading.getSpeed());
		if(!newVelocity.equals(velocity)) {
			velocity = newVelocity;
			resourcesChanged.add(4);
		}
		
		if(reading.getTime().getTime() != timestamp.getTime()) {
			timestamp = reading.getTime();
			resourcesChanged.add(5);
		}
		
		logger.log(Level.FINEST, "Resources changed: " + resourcesChanged.size());
		
		if(resourcesChanged.size() > 0) {
			int[] resources = new int[resourcesChanged.size()];
			int index = 0;
			for(Integer r : resourcesChanged) {
				logger.log(Level.FINER, "resource changed " + r);
				resources[index] = r;
				index++;
			}
			fireResourcesChange(resources);
			logger.log(Level.FINER, "FIRED!" + resources.toString() + " " + resources.length);

		} else {
			logger.log(Level.FINER, "Nothing to fire!");
		}
		
	}
}