package com.ericsson.appiot.simplelwm2mclient.lwm2m.telemetry;

import java.util.List;
import java.util.Vector;

import com.google.gson.annotations.SerializedName;

public class TelemetryData {

	@SerializedName("eids")
	private List<Endpoint> endpointIds = new Vector<Endpoint>();

	
	public TelemetryData() {
	}

	public void clear() {
		endpointIds.clear();
	}
	
	public Endpoint getEndpoint(String endpointId) {
		Endpoint result = null;
		for(Endpoint e : endpointIds) {
			if(e.endpointId == endpointId) {
				result = e;
				break;
			}
		}
		
		if(result == null) {
			result = new Endpoint(endpointId);
			endpointIds.add(result);
		}
		return result;
	}
	
	
	public void addMeasurement(String endpointId, int objectId, int instanceId, int resourceId, long timestamp, double value) {
		getEndpoint(endpointId).getObject(objectId).getInstance(instanceId).addMeasurement(timestamp, value);
	}

	private class Endpoint {

		@SerializedName("eid")
		private String endpointId;
		
		@SerializedName("oids")
		private List<Obj> objects = new Vector<Obj>();
		
		public Endpoint(String endpointId) {
			this.endpointId = endpointId;
		}
		
		public Obj getObject(int objectId) {

			Obj result = null;
			for(Obj o : objects) {
				if(o.objectId == objectId) {
					result = o;
					break;
				}
			}
			
			if(result == null) {
				result = new Obj(objectId);
				objects.add(result);
			}
			return result;
		}		
	}
	
	private class Obj {
		
		@SerializedName("oid")
		private int objectId;
		
		@SerializedName("iids")
		private List<Instance> instances = new Vector<Instance>();

		public Obj(int objectId) {
			this.objectId = objectId;
		}
		
		public Instance getInstance(int instanceId) {
			Instance result = null;
			for(Instance i : instances) {
				if(i.instanceId == instanceId) {
					result = i;
					break;
				}
			}
			
			if(result == null) {
				result = new Instance(instanceId);
				instances.add(result);
			}
			return result;
		}	
	
	} 
	
	private class Instance {
		@SerializedName("iid")
		private int instanceId;
		@SerializedName("m")
		private List<Measurement> measurements = new Vector<Measurement>();
		
		public Instance (int instanceId) {
			this.instanceId = instanceId;
		}
		
		public void addMeasurement(long timestamp, double value) {
			measurements.add(new Measurement(timestamp, value));
		}
		
	}
	
	private class Measurement {
		@SerializedName("t")
		private long timestamp;
		@SerializedName("v")
		private double value;
		
		public Measurement(long timestamp, double value) {
			this.timestamp = timestamp;
			this.value = value;
		}
	}
	
	
}
