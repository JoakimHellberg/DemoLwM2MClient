package com.ericsson.appiot.demo.lwm2m;

import static org.eclipse.leshan.client.object.Security.noSecBootstap;

import java.io.File;
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
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.request.BindingMode;

import com.appiot.examples.simulated.platform.SimulatedPlatformListener;
import com.appiot.examples.simulated.platform.SimulatedPlatformManager;
import com.appiot.examples.simulated.platform.device.SensorData;
import com.appiot.examples.simulated.platform.device.SimulatedDevice;
import com.ericsson.appiot.demo.lwm2m.gps.GPSMock;
import com.ericsson.appiot.demo.lwm2m.gps.GpsSource;
import com.ericsson.appiot.demo.lwm2m.smartobject.MyAddressableTextDisplay;
import com.ericsson.appiot.demo.lwm2m.smartobject.MyFirmwareUpdate;
import com.ericsson.appiot.demo.lwm2m.smartobject.MyLocation;
import com.ericsson.appiot.demo.lwm2m.smartobject.MyTemperature;

public class App {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Make sure ENDPOINT is unique for your application! Otherwise you may run
	 * into conflicts with other applications. You can generate your own UUID
	 * here: https://www.uuidgenerator.net/version1 Then register the device to
	 * the bootstrap server: http://lwm2mdemobs.cloudapp.net/
	 */
	private final static String ENDPOINT = "143a801a-d9ba-4c38-b23f-0493cc75e7c2";

	/**
	 * This is the bootstrap server uri for registering the device. Replace this
	 * value with the url for your bootstrap server.
	 */
	//private static final String SERVER_URI = "coap://40.69.67.129:5673";
	private static final String SERVER_URI = "coap://lwm2mdemobs.cloudapp.net:5683";
	
	
	/** SMART OBJECT ID'S */
	private static final int SECURITY_ID = 0;
	private static final int SERVER_ID = 1;
	private static final int DEVICE_ID = 3;
	private static final int FIRMWARE_UPDATE_ID = 5;
	private static final int LOCATION_ID = 6;
	private static final int TEMPERATURE_ID = 3303;
	private static final int ADDRESSABLE_TEXT_DISPLAY_ID = 3341;

	private LeshanClient leshanClient;
	private SimulatedDevice simulatedDevice;
	private SimulatedPlatformManager manager;
	private GpsSource gpsDataSource;

	public static void main(final String[] args) {
		App app = new App();
		app.register(SERVER_URI, ENDPOINT);
	}

	public void register(String serverUri, String endpoint) {
		String ipAddress = "0.0.0.0";
		int localPort = 5685;
		int secureLocalPort = 5686;

		// Smart Objects
		Device device = new Device("AppIoT", "Demo LwM2M Client", ENDPOINT, BindingMode.U.name());
		MyFirmwareUpdate firmwareUpdate = new MyFirmwareUpdate();
		MyLocation location = new MyLocation();
		final MyTemperature temperature = new MyTemperature();
		MyAddressableTextDisplay addressableTextDisplay = new MyAddressableTextDisplay();
		
		// Setup Objects Initializer 
		LwM2mModel model = new LwM2mModel(ObjectLoader.load(new File("models/")));
		ObjectsInitializer initializer = new ObjectsInitializer(model);
		initializer.setInstancesForObject(SECURITY_ID, noSecBootstap(serverUri));
		initializer.setInstancesForObject(DEVICE_ID, device);		
		initializer.setInstancesForObject(FIRMWARE_UPDATE_ID, firmwareUpdate);
		initializer.setInstancesForObject(LOCATION_ID, location);
		initializer.setInstancesForObject(TEMPERATURE_ID, temperature);
		initializer.setInstancesForObject(ADDRESSABLE_TEXT_DISPLAY_ID, addressableTextDisplay);
		
		List<LwM2mObjectEnabler> enablers = initializer.create(
				SECURITY_ID, 
				SERVER_ID, 
				DEVICE_ID, 
				FIRMWARE_UPDATE_ID,
				LOCATION_ID,
				TEMPERATURE_ID, 
				ADDRESSABLE_TEXT_DISPLAY_ID
		);

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
				for (SensorData data : datas) {
					if (data.getSensorType().equals("TEMP")) {
						temperature.updateValue((float) data.getValue());
					}
				}
			}
		});
		simulatedDevice = manager.getDeviceBySerialNumber("1");
	}

	private void start() {
		gpsDataSource.start();
		manager.start();
		simulatedDevice.start();
	}

	private void stop() {
		gpsDataSource.stop();
		manager.stop();
		simulatedDevice.stop();
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