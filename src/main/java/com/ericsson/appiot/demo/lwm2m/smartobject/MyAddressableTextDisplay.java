package com.ericsson.appiot.demo.lwm2m.smartobject;

import java.util.logging.Logger;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;


public class MyAddressableTextDisplay extends BaseInstanceEnabler {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private static final int RESOURCE_ID_TEXT = 5527;
	private static final int RESOURCE_ID_CLEAR_DISPLAY = 5530;

    private String text;
    
    public MyAddressableTextDisplay() {
    }
	
    @Override
    public ReadResponse read(int resourceid) {
        logger.finest("Read on Text Resource " + resourceid);
        switch (resourceid) {
        case RESOURCE_ID_TEXT:
            return ReadResponse.success(resourceid, text);
        default:
            return super.read(resourceid);
        }
    }
	
    @Override
	public WriteResponse write(int resourceid, LwM2mResource value) {
    	logger.finest("Write on Text Resource " + resourceid + " value: " + value.getValue().toString());
    	 switch (resourceid) {
         	case RESOURCE_ID_TEXT:
        		this.text = value.getValue().toString();        		
        		fireResourcesChange(RESOURCE_ID_TEXT);
        		return WriteResponse.success();
         	default:
         		return WriteResponse.notFound();
    	 }
	}

	@Override
    public ExecuteResponse execute(int resourceid, String params) {
        switch (resourceid) {
        case RESOURCE_ID_CLEAR_DISPLAY:
        	text = "<cleared>";
        	return ExecuteResponse.success();
        default: 
        	return super.execute(resourceid, params);
        }
    }
}