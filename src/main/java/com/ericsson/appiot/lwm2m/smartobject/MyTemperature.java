package com.ericsson.appiot.lwm2m.smartobject;

import java.util.Date;
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
    private Date timestamp;
    private float minMeasuredValue = 0.0f;
    private float maxMeasuredValue = 0.0f;
    private float minRangeValue = 0.0f;
    private float maxRangeValue = 100.0f;
    
    public MyTemperature() {
    	this.timestamp = new Date();
    }    
    
    
    @Override
    public ExecuteResponse execute(int resourceid, String params) {
        switch (resourceid) {
        case 5605:
        	timestamp = new Date();
        	setMaxMeasuredValue(0.0f);
        	setMinMeasuredValue(0.0f);
        	fireResourcesChange(0, 5601, 5602);
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
            return ReadResponse.success(resourceid, getTemperature());
        case 5701:
            return ReadResponse.success(resourceid, getUnits());
        case 5601:
            return ReadResponse.success(resourceid, getMinMeasuredValue());
        case 5602:
            return ReadResponse.success(resourceid, getMaxMeasuredValue());
        case 5603:
            return ReadResponse.success(resourceid, getMinRangeValue());
        case 5604:
            return ReadResponse.success(resourceid, getMaxRangeValue());
        default:
            return super.read(resourceid);
        }
    }

    public void updateValue(float value) {
        temperature = value;
    	timestamp = new Date();
    	List<Integer> resourcesChanged = new Vector<Integer>();
    	resourcesChanged.add(5700);
    	resourcesChanged.add(0);
        
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
			logger.log(Level.FINEST, "FIRED!" + resources.toString() + " " + resources.length);

		} else {
			logger.log(Level.INFO, "Nothing to fire!");
		}        
    }


    
    
    public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public float getMinMeasuredValue() {
		return minMeasuredValue;
	}

	public void setMinMeasuredValue(float minMeasuredValue) {
		this.minMeasuredValue = minMeasuredValue;
	}

	public float getMaxMeasuredValue() {
		return maxMeasuredValue;
	}

	public void setMaxMeasuredValue(float maxMeasuredValue) {
		this.maxMeasuredValue = maxMeasuredValue;
	}

	public float getMinRangeValue() {
		return minRangeValue;
	}

	public void setMinRangeValue(float minRangeValue) {
		this.minRangeValue = minRangeValue;
	}

	public float getMaxRangeValue() {
		return maxRangeValue;
	}

	public void setMaxRangeValue(float maxRangeValue) {
		this.maxRangeValue = maxRangeValue;
	}

	public float getTemperature() {
        return temperature;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}