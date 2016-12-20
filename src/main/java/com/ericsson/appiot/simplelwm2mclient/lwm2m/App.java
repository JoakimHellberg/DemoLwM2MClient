package com.ericsson.appiot.simplelwm2mclient.lwm2m;

import static org.eclipse.leshan.LwM2mId.DEVICE;
import static org.eclipse.leshan.LwM2mId.LOCATION;
import static org.eclipse.leshan.LwM2mId.SECURITY;
import static org.eclipse.leshan.LwM2mId.SERVER;
import static org.eclipse.leshan.client.object.Security.psk;
import static org.eclipse.leshan.client.object.Security.noSecBootstap;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Device;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.observer.LwM2mClientObserver;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.client.servers.DmServerInfo;
import org.eclipse.leshan.client.servers.ServerInfo;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.codec.tlv.LwM2mNodeTlvDecoder;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.util.Hex;

import com.appiot.examples.simulated.platform.SimulatedPlatformListener;
import com.appiot.examples.simulated.platform.SimulatedPlatformManager;
import com.appiot.examples.simulated.platform.device.SensorData;
import com.appiot.examples.simulated.platform.device.SimulatedDevice;
import com.ericsson.appiot.example.ConnectivityHelper;


public class App {
	private static final Logger logger = Logger.getLogger("App"); 


	public final static String ENDPOINT = "4EDBC968-244E-4997-BF51-81EE04726037";
	private final static MyLocation locationInstance = new MyLocation();
	private final static MyTemperature temperatureInstance = new MyTemperature();
	
	private final static MyConsole consoleInstance = new MyConsole(); 
	private final static SimulatedPlatformManager manager = new SimulatedPlatformManager();

	public static void main(final String[] args) {
		test();
		String endpoint = "JockeLocalClient";
 		
		// Get server URI
		//String serverURI = "coaps://81.231.234.148:5683"; //coaps://13.81.123.255:5683"; //"lwm2mDemoServer.cloudapp.net:5684";  //coap://lwm2mgw.local:5683
		//String serverURI = "coap://13.81.123.255:5683";
		//String serverURI = "coaps://192.168.56.1:5684";//"coaps://10.13.0.104:5684";
		//String serverURI = "coap://lwm2mdemobs.cloudapp.net:5683";//"coaps://lwm2mdemoserver.cloudapp.net:5684";
		
		//String serverURI = "coaps://192.168.1.155:5684";
		String serverURI = "coaps://10.13.0.79:5684";
		//String serverURI = "coaps://appiotlwm2m.cloudapp.net:5684";
		
		// get security info
		byte[] pskIdentity = "555".getBytes();
		byte[] pskKey = Hex.decodeHex("353535".toCharArray());

		String identity = new String(pskIdentity);
		logger.log(Level.INFO, "identity: " + identity);

		String key = new String(pskKey);
		logger.log(Level.INFO, "PSK: " + key);
		
		
		String ipAddress = "0.0.0.0";//ConnectivityHelper.getExternalIP();
		
//		try {
//			InetAddress address = ConnectivityHelper.getLocalHostLANAddress("eth6");
//			ipAddress = address.getHostAddress();
//
//		} catch (UnknownHostException e1) {
//			System.out.println(e1.getMessage());
//			e1.printStackTrace();
//		}

		logger.log(Level.INFO, "Resolved IP ADDRESS: " + ipAddress);
		
		// get local address
		String localAddress = ipAddress;
		int localPort = 5685;

		// get secure local address
		String secureLocalAddress = ipAddress;
		int secureLocalPort = 5686;

		boolean needsBootstrap = false;
		
		
		
		createAndStartClient(endpoint, localAddress, localPort, secureLocalAddress, secureLocalPort, needsBootstrap,
				serverURI, pskIdentity, pskKey);
	}
	
	
	  public static void test() {
	        byte[] content = new byte[] {-56, 0, 36, 99, 111, 97, 112, 58, 47, 47, 108, 119, 109, 50, 109, 100, 101, 109, 111, 98, 115, 46, 99, 108, 111, 117, 100, 97, 112, 112, 46, 110, 101, 116, 58, 53, 54, 56, 51, -63, 1, 1, -63, 2, 3, -64, 3, -64, 4, -64, 5, -63, 6, 3, -64, 7, -64, 8, -64, 9, -63, 10, 111, -63, 11, 1};
	        LwM2mPath path = new LwM2mPath("/0/0");
	        InputStream is = App.class.getResourceAsStream("/oma-objects-spec.json");
	        LwM2mModel model = new LwM2mModel(ObjectLoader.loadJsonStream(is));
	        LwM2mNode lwM2mNode;
	        try {
	            lwM2mNode = LwM2mNodeTlvDecoder.decode(content, path, model, LwM2mObjectInstance.class);
	            int id = lwM2mNode.getId();
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }


	    }

	public static void createAndStartClient(String endpoint, String localAddress, int localPort,
			String secureLocalAddress, int secureLocalPort, boolean needBootstrap, String serverURI, byte[] pskIdentity,
			byte[] pskKey) {

		// Initialize object list
		ObjectsInitializer initializer = new ObjectsInitializer();
		
		// NO SEC
		//initializer.setInstancesForObject(SECURITY, noSec(serverURI, 123));
        //initializer.setInstancesForObject(SERVER, new Server(123, 30, BindingMode.U, false));
		
		//initializer.setInstancesForObject(SECURITY, noSecBootstap(serverURI));
	       
        // SEC
		initializer.setInstancesForObject(SECURITY, psk(serverURI, 123, pskIdentity, pskKey));
		initializer.setInstancesForObject(SERVER, new Server(123, 3600, BindingMode.U, false));

		initializer.setInstancesForObject(DEVICE, new Device("RASPBERRY", "Pi3", "12345", BindingMode.U.name()));
		initializer.setInstancesForObject(LOCATION, locationInstance);
		initializer.setInstancesForObject(3303, temperatureInstance);
		initializer.setInstancesForObject(3341, consoleInstance);
		
		List<LwM2mObjectEnabler> enablers = initializer.create(SECURITY, SERVER, DEVICE, LOCATION, 3303, 3341);
		
		// Create client
		LeshanClientBuilder builder = new LeshanClientBuilder(endpoint);
		builder.setLocalAddress(localAddress, localPort);
		builder.setLocalSecureAddress(secureLocalAddress, secureLocalPort);
		builder.setObjects(enablers);

		final LeshanClient client = builder.build();

		// Start the client
		client.start();
		
		// GPS
		//GPSPipe.addListener(locationInstance);
		//GPSPipe.start();
		locationInstance.startFakeGPS();
		
		// TEMPERATURE
		SimulatedPlatformManager manager = new SimulatedPlatformManager();
		manager.addDevice("1", "1");
		
		manager.addListener(new SimulatedPlatformListener() {
			public void onData(List<SensorData> datas) {
				for(SensorData data : datas) {
					if(data.getSensorType().equals("TEMP")) {
						temperatureInstance.updateValue((float)data.getValue());
					}
				}				
			}
		});
		manager.start();
		SimulatedDevice device = manager.getDeviceBySerialNumber("1");
		device.start();		
		device.getTemperatureSensor().setReportInterval(1000);
		
		
		
		client.addObserver(new LwM2mClientObserver() {
			
			public void onUpdateTimeout(DmServerInfo server) {
				logger.log(Level.SEVERE, "###UPDATE TIME OUT");
				
			}
			
			public void onUpdateSuccess(DmServerInfo server, String registrationID) {
				logger.log(Level.INFO, "UPDATE SUCCESS");
				
			}
			
			public void onUpdateFailure(DmServerInfo server, ResponseCode responseCode, String errorMessage) {
				logger.log(Level.SEVERE, "###UPDATE FAILURE");
				logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
				
			}
			
			public void onRegistrationTimeout(DmServerInfo server) {
				logger.log(Level.SEVERE, "###REGISTRATION TIME OUT");
			}
			
			public void onRegistrationSuccess(DmServerInfo server, String registrationID) {
				logger.log(Level.INFO, "REGISTRATION SUCCESS");
			}
			
			public void onRegistrationFailure(DmServerInfo server, ResponseCode responseCode, String errorMessage) {
				logger.log(Level.SEVERE, "###REGISTRATION FAILURE");
				logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
				
			}
			
			public void onDeregistrationTimeout(DmServerInfo server) {
				logger.log(Level.SEVERE, "###DEREGISTRATION TIME OUT");
			}
			
			public void onDeregistrationSuccess(DmServerInfo server, String registrationID) {
				logger.log(Level.INFO, "DEREGISTRATION SUCCESS");
			}
			
			public void onDeregistrationFailure(DmServerInfo server, ResponseCode responseCode, String errorMessage) {
				logger.log(Level.SEVERE, "###DEREGISTRATION FAILURE");
				logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
			}
			
			public void onBootstrapTimeout(ServerInfo bsserver) {
				logger.log(Level.SEVERE, "###BOOTSTRAP TIME OUT");
			}
			
			public void onBootstrapSuccess(ServerInfo bsserver) {
				logger.log(Level.INFO, "BOOTSTRAP SUCCESS");
			}
			
			public void onBootstrapFailure(ServerInfo bsserver, ResponseCode responseCode, String errorMessage) {
				logger.log(Level.SEVERE, "###BOOTSTRAP FAILURE");
				logger.log(Level.SEVERE, "ResponseCode: " + responseCode.toString() + " : " + errorMessage);
			}
		});
		
		
		// De-register on shutdown and stop client.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				client.destroy(true); // send de-registration request before
										// destroy
			}
		});


	}
}
