package com.ericsson.appiot.simplelwm2mclient.lwm2m;

import java.util.Date;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTemperature extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MyTemperature.class);

    private float temperature;
    private Date timestamp;
    
    public MyTemperature() {
    	this.timestamp = new Date();     
    }

	@Override
    public ReadResponse read(int resourceid) {
        LOG.info("Read on Temperature Resource " + resourceid);
        switch (resourceid) {
        case 5700:
            return ReadResponse.success(resourceid, getTemperature());
        default:
            return super.read(resourceid);
        }
    }

    public void updateValue(float value) {
        temperature = value;
    	timestamp = new Date();
        fireResourcesChange(0, 5700);
        
    }


    public float getTemperature() {
        return temperature;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}