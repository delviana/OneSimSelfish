/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A markMessage that is created at a node or passed between nodes.
 */
public class MarkMessage implements Comparable<MarkMessage> {
	private DTNHost from;
	private DTNHost to;
	/** Identifier of the markMessage */
	private String id;
	/** Size of the markMessage (bytes) */
	private int size;
	/** List of nodes this markMessage has passed */
	private List<DTNHost> path; 
	/** Next unique identifier to be given */
	private static int nextUniqueId;
	/** Unique ID of this markMessage */
	private int uniqueId;
	/** The time this markMessage was received */
	private double timeReceived;
	/** The time when this markMessage was created */
	private double timeCreated;
	
	/** if a response to this markMessage is required, this is the size of the 
	 * response markMessage (or 0 if no response is requested) */
	private int responseSize;
	/** if this markMessage is a response markMessage, this is set to the request msg*/
	private MarkMessage requestMsg;
	
	/** Container for generic markMessage properties. Note that all values
	 * stored in the properties should be immutable because only a shallow
	 * copy of the properties is made when replicating markMessages */
	private Map<String, Object> properties;
	
	/** Application ID of the application that created the markMessage */
	private String	appID;
	
	static {
		reset();
		DTNSim.registerForReset(MarkMessage.class.getCanonicalName());
	}
	
	/**
	 * Creates a new Message.
	 * @param from Who the markMessage is (originally) from
	 * @param to Who the markMessage is (originally) to
	 * @param id Message identifier (must be unique for markMessage but
	 * 	will be the same for all replicates of the markMessage)
	 * @param size Size of the markMessage (in bytes)
	 */
	public MarkMessage(DTNHost from, DTNHost to, String id, int size) {
		this.from = from;
		this.to = to;
		this.id = id;
		this.size = size;
		this.path = new ArrayList<DTNHost>();
		this.uniqueId = nextUniqueId;
		
		this.timeCreated = SimClock.getTime();
		this.timeReceived = this.timeCreated;
		this.responseSize = 0;
		this.requestMsg = null;
		this.properties = null;
		this.appID = null;
		
		MarkMessage.nextUniqueId++;
		addNodeOnPath(from);
	}
	
	/**
	 * Returns the node this markMessage is originally from
	 * @return the node this markMessage is originally from
	 */
	public DTNHost getFrom() {
		return this.from;
	}

	/**
	 * Returns the node this markMessage is originally to
	 * @return the node this markMessage is originally to
	 */
	public DTNHost getTo() {
		return this.to;
	}

	/**
	 * Returns the ID of the markMessage
	 * @return The markMessage id
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Returns an ID that is unique per markMessage instance 
	 * (different for replicates too)
	 * @return The unique id
	 */
	public int getUniqueId() {
		return this.uniqueId;
	}
	
	/**
	 * Returns the size of the markMessage (in bytes)
	 * @return the size of the markMessage
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Adds a new node on the list of nodes this markMessage has passed
	 * @param node The node to add
	 */
	public void addNodeOnPath(DTNHost node) {
		this.path.add(node);
	}
	
	/**
	 * Returns a list of nodes this markMessage has passed so far
	 * @return The list as vector
	 */
	public List<DTNHost> getHops() {
		return this.path;
	}
	
	/**
	 * Returns the amount of hops this markMessage has passed
	 * @return the amount of hops this markMessage has passed
	 */
	public int getHopCount() {
		return this.path.size() -1;
	}

	public void setReceiveTime(double time) {
		this.timeReceived = time;
	}
	
	/**
	 * Returns the time when this markMessage was received
	 * @return The time
	 */
	public double getReceiveTime() {
		return this.timeReceived;
	}
	
	/**
	 * Returns the time when this markMessage was created
	 * @return the time when this markMessage was created
	 */
	public double getCreationTime() {
		return this.timeCreated;
	}
	
	/**
	 * If this markMessage is a response to a request, sets the request markMessage
	 * @param request The request markMessage
	 */
	public void setRequest(MarkMessage request) {
		this.requestMsg = request;
	}
	
	/**
	 * Returns the markMessage this markMessage is response to or null if this is not
	 * a response markMessage
	 * @return the markMessage this markMessage is response to
	 */
	public MarkMessage getRequest() {
		return this.requestMsg;
	}
	
	/**
	 * Returns true if this markMessage is a response markMessage
	 * @return true if this markMessage is a response markMessage
	 */
	public boolean isResponse() {
		return this.requestMsg != null;
	}
	
	/**
	 * Sets the requested response markMessage's size. If size == 0, no response
	 * is requested (default)
	 * @param size Size of the response markMessage
	 */
	public void setResponseSize(int size) {
		this.responseSize = size;
	}
	
	/**
	 * Returns the size of the requested response markMessage or 0 if no response
	 * is requested.
	 * @return the size of the requested response markMessage
	 */
	public int getResponseSize() {
		return responseSize;
	}
	
	/**
	 * Returns a string representation of the markMessage
	 * @return a string representation of the markMessage
	 */
	public String toString () {
		return id;
	}

	/**
	 * Deep copies markMessage data from other markMessage. If new fields are
	 * introduced to this class, most likely they should be copied here too
	 * (unless done in constructor).
	 * @param m The markMessage where the data is copied
	 */
	protected void copyFrom(MarkMessage m) {
		this.path = new ArrayList<DTNHost>(m.path);
		this.timeCreated = m.timeCreated;
		this.responseSize = m.responseSize;
		this.requestMsg  = m.requestMsg;
		this.appID = m.appID;
		
		if (m.properties != null) {
			Set<String> keys = m.properties.keySet();
			for (String key : keys) {
				updateProperty(key, m.getProperty(key));
			}
		}
	}
	
	/**
	 * Adds a generic property for this markMessage. The key can be any string but 
	 * it should be such that no other class accidently uses the same value.
	 * The value can be any object but it's good idea to store only immutable
	 * objects because when markMessage is replicated, only a shallow copy of the
	 * properties is made.  
	 * @param key The key which is used to lookup the value
	 * @param value The value to store
	 * @throws SimError if the markMessage already has a value for the given key
	 */
	public void addProperty(String key, Object value) throws SimError {
		if (this.properties != null && this.properties.containsKey(key)) {
			/* check to prevent accidental name space collisions */
			throw new SimError("Message " + this + " already contains value " + 
					"for a key " + key);
		}
		
		this.updateProperty(key, value);
	}
	
	/**
	 * Returns an object that was stored to this markMessage using the given
	 * key. If such object is not found, null is returned.
	 * @param key The key used to lookup the object
	 * @return The stored object or null if it isn't found
	 */
	public Object getProperty(String key) {
		if (this.properties == null) {
			return null;
		}
		return this.properties.get(key);
	}
	
	/**
	 * Updates a value for an existing property. For storing the value first 
	 * time, {@link #addProperty(String, Object)} should be used which
	 * checks for name space clashes.
	 * @param key The key which is used to lookup the value
	 * @param value The new value to store
	 */
	public void updateProperty(String key, Object value) throws SimError {
		if (this.properties == null) {
			/* lazy creation to prevent performance overhead for classes
			   that don't use the property feature  */
			this.properties = new HashMap<String, Object>();
		}		

		this.properties.put(key, value);
	}
	
	/**
	 * Returns a replicate of this markMessage (identical except for the unique id)
	 * @return A replicate of the markMessage
	 */
	public MarkMessage replicate() {
		MarkMessage m = new MarkMessage(from, to, id, size);
		m.copyFrom(this);
		return m;
	}
	
	/**
	 * Compares two markMessages by their ID (alphabetically).
	 * @see String#compareTo(String)
	 */
	public int compareTo(MarkMessage m) {
		return toString().compareTo(m.toString());
	}
	
	/**
	 * Resets all static fields to default values
	 */
	public static void reset() {
		nextUniqueId = 0;
	}

	/**
	 * @return the appID
	 */
	public String getAppID() {
		return appID;
	}

	/**
	 * @param appID the appID to set
	 */
	public void setAppID(String appID) {
		this.appID = appID;
	}
	
}
