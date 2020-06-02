/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.bubleRapSelfish;

import core.DTNHost;
import routing.community.CommunityDetection;

/**
 *
 * @author Jarkom
 */
public class ListPastForwards_O {

    private final DTNHost senderID;
    private final DTNHost receiverID;
    private final CommunityDetection senderComm;
    private final CommunityDetection receiverComm;
    private final Double timeOfContact;

    public DTNHost getSenderID() {
        return senderID;
    }

    public DTNHost getReceiverID() {
        return receiverID;
    }

    public CommunityDetection getSenderComm() {
        return senderComm;
    }

    public CommunityDetection getReceiverComm() {
        return receiverComm;
    }

    public Double getTimeOfContact() {
        return timeOfContact;
    }

    public ListPastForwards_O(DTNHost senderID, DTNHost receiverID, CommunityDetection senderComm, CommunityDetection receiverComm, Double timeOfContact) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.senderComm = senderComm;
        this.receiverComm = receiverComm;
        this.timeOfContact = timeOfContact;
    }

    @Override
    public String toString() {
        return senderID.toString() + ":" + receiverID.toString() + ":" + senderComm.toString() + ":" + receiverComm.toString()
                + ":" + timeOfContact.toString();
    }
}
