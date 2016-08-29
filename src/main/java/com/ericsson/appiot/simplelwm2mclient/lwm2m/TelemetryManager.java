package com.ericsson.appiot.simplelwm2mclient.lwm2m;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import com.ericsson.appiot.simplelwm2mclient.lwm2m.telemetry.TelemetryData;
import com.google.common.io.Resources;
import com.google.gson.Gson;





public class TelemetryManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private KafkaProducer<String, String> producer;
	private MyTelemetryEndpoint telemetryEndpointObject;
	
	private TelemetryData telemetryData = new TelemetryData();
	
	public TelemetryManager(MyTelemetryEndpoint telemetryEndpointObject) {
		this.telemetryEndpointObject = telemetryEndpointObject;
		init();
	}
	
	public void init() {
	
	}
	
	int count = 0;
	public void addMeasurement(int objectId, int instanceId, int resourceId, long timestamp, double value) {
		telemetryData.addMeasurement(App.ENDPOINT, objectId, instanceId, resourceId, timestamp, value);
		count++;
		if(count == 5) {
			send();
			telemetryData.clear();
			count = 0;
		}
	}
	
	public void send() {
		try {
			CoapClient client = new CoapClient("coap://127.0.0.1:6683/Ingest");
			client.post(new Gson().toJson(telemetryData), MediaTypeRegistry.APPLICATION_JSON);
		} catch (Throwable throwable) {
			logger.log(Level.SEVERE, "Failed to send measurement", throwable);
		} 
	}
	
	public void send(int objectId, int instanceId, int resourceId, long timestamp, double value) {
		try {
			if(producer != null) {
				producer.send(new ProducerRecord<String, String>("ingest",
						String.format("{\"endpoint\":\"%s, /%s/%s/%s\", \"value\":%s, \"timestamp:\":%s}", App.ENDPOINT, objectId, instanceId, resourceId, value, Long.toString(timestamp))));
				producer.flush();
				logger.log(Level.INFO, "OK");
			} else {
				logger.log(Level.INFO, "producer null");
			}
			
		} catch (Throwable throwable) {
			logger.log(Level.SEVERE, "Failed to send measurement", throwable);
		} 
	}

}
