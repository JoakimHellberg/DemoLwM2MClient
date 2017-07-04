package com.ericsson.appiot.demo.lwm2m.smartobject;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ReadResponse;

import com.ericsson.appiot.demo.lwm2m.gps.GPSPipeListener;
import com.ericsson.appiot.demo.lwm2m.gps.GPSReading;

public class MyLocation extends BaseInstanceEnabler implements GPSPipeListener {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private static final int RESOURCE_ID_LATITUDE = 0;
	private static final int RESOURCE_ID_LONGITUDE = 1;
	private static final int RESOURCE_ID_ALTITUDE = 2;
	private static final int RESOURCE_ID_UNCERTAINTY = 3;
	private static final int RESOURCE_ID_VELOCITY = 4;
	private static final int RESOURCE_ID_TIMESTAMP = 5;

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
        case RESOURCE_ID_LATITUDE:
            return ReadResponse.success(resourceid, latitude);
        case RESOURCE_ID_LONGITUDE:
            return ReadResponse.success(resourceid, longitude);
        case RESOURCE_ID_ALTITUDE:
            return ReadResponse.success(resourceid, altitude);
        case RESOURCE_ID_UNCERTAINTY:
            return ReadResponse.success(resourceid, uncertainty);
        case RESOURCE_ID_VELOCITY:
            return ReadResponse.success(resourceid, velocity);
        case RESOURCE_ID_TIMESTAMP:
            return ReadResponse.success(resourceid, timestamp);
        default:
            return super.read(resourceid);
        }
    }
	
	public void onReading(GPSReading reading) {
		List<Integer> resourcesChanged = new Vector<Integer>();

		if(reading.getLat() != latitude) {
			latitude = reading.getLat();
			resourcesChanged.add(RESOURCE_ID_LATITUDE);
		}
		
		if(reading.getLon() != longitude) {
			longitude = reading.getLon();
			resourcesChanged.add(RESOURCE_ID_LONGITUDE);
		}

		if(reading.getAlt() != altitude) {
			altitude = reading.getAlt();
			resourcesChanged.add(RESOURCE_ID_ALTITUDE);
		}
		
		float uncertaintyFromReading = Math.max(reading.getEpx(), reading.getEpy());
		if(uncertaintyFromReading != uncertainty) {
			uncertainty = uncertaintyFromReading;
			resourcesChanged.add(RESOURCE_ID_UNCERTAINTY);
		}
		
		String newVelocity = Float.toHexString(reading.getSpeed());
		if(!newVelocity.equals(velocity)) {
			velocity = newVelocity;
			resourcesChanged.add(RESOURCE_ID_VELOCITY);
		}
		
		if(reading.getTime().getTime() != timestamp.getTime()) {
			timestamp = reading.getTime();
			resourcesChanged.add(RESOURCE_ID_TIMESTAMP);
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
		}
	}
}