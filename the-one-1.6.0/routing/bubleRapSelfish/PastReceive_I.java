/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.bubleRapSelfish;

import core.DTNHost;
import java.util.List;
import java.util.Set;
import routing.community.Duration;

/**
 *
 * @author Jarkom
 */
public interface PastReceive_I {
    

/**
	 * Called to inform the object that a new connection was made. 
	 * 
	 * @param myHost Host to which this CommunityDetection object belongs
	 * @param peer Host that connected to this host
	 * @param peerPast_I Instance of CommunityDetection residing at the new peer 
	 */

public void newConnection(DTNHost myHost, DTNHost peer, PastReceive_I peerPast_I);

/**
	 * Called to inform the object that a connection was lost.
	 * 
	 * @param myHost Host to which this CommunityDetection object belongs
	 * @param peer Host that is now disconnected from this object
	 * @param peerPast_I Instance of CommunityDetection residing at the lost peer
	 * @param connHistory Entire connection history between this host and the peer
	 */

public void ConnectionLost(DTNHost myHost, DTNHost peer, PastReceive_I peerPast_I, List<Duration> connHistory);

/**
	 * Determines if the given host is a member of the local community of this 
	 * object. 
	 * 
	 * @param h Host to consider
	 * @return true if h is a member of the community, false otherwise
	 */

public boolean isHostInCommunity(DTNHost h);

/**
	 * Returns a set of hosts that are members of the local community of this 
	 * object. This method is really only provided for {@link 
	 * report.CommunityDetectionReport} to use.
	 * 
	 * @return the Set representation of the local community
	 */

public Set<DTNHost> getLocalCommunity();

/**
	 * Duplicates this CommunityDetection object.
	 * 
	 * @return A semantically equal copy of this CommunityDetection object
	 */

public PastReceive_I replicate();

}


