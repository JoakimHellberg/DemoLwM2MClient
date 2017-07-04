package com.ericsson.appiot.demo.lwm2m.smartobject;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;


public class MyTemperature extends BaseInstanceEnabler {
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

    private String units = "Cel";
    
    private float temperature;
    private float minMeasuredValue = 0.0f;
    private float maxMeasuredValue = 0.0f;
    private float minRangeValue = 0.0f;
    private float maxRangeValue = 100.0f;
    
    public MyTemperature() {
    }    
    
    @Override
    public ExecuteResponse execute(int resourceid, String params) {
        switch (resourceid) {
        case 5605:
        	minMeasuredValue = temperature;
        	maxMeasuredValue = temperature;
        	fireResourcesChange(5601, 5602);
        	return ExecuteResponse.success();
        default: 
        	return super.execute(resourceid, params);
        }
    }

	@Override
    public ReadResponse read(int resourceid) {
        logger.finest("Read on Temperature Resource " + resourceid);
        switch (resourceid) {
        case 5700:
            return ReadResponse.success(resourceid, temperature);
        case 5701:
            return ReadResponse.success(resourceid, units);
        case 5601:
            return ReadResponse.success(resourceid, minMeasuredValue);
        case 5602:
            return ReadResponse.success(resourceid, maxMeasuredValue);
        case 5603:
            return ReadResponse.success(resourceid, minRangeValue);
        case 5604:
            return ReadResponse.success(resourceid, maxRangeValue);
        default:
            return super.read(resourceid);
        }
    }

    public void updateValue(float value) {
        temperature = value;
    	List<Integer> resourcesChanged = new Vector<Integer>();
    	resourcesChanged.add(5700);
        
        if(temperature > maxMeasuredValue) {
        	maxMeasuredValue = temperature;
        	resourcesChanged.add(5602);
        }
        else if(temperature < minMeasuredValue) {
        	minMeasuredValue = temperature;
        	resourcesChanged.add(5601);
        }
        
		if(resourcesChanged.size() > 0) {
			int[] resources = new int[resourcesChanged.size()];
			int index = 0;
			for(Integer r : resourcesChanged) {
				logger.log(Level.FINEST, "resource changed " + r);
				resources[index] = r;
				index++;
			}
			fireResourcesChange(resources);
		}        
    }
}