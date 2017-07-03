package com.ericsson.appiot.lwm2m.smartobject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class MyFirmwareUpgrade extends BaseInstanceEnabler {
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

	private byte[] packageData;
	private String packageUri;
	private int state = 0;
	private int updateResult;
	private String packageName = "New package";
	private String packageVersion = "1.0";
	private int protocolSupport = 3;
	private int deliveryMethod = 0;

	public MyFirmwareUpgrade() {
	}

	@Override
	public WriteResponse write(int resourceid, LwM2mResource value) {
		logger.info("Write on Text Resource " + resourceid + " value: " + value.getValue().toString());
		switch (resourceid) {
		case RESOURCE_ID_PACKAGE:
			return WriteResponse.notFound();
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
		switch (resourceid) {
		case RESOURCE_ID_UPDATE:
			startFirmwareUpdate();
			return ExecuteResponse.success();
		default:
			return super.execute(resourceid, params);
		}
	}

	private void startFirmwareUpdate() {
		try {
			setState(STATE_UPDATING);
			setUpdateResult(UPDATE_RESULT_INITIAL);

			Thread.sleep(5000);

			setUpdateResult(UPDATE_RESULT_SUCCESS);
		} catch (Exception e) {
			setState(STATE_IDLE);
			setUpdateResult(UPDATE_RESULT_FIRMWARE_UPDATE_FAILED);
		}
	}

	private void startDownload() {
		try {
			setState(STATE_DOWLOADING);
			String workingDirName = System.getProperty("user.dir");
			File packagesDir = new File(workingDirName + File.pathSeparator + "packages");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:ss");

			File packageFile = new File(packagesDir, sdf.format(new Date()));
			//String contentType = "text/xml";
			HttpGet request = new HttpGet(URLEncoder.encode(this.packageUri));
			// request.addHeader("Authorization", authorizationHeader);
			// request.addHeader("Content-Type", contentType);

			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpResponse response = httpClient.execute(request);

			int responseCode = response.getStatusLine().getStatusCode();
			logger.log(Level.FINEST, "[GatewaySDK] Response Code: " + responseCode);

			if (responseCode < 200 || responseCode > 299) {
				String reason = response.getStatusLine().getReasonPhrase();
				setState(STATE_IDLE);
				setUpdateResult(UPDATE_RESULT_INVALID_URI);
			}

			InputStream is = response.getEntity().getContent();
			FileUtils.copyInputStreamToFile(is, packageFile);
			setState(STATE_DOWNLOADED);

		} catch (Exception e) {
			setState(STATE_IDLE);
			setUpdateResult(UPDATE_RESULT_CONNECTION_LOST_DURING_DOWNLOAD);
		}
	}


	@Override
	public ReadResponse read(int resourceid) {
		logger.finest("Read on Text Resource " + resourceid);
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

	public void setUpdateResult(int updateResult) {
		this.updateResult = updateResult;
		fireResourcesChange(RESOURCE_ID_UPDATE_RESULT);
	}

	public void setState(int state) {
		this.state = state;
		fireResourcesChange(RESOURCE_ID_STATE);
	}

}