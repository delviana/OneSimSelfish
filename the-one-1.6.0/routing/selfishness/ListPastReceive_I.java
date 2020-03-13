/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.selfishness;

/**
 *
 * @author Jarkom
 * @param <sID>
 * @param <rID>
 * @param <sComm>
 * @param <rComm>
 * @param <time>
 */
public class ListPastReceive_I<sID, rID, sComm, rComm, time> {

    private final sID senderID;
    private final rID receiverID;
    private final sComm senderComm;
    private final rComm receiverComm;
    private final time timeOfContact;

    public ListPastReceive_I(sID senderID, rID receiverID, sComm senderComm, rComm receiverComm, time timeOfContact) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.senderComm = senderComm;
        this.receiverComm = receiverComm;
        this.timeOfContact = timeOfContact;
    }

    public sID getSenderID() {
        return senderID;
    }

    public rID getReceiverID() {
        return receiverID;
    }

    public sComm getSenderComm() {
        return senderComm;
    }

    public rComm getReceiverComm() {
        return receiverComm;
    }
    public time getTimeOfContact() {
        return timeOfContact;
    }

    @Override
    public String toString() {
        return senderID.toString() + ":" + receiverID.toString() + ":" + senderComm.toString() + ":" + receiverComm.toString()
                + ":" + timeOfContact.toString();
    }
}
