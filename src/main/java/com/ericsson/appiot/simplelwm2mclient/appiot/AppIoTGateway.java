package com.ericsson.appiot.simplelwm2mclient.appiot;

import se.sigma.sensation.gateway.sdk.client.SensationClient;

import com.ericsson.appiot.simplelwm2mclient.lwm2m.MyTemperature;
import com.google.gson.Gson;

import se.sigma.sensation.dto.RegistrationTicket;

public class AppIoTGateway implements Runnable {

	private SensationClient client;
	private boolean stop = false;
	private Thread t;
	private String registrationTicket;
	private MyTemperature temperature;
	
	public AppIoTGateway(String registrationTicket, MyTemperature temperature) {
		this.registrationTicket = registrationTicket;
		this.temperature = temperature;
	}
	
	public void start() {
		stop = false;
		t = new Thread(this);
		t.start();
	}	
	
	public void stop() {
		stop = true;
		t.interrupt();
	}
		
	public void run() {

		
		
		
		
		RegistrationTicket ticket = new Gson().fromJson(registrationTicket, RegistrationTicket.class);		
		MyPlatform platform = new MyPlatform(temperature);
		this.client = new SensationClient(ticket, platform);
		
		//this.client.register(ticket);

		client.start();
		
		while(!stop) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {
				
			}
		}
	}


}
