package com.ericsson.appiot.simplelwm2mclient.appiot;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		
		
		logger.log(Level.INFO, "called");
		

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
