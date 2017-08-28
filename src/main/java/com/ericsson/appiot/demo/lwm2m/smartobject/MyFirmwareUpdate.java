package com.ericsson.appiot.demo.lwm2m.smartobject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;

public class MyFirmwareUpdate extends BaseInstanceEnabler {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	public static final int RESOURCE_ID_PACKAGE = 0;
	public static final int RESOURCE_ID_PACKAGE_URI = 1;
	public static final int RESOURCE_ID_UPDATE = 2;
	public static final int RESOURCE_ID_STATE = 3;
	public static final int RESOURCE_ID_UPDATE_RESULT = 5;
	public static final int RESOURCE_ID_PACKAGE_NAME = 6;
	public static final int RESOURCE_ID_PACKAGE_VERSION = 7;
	public static final int RESOURCE_ID_PROTOCOL_SUPPORT = 8;
	public static final int RESOURCE_ID_DELIVERY_METHOD = 9;

	public static final int STATE_IDLE = 0;
	public static final int STATE_DOWLOADING = 1;
	public static final int STATE_DOWNLOADED = 2;
	public static final int STATE_UPDATING = 3;

	public static final int UPDATE_RESULT_INITIAL = 0;
	public static final int UPDATE_RESULT_SUCCESS = 1;
	public static final int UPDATE_RESULT_NOT_ENOUGH_FLASH_MEMORY = 2;
	public static final int UPDATE_RESULT_OUT_OF_RAM = 3;
	public static final int UPDATE_RESULT_CONNECTION_LOST_DURING_DOWNLOAD = 4;
	public static final int UPDATE_RESULT_INTEGRETY_CHECK_FAILURE = 5;
	public static final int UPDATE_RESULT_UNSUPPORTED_PACKAGE_TYPE = 6;
	public static final int UPDATE_RESULT_INVALID_URI = 7;
	public static final int UPDATE_RESULT_FIRMWARE_UPDATE_FAILED = 8;
	public static final int UPDATE_RESULT_UNSUPPORTED_PROTOCOL = 9;

	private String packageUri;
	private int state = STATE_IDLE;
	private int updateResult = UPDATE_RESULT_INITIAL;
	private String packageName = "New package";
	private String packageVersion = "1.0";
	private int protocolSupport = 3;
	private int deliveryMethod = 0;

	public MyFirmwareUpdate() {
	}

	@Override
	public ReadResponse read(int resourceid) {
		logger.finest("Read on Resource " + resourceid);
		switch (resourceid) {
		case RESOURCE_ID_UPDATE_RESULT:
			return ReadResponse.success(resourceid, this.updateResult);
		case RESOURCE_ID_STATE:
			return ReadResponse.success(resourceid, this.state);
		case RESOURCE_ID_DELIVERY_METHOD:
			return ReadResponse.success(resourceid, this.deliveryMethod);
		case RESOURCE_ID_PROTOCOL_SUPPORT:
			return ReadResponse.success(resourceid, this.protocolSupport);
		case RESOURCE_ID_PACKAGE_NAME:
			return ReadResponse.success(resourceid, this.packageName);
		case RESOURCE_ID_PACKAGE_VERSION:
			return ReadResponse.success(resourceid, this.packageVersion);

		default:
			return super.read(resourceid);
		}
	}
	
	@Override
	public WriteResponse write(int resourceid, LwM2mResource value) {
		logger.info("Write on Resource " + resourceid + " value: " + value.getValue().toString());
		switch (resourceid) {
		case RESOURCE_ID_PACKAGE:
			storePackageFile(value.getValue().toString());
			return WriteResponse.success();
		case RESOURCE_ID_PACKAGE_URI:
			this.packageUri = value.getValue().toString();
			startDownload();
			return WriteResponse.success();
		default:
			return WriteResponse.notFound();
		}
	}

	@Override
	public ExecuteResponse execute(int resourceid, String params) {
		logger.info("Execute on Resource " + resourceid);
		switch (resourceid) {
		case RESOURCE_ID_UPDATE:
			startFirmwareUpdate();
			return ExecuteResponse.success();
		default:
			return super.execute(resourceid, params);
		}
	}

	private void startFirmwareUpdate() {
		logger.log(Level.INFO, "Starting upgrade.");
		try {
			setState(STATE_UPDATING);
			setUpdateResult(UPDATE_RESULT_INITIAL);

			// Apply firmware upgrade
			Thread.sleep(5000);

			setUpdateResult(UPDATE_RESULT_SUCCESS);
			setState(STATE_IDLE);
			logger.log(Level.INFO, "Upgrade complete.");

		} catch (Exception e) {
			setState(STATE_IDLE);
			setUpdateResult(UPDATE_RESULT_FIRMWARE_UPDATE_FAILED);
			logger.log(Level.INFO, "Upgrade failed.");
		}
	}
	
	private void storePackageFile(String value) {
		byte[] packageData = DatatypeConverter.parseBase64Binary(value);
		String workingDirName = System.getProperty("user.dir");
		File packagesDir = new File(workingDirName + File.separatorChar + "packages");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hhmmss");
		File packageFile = new File(packagesDir, sdf.format(new Date()));
		try {
			FileUtils.writeByteArrayToFile(packageFile, packageData);
		} catch (IOException e) {
			setState(STATE_IDLE);
			setUpdateResult(UPDATE_RESULT_NOT_ENOUGH_FLASH_MEMORY);
			logger.log(Level.INFO, "Firmware update file storage failed.");
		}
	}

	private void startDownload() {
		logger.log(Level.INFO, "Starting download.");

		try {
			setState(STATE_DOWLOADING);
			String workingDirName = System.getProperty("user.dir");
			File packagesDir = new File(workingDirName + File.separatorChar + "packages");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hhmmss");

			File packageFile = new File(packagesDir, sdf.format(new Date()));
			HttpGet request = new HttpGet(this.packageUri);

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = httpClient.execute(request);

			int responseCode = response.getStatusLine().getStatusCode();
			logger.log(Level.FINEST, "[GatewaySDK] Response Code: " + responseCode);

			if (responseCode < 200 || responseCode > 299) {
				String reason = response.getStatusLine().getReasonPhrase();
				logger.log(Level.SEVERE, "Could not download firmware update. Got response code " + responseCode + " " + reason);
				setState(STATE_IDLE);
				setUpdateResult(UPDATE_RESULT_INVALID_URI);
				return;
			}
			
			InputStream is = response.getEntity().getContent();
			FileUtils.copyInputStreamToFile(is, packageFile);
			setState(STATE_DOWNLOADED);
			logger.log(Level.INFO, "Download complete.");
		} catch (IllegalArgumentException iae) {
			setState(STATE_IDLE);
			setUpdateResult(UPDATE_RESULT_INVALID_URI);
			logger.log(Level.INFO, "Download failed.");
		}
		catch (IOException e) {
			setState(STATE_IDLE);
			setUpdateResult(UPDATE_RESULT_CONNECTION_LOST_DURING_DOWNLOAD);
			logger.log(Level.INFO, "Download failed.");
		}
	}

	public void setUpdateResult(int updateResult) {
		this.updateResult = updateResult;
		fireResourcesChange(RESOURCE_ID_UPDATE_RESULT);
		logger.log(Level.INFO, "Update result is " + updateResult);
	}

	public void setState(int state) {
		this.state = state;
		if(state == STATE_DOWNLOADED) {
			this.packageUri = "";
		}
		fireResourcesChange(RESOURCE_ID_STATE);
		logger.log(Level.INFO, "State is " + state);
	}	
}


