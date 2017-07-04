package com.ericsson.appiot.lwm2m.gps;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

public class GPSReading {
	private float lat;
	private float lon;
	private float alt;
	private float epx;
	private float epy;
	private float speed;
	private float climb;
	private String time;
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public float getLon() {
		return lon;
	}
	public void setLon(float lon) {
		this.lon = lon;
	}
	public float getAlt() {
		return alt;
	}
	public void setAlt(float alt) {
		this.alt = alt;
	}
	public float getEpx() {
		return epx;
	}
	public void setEpx(float epx) {
		this.epx = epx;
	}
	public float getEpy() {
		return epy;
	}
	public void setEpy(float epy) {
		this.epy = epy;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public float getClimb() {
		return climb;
	}
	public void setClimb(float climb) {
		this.climb = climb;
	}
	public Date getTime() {
		Calendar cal = DatatypeConverter.parseDateTime(time);
		return cal.getTime();
	}
	public void setTime(String time) {
		this.time = time;
	}

	
}
