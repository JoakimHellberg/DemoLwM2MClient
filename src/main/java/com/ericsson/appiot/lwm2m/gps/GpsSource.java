package com.ericsson.appiot.lwm2m.gps;

public interface GpsSource {
	void addListener(GPSPipeListener listener);
	void removeListener(GPSPipeListener listener);	
	void start();
	void stop();
}
