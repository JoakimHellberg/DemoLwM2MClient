package com.ericsson.appiot.demo.lwm2m;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class ButtonLedController implements Runnable {

	private GpioPinDigitalOutput ledPin;
	private GpioPinDigitalInput buttonPin;

	private Thread t;
	private boolean stop = false;
	private boolean registered = false;
	private boolean running = false;
	
	private ButtonPressedListener listener = null;
	private int flashTime;
	private int blackTime;
	
	public ButtonLedController() {
		// Define GPIO pins for button and led.
		GpioController gpio = GpioFactory.getInstance();
		this.ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29);
		
		// If button is pressed, startup deployment application manager.
		// This result in a bluetooth socket to be established.
		this.buttonPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, PinPullResistance.PULL_UP);
		buttonPin.addListener(new GpioPinListenerDigital() {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            	if(event.getState().isHigh()) {
            		if(listener != null) {
            			listener.onPressed();
            		}
            	} 
            }
        });	
		
	}
	
	public void setListener(ButtonPressedListener listener) {
		this.listener = listener;
	}

	private void start() {
		if(!running) {
			stop = false;
			t = new Thread(this);
			t.start();
			ledPin.low();
		}
	}

	private void stop() {
		running = false;
		stop = true;
		ledPin.low();
	}
	
	public void turnOnLed() {
		stop();
		ledPin.high();
	}
	
	public void turnOffLed() {
		stop();
		ledPin.low();
	}
	
	public void flashLed(int flashTime, int blackTime) {
		this.flashTime = flashTime;
		this.blackTime = blackTime;
		
		start();
	}
	
	public void stopFlash() {
		stop();
	}
	
	

	public void run() {
		running = true;
		while(!stop) {
			try {				
				ledPin.high();
				Thread.sleep(flashTime);
				if(!registered) {
					ledPin.low();
					Thread.sleep(blackTime);
				}
			} catch (InterruptedException e) {
			}
		}
		running = false;
	}
}
