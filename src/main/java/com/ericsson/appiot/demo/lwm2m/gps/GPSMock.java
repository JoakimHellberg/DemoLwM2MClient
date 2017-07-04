package com.ericsson.appiot.demo.lwm2m.gps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Dummy {@link GpsSource} implementation to be used for simulating data.
 * @author Joakim Hellberg
 *
 */
public class GPSMock implements Runnable, GpsSource {
	List<GPSReading> readings;

	private boolean stop = false;
	private Thread runningThread;

	private Vector<GPSPipeListener> listeners = new Vector<GPSPipeListener>(8);

	public GPSMock() {	
	}	
	
	public void addListener(GPSPipeListener listener) {
		listeners.add(listener);
	}

	public void removeListener(GPSPipeListener listener) {
		listeners.remove(listener);
	}

	public void run() {
		while (!stop) {
			for (GPSReading reading : readings) {
				if (reading != null) {
					for (int i = 0; i < listeners.size(); i++) {
						if (listeners.elementAt(i) != null) {
							listeners.get(i).onReading(reading);
						}
					}
				}
				try {Thread.sleep(1000);} 
				catch (InterruptedException e) {} // Ignore
			}
		}
	}

	public void stop() {
		stop = true;
		runningThread.interrupt();
	}
	
	public void start() {
		stop = false;
		try {
			InputStream is = GPSMock.class.getClassLoader().getResourceAsStream("mockroute.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}

			String route = result.toString();
			StringTokenizer st = new StringTokenizer(route, " ");
			List<GPSReading> readings = new Vector<GPSReading>();

			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); 
			df.setTimeZone(tz);

			while (st.hasMoreTokens()) {
				String position = st.nextToken();
				String[] coords = position.split(",");
				float lon = Float.parseFloat(coords[0]);
				float lat = Float.parseFloat(coords[1]);

				GPSReading reading = new GPSReading();
				reading.setLon(lon);
				reading.setLat(lat);
				reading.setTime(df.format(new Date()));
				readings.add(reading);
			}

			this.readings = readings;

			runningThread = new Thread(this);
			runningThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
