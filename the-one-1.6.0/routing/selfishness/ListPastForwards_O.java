/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.selfishness;

/**
 *
 * @author Jarkom
 */
public class ListPastForwards_O <sID, rID, sComm, rComm> {
    private sID senderID;
    private rID receiverID;
    private sComm senderComm;
    private rComm receiverComm;
    
    public ListPastForwards_O(sID senderID, rID receiverID, sComm senderComm, rComm receiverComm){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.senderComm = senderComm;
        this.receiverComm = receiverComm;
    }
    
    public sID getSenderID(){
        return senderID;
    }
    
    public rID getReceiverID(){
        return receiverID;
    }
    
    public sComm getSenderComm (){
        return senderComm;
    }
    
    public rComm getReceiverComm(){
        return receiverComm;
    }
    
    @Override
    public String toString(){
        return senderID.toString() + ":" + receiverID.toString() + ":" + senderComm.toString() + ":" + receiverComm.toString();
    }
}
