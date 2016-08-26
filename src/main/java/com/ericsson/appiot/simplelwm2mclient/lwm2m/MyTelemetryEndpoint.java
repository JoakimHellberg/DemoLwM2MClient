package com.ericsson.appiot.simplelwm2mclient.lwm2m;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyTelemetryEndpoint extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(MyTelemetryEndpoint.class);
    
    private String url = "tjoffa";
    private String sas = "123";
    
    

    private static final String SUPPORTED_PROTOCOLS = "HTTPS,AMQPS";
    
    public MyTelemetryEndpoint() {

    }

    @Override
    public ReadResponse read(int resourceid) {
        LOG.info("Read on Telemetry Endpoint Resource " + resourceid);
        switch (resourceid) {
        case 10001:
            return ReadResponse.success(resourceid, getUrl());
        case 10002:
        	return ReadResponse.success(resourceid, getSas());
        case 10003:
        	return ReadResponse.success(resourceid, SUPPORTED_PROTOCOLS);
        default:
            return super.read(resourceid);
        }
    }

    @Override
    public ExecuteResponse execute(int resourceid, String params) {
        LOG.info("Execute on Device resource " + resourceid);
        if (params != null && params.length() != 0)
            System.out.println("\t params " + params);
        return ExecuteResponse.success();
    }

    @Override
    public WriteResponse write(int resourceid, LwM2mResource value) {
        LOG.info("Write on Device Resource " + resourceid + " value " + value);
        switch (resourceid) {
        case 10001:
	        setUrl((String) value.getValue());
	        fireResourcesChange(resourceid);
	        return WriteResponse.success();
        case 10002:
	        setSas((String) value.getValue());
	        fireResourcesChange(resourceid);
	        return WriteResponse.success();
        default:
            return super.write(resourceid, value);
        }
    }

    
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
    	this.url = url;
    }
    
    public String getSas() {
		return sas;
	}

	public void setSas(String sas) {
		this.sas = sas;
	}
}