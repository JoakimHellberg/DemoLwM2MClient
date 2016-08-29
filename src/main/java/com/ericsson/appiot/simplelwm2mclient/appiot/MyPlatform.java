package com.ericsson.appiot.simplelwm2mclient.appiot;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.appiot.examples.simulated.platform.SimulatedPlatformListener;
import com.appiot.examples.simulated.platform.SimulatedPlatformManager;
import com.appiot.examples.simulated.platform.device.SensorData;
import com.appiot.examples.simulated.platform.device.SimulatedDevice;
import com.ericsson.appiot.simplelwm2mclient.lwm2m.MyTemperature;
import com.google.common.io.Resources;

import se.sigma.sensation.gateway.sdk.client.Platform;
import se.sigma.sensation.gateway.sdk.client.PlatformInitialisationException;
import se.sigma.sensation.gateway.sdk.client.SensationClient;
import se.sigma.sensation.gateway.sdk.client.core.SensationClientProperties;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorDeleteResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorStatus;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorStatusCode;
import se.sigma.sensation.gateway.sdk.client.data.ISensorMeasurement;
import se.sigma.sensation.gateway.sdk.client.data.NetworkSetting;
import se.sigma.sensation.gateway.sdk.client.data.NetworkSettingResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.RebootResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.RestartApplicationResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.SensorCollectionRegistrationResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.UpdatePackage;
import se.sigma.sensation.gateway.sdk.client.data.UpdatePackageResponseCode;
import se.sigma.sensation.gateway.sdk.client.registry.SensorCollectionRegistration;

public class MyPlatform implements Platform {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private MyTemperature temperature;
	public MyPlatform(MyTemperature temperature) {
		this.temperature = temperature;
	}
	
	public void acknowledgeMeasurementsSent(List<ISensorMeasurement> arg0) {
		// TODO Auto-generated method stub

	}

	public NetworkSettingResponseCode addNetworkSetting(NetworkSetting arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public DataCollectorDeleteResponseCode deleteDataCollector(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void handleCustomCommand(String arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	public void init(SensationClient client) throws PlatformInitialisationException {

		Properties properties = new Properties();

		try (InputStream props = Resources.getResource("producer.props").openStream()) {
			properties.load(props);
		} catch (Throwable throwable) {
			System.out.printf("%s", throwable.getStackTrace());
		}

		final KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
//		finally {
//			if (producer != null) {
//			//	producer.close();
//			}
//		}
		logger.log(Level.INFO, "called");

		SimulatedPlatformManager manager = new SimulatedPlatformManager();
		manager.addDevice("1", "1");
		
		manager.addListener(new SimulatedPlatformListener() {
			public void onData(List<SensorData> datas) {
				for(SensorData data : datas) {
					if(data.getSensorType().equals("TEMP")) {
						temperature.updateValue((float)data.getValue());
						try {
							producer.send(new ProducerRecord<String, String>("ingest",
									String.format("{\"type\":\"test\", \"t\":%.3f}", data.getValue())));
							//producer.flush();
							logger.log(Level.INFO, "OK"); 
						} catch (Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to send measurement", throwable);
						} 
					}
				}				
			}
		});
		manager.start();
		SimulatedDevice device = manager.getDeviceBySerialNumber("1");
		device.start();
	}

	public RebootResponseCode reboot() {
		// TODO Auto-generated method stub
		return null;
	}

	public void reportDiscoveredSensorCollections(String arg0) {
		// TODO Auto-generated method stub

	}

	public RestartApplicationResponseCode restartApplication() {
		// TODO Auto-generated method stub
		return null;
	}

	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationCreated(
			SensorCollectionRegistration arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationDeleted(
			SensorCollectionRegistration arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationUpdated(
			SensorCollectionRegistration arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public UpdatePackageResponseCode updateApplication(UpdatePackage arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateDataCollectorSettings(SensationClientProperties arg0) {
		// TODO Auto-generated method stub

	}

	public DataCollectorStatus updateDataCollectorStatus() {
		DataCollectorStatus result = new DataCollectorStatus();
		result.setStatus(DataCollectorStatusCode.OK);
		return result;
	}

	public UpdatePackageResponseCode updateSensorCollection(SensorCollectionRegistration arg0, UpdatePackage arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public SensorCollectionRegistration updateSensorCollectionStatus(SensorCollectionRegistration arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public UpdatePackageResponseCode updateSystem(UpdatePackage arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
