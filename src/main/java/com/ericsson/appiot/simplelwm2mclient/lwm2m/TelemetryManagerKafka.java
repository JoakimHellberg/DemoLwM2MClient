package com.ericsson.appiot.simplelwm2mclient.lwm2m;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.ericsson.appiot.simplelwm2mclient.lwm2m.telemetry.TelemetryData;
import com.google.common.io.Resources;
import com.google.gson.Gson;





public class TelemetryManagerKafka {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private KafkaProducer<String, String> producer;
	private MyTelemetryEndpoint telemetryEndpointObject;
	
	private TelemetryData telemetryData = new TelemetryData();
	
	public TelemetryManagerKafka(MyTelemetryEndpoint telemetryEndpointObject) {
		this.telemetryEndpointObject = telemetryEndpointObject;
		init();
	}
	
	public void init() {
		if(producer != null) {
			producer.flush();
			producer.close();
			producer = null;
		}
		
		Properties properties = new Properties();
		InputStream props = null;
		try {
			props = Resources.getResource("producer.props").openStream();
			properties.load(props);
			props.close();
			properties.setProperty("bootstrap.servers", telemetryEndpointObject.getUrl());
			producer = new KafkaProducer<>(properties);
			logger.log(Level.INFO, "Producer set up");
		} catch (Throwable throwable) {
			logger.log(Level.SEVERE, "Failed to load properties", throwable);
		} finally {
			if (props != null) {
				try {
					props.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
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
			if(producer != null) {
				producer.send(new ProducerRecord<String, String>("ingest", new Gson().toJson(telemetryData)));
				producer.flush();
				logger.log(Level.INFO, "OK");
			} else {
				logger.log(Level.INFO, "producer null");
			}
			
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
