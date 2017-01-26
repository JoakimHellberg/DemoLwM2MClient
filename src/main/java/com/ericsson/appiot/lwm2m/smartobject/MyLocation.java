package com.ericsson.appiot.lwm2m.smartobject;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.util.Hex;

import com.ericsson.appiot.lwm2m.gps.GPSPipeListener;
import com.ericsson.appiot.lwm2m.gps.GPSReading;

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