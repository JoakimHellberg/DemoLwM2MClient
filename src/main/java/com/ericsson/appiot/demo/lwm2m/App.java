package com.ericsson.appiot.demo.lwm2m;

import static org.eclipse.leshan.LwM2mId.DEVICE;
import static org.eclipse.leshan.LwM2mId.LOCATION;
import static org.eclipse.leshan.LwM2mId.SECURITY;
import static org.eclipse.leshan.LwM2mId.SERVER;
import static org.eclipse.leshan.client.object.Security.noSecBootstap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.observer.LwM2mClientObserver;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.servers.DmServerInfo;
import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.request.BindingMode;

import com.appiot.examples.simulated.platform.SimulatedPlatformListener;
import com.appiot.examples.simulated.platform.SimulatedPlatformManager;
import com.appiot.examples.simulated.platform.device.SensorData;
import com.appiot.examples.simulated.platform.device.SimulatedDevice;
import com.ericsson.appiot.lwm2m.gps.GPSMock;
import com.ericsson.appiot.lwm2m.gps.GpsSource;
import com.ericsson.appiot.lwm2m.smartobject.MyConsole;
import com.ericsson.appiot.lwm2m.smartobject.MyLocation;
import com.ericsson.appiot.lwm2m.smartobject.MyTemperature;

public class App {
	private static final Logger logger = Logger.getLogger("App"); 

	/**
	 * Make sure ENDPOINT is unique for your application! Otherwise you may run into conflicts with other applications.
	 * You can generate your own UUID here: https://www.uuidgenerator.net/version1
	 * Then register the device to the bootstrap server: http://lwm2mdemobs.cloudapp.net/
	 */
	// smart building test
	//private final static String ENDPOINT = "8677e32f-253f-4e0a-a34f-b8bc4d485c8b";
	// DEV NG
	//private final static String ENDPOINT = "09a6c85d-662f-4efd-917f-a0ca0a7e2420";
	// Local
	//private final static String ENDPOINT = "aabbe874-a6c7-43bd-a54e-3cae4cfb335b";
	// RASP
	private final static String ENDPOINT = "9a97f722-352c-4e85-845b-e8dc469760d2";
	
	/**
	 * This is the bootstrap server uri for registering the device.
	 */
	private static final String SERVER_URI = "coap://lwm2mdemobs.cloudapp.net:5683";
	
	private LeshanClient leshanClient;
	private SimulatedDevice device;
	private SimulatedPlatformManager manager;
	private GpsSource gpsDataSource;
	
	public static void main(final String[] args) {
	
		App app = new App();
		app.start(SERVER_URI, ENDPOINT);
	}
	

	public void start(String serverUri, String endpoint) {
		String ipAddress = "0.0.0.0";	
		int localPort = 5683;
		int secureLocalPort = 5684;

		
		// Smart Objects
		MyLocation location = new MyLocation();
		final MyTemperature temperature = new MyTemperature();
		MyConsole addressableTextDisplay = new MyConsole();
		

		// Initialize object list
		ObjectsInitializer initializer = new ObjectsInitializer();
		initializer.setInstancesForObject(SECURITY, noSecBootstap(serverUri));
		initializer.setInstancesForObject(DEVICE, new Device("AppIoT", "Simple LwM2M Client", ENDPOINT, BindingMode.U.name()));
		initializer.setInstancesForObject(LOCATION, location);
		initializer.setInstancesForObject(3303, temperature);
		initializer.setInstancesForObject(3341, addressableTextDisplay);
		
		List<LwM2mObjectEnabler> enablers = initializer.create(SECURITY, SERVER, DEVICE, LOCATION, 3303, 3341);
		
		// Create and start client 
		LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
		builder.setLocalAddress(ipAddress, localPort);
		builder.setLocalSecureAddress(ipAddress, secureLocalPort);
		builder.setObjects(enablers);
		
		leshanClient = builder.build();
		leshanClient.addObserver(observer);
		leshanClient.start();
		
		// Start GPS reporting
		gpsDataSource = new GPSMock();
		gpsDataSource.addListener(location);

		// Start temperature reporting
		manager = new SimulatedPlatformManager();
		manager.addDevice("1", "1");
		
		manager.addListener(new SimulatedPlatformListener() {
			public void onData(List<SensorData> datas) {
				for(SensorData data : datas) {
					if(data.getSensorType().equals("TEMP")) {
						temperature.updateValue((float)data.getValue());
					}
				}				
			}
		});
		device = manager.getDeviceBySerialNumber("1");
			
		device.getTemperatureSensor().setReportInterval(1000);
	}
	
	
	private void start() {
		gpsDataSource.start();
		manager.start();
		device.start();	

	}
	
	private void stop() {
		gpsDataSource.stop();
		manager.stop();
		device.stop();	

	}
	
	
	// LwM2M Client Observer for monitoring registration sequence
	private LwM2mClientObserver observer = new LwM2mClientObserver() {
		
		public void onUpdateTimeout(DmServerInfo server) {
			logger.log(Level.SEVERE, "###UPDATE TIME OUT");
			stop();
		}
		
		public void onUpdateSuccess(DmServerInfo server, String registrationID) {
			logger.log(Level.INFO, "UPDATE SUCCESS");
		}
		
		public void onUpdateFailure(DmServerInfo server, ResponseCode responseCode, String errorMessage) {
			logger.log(Level.SEVERE, "###UPDATE FAILURE");
			logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
			stop();
		}
		
		public void onRegistrationTimeout(DmServerInfo server) {
			logger.log(Level.SEVERE, "###REGISTRATION TIME OUT");
		}
		
		public void onRegistrationSuccess(DmServerInfo server, String registrationID) {
			logger.log(Level.INFO, "REGISTRATION SUCCESS");
			start();
		}
		
		public void onRegistrationFailure(DmServerInfo server, ResponseCode responseCode, String errorMessage) {
			logger.log(Level.SEVERE, "###REGISTRATION FAILURE");
			logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
			stop();
		}
		
		public void onDeregistrationTimeout(DmServerInfo server) {
			logger.log(Level.SEVERE, "###DEREGISTRATION TIME OUT");
			stop();
		}
		
		public void onDeregistrationSuccess(DmServerInfo server, String registrationID) {
			logger.log(Level.INFO, "DEREGISTRATION SUCCESS");
			stop();
		}
		
		public void onDeregistrationFailure(DmServerInfo server, ResponseCode responseCode, String errorMessage) {
			logger.log(Level.SEVERE, "###DEREGISTRATION FAILURE");
			logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
			stop();
		}
		
		public void onBootstrapTimeout(ServerInfo bsserver) {
			logger.log(Level.SEVERE, "###BOOTSTRAP TIME OUT");
			stop();
		}
		
		public void onBootstrapSuccess(ServerInfo bsserver) {
			logger.log(Level.INFO, "BOOTSTRAP SUCCESS");		
		}
		
		public void onBootstrapFailure(ServerInfo bsserver, ResponseCode responseCode, String errorMessage) {
			logger.log(Level.SEVERE, "###BOOTSTRAP FAILURE");
			logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
			stop();
		}
	};    	
	
}
