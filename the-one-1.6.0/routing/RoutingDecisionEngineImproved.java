/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;

/**
 *
 * @author Gregorius Bima, Sanata Dharma Univeristy
 */
public interface RoutingDecisionEngineImproved extends RoutingDecisionEngine{
    
    /**
     * Called when update time,
     * needed if necessary
     * 
     * @param host the updated host
     */
    public void update(DTNHost host);
    
    /**
     * Called after the message being sent to other host,
     * needed if necessary
     * 
     * @param con 
     */
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost);
   
}
