/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.estimator;

import core.*;
import java.util.*;
import report.MarkResidu;
import report.MarkingNode;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;
import static routing.estimator.SprayAndWaitRouterWithEstimator.MSG_COUNT_PROPERTY;

/**
 *
 * @author Gregorius Bima, Sanata Dharma University
 */
public class MarkAndRecaptureRouter implements RoutingDecisionEngineImproved, MarkResidu, MarkingNode {

    public static final String MSG_COUNT_PROPERTY = "copies";
    public static final String MARKING_FLAG = "nrofFlag";
    public static final String BINARY_MODE = "binaryMode";
    public static final String RECAPTURE_INTERVAL = "recaptureInterval";
    public static final String INITIATOR_MARKING = "initiator";
    public static final int DEFAULT_INTERVAL = 3600;
    public static final int DEFAULT_MARK_FLAG = 20;
    public static final boolean DEFAULT_MODE = true;

    private double lastUpdate = Double.MIN_VALUE;
    private boolean isBinary;
    private boolean isMarking;
    private int initialMarkFlag;
    private int initiator;
    private int intervalRecapture;
    private int estimation;
    private Map<Integer, Map<Integer, Integer>> flagCollections;
    private Set<DTNHost> markingNode;
    private Set<DTNHost> recapturedNode;

    public MarkAndRecaptureRouter(Settings s) {
        if (s.contains(MARKING_FLAG)) {
            initialMarkFlag = s.getInt(MARKING_FLAG);
        } else {
            initialMarkFlag = DEFAULT_MARK_FLAG;
        }
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            isBinary = DEFAULT_MODE;
        }

        if (s.contains(RECAPTURE_INTERVAL)) {
            intervalRecapture = s.getInt(RECAPTURE_INTERVAL);
        } else {
            intervalRecapture = DEFAULT_INTERVAL;
        }
        if (s.contains(INITIATOR_MARKING)) {
            initiator = s.getInt(INITIATOR_MARKING);
        }

        this.isMarking = true;
        estimation = 0;
        flagCollections = new HashMap<>();
        markingNode = new HashSet<>();
        recapturedNode = new HashSet<>();
    }

    public MarkAndRecaptureRouter(MarkAndRecaptureRouter prototype) {
        this.initialMarkFlag = prototype.initialMarkFlag;
        this.isBinary = prototype.isBinary;
        this.intervalRecapture = prototype.intervalRecapture;
        this.initiator = prototype.initiator;
        this.estimation = prototype.estimation;
        flagCollections = new HashMap<>();
        markingNode = new HashSet<>();
        recapturedNode = new HashSet<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        if (isMarking) {
            markingPhase(thisHost, peer);
        } else {
            recapture(peer);
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        m.addProperty(MSG_COUNT_PROPERTY, new Integer(getEstimation()));
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            nrofCopies = 1;
        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
           if (m.getTo() == otherHost) {
            return true;
        }

        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (nrofCopies > 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return false;
    }

    private MarkAndRecaptureRouter getOtherEstimatorRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouterImproved : "This router only works "
                + " with other routers of same type";

        return (MarkAndRecaptureRouter) ((DecisionEngineRouterImproved) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        double currentTime = SimClock.getTime();
        if (currentTime - lastUpdate < intervalRecapture) {
            this.lastUpdate = currentTime - currentTime % intervalRecapture;
        }
    }

//    @Override
//    public void transferDone(Connection con) {
//           Message m = con.getMessage();
//
//        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
//
//        if (isBinary) {
//            nrofCopies /= 2;
//        } else {
//            nrofCopies--;
//        }
//
//        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
//    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new MarkAndRecaptureRouter(this);
    }

    private void markingPhase(DTNHost thisHost, DTNHost peer) {
        if (isHostInitiator(thisHost)) {
            if (isBinary) {
                Integer myRemainingFlag = this.getInitialMarkFlag() / 2;
                this.setInitialMarkFlag(myRemainingFlag);
            } else {
                this.setInitialMarkFlag(this.getInitialMarkFlag() - 1); //source mark
            }

            if (!(this.getInitialMarkFlag() > 1)) {
                isMarking = false;
            }
        } else {
               Map<Integer, Integer> flagFromOtherCollections = this.getFlagFromOtherCollections(initiator); //node ini membaca isi collection flag dari peer 
           
               if (isBinary) {         
                Integer myRemainingFlag = (int) Math.ceil(this.getInitialMarkFlag() / 2); // this.getInitial harusnya bukan
                this.setInitialMarkFlag(myRemainingFlag); // menjadikan mark node ini menjadi setengah      
                flagFromOtherCollections.put(peer.getAddress(), this.getEstimation()); //node ini memasukan id node peer dan nilai estimasinya
                this.flagCollections.put(initiator, flagFromOtherCollections); // 
           
            } else {
               
                this.setInitialMarkFlag(this.getInitialMarkFlag() - 1);       
                flagFromOtherCollections.put(peer.getAddress(), this.getEstimation()); //node ini memasukan id node peer dan nilai estimasinya
                this.flagCollections.put(initialMarkFlag, flagFromOtherCollections); //
            }
        }
    }

    private void recapture(DTNHost host) {
        Set<DTNHost> newComerHosts = new HashSet<>();
           if (!recapturedNode.contains(initialMarkFlag)) {
                recapturedNode.add(host);
                newComerHosts.add(host);
            }
        int markedNode = recapturedNode.size() - newComerHosts.size();
        /**
         * For the first time recapture phase, the markedNode size always 0
         * because recapturedNode size and newComerHosts size are same
         */
       if (markedNode != 0) {
            int totalEstimation = initialMarkFlag * recapturedNode.size()
                    / markedNode;
            setEstimation(totalEstimation);
        } else {
            setEstimation(recapturedNode.size());
        }
        this.recapturedNode.clear();
    }

    private int getEstimation() {
        return estimation;
    }

    private void setEstimation(int estimation) {
        this.estimation = estimation;
    }

    private int getInitialMarkFlag() {
        return initialMarkFlag;
    }

    private void setInitialMarkFlag(int initialMarkFlag) {
        this.initialMarkFlag = initialMarkFlag;
    }

    private Map<Integer, Integer> getFlagFromOtherCollections(Integer idHost) {
        if (flagCollections.containsKey(idHost)) {
            return flagCollections.get(idHost);
        } else {
            Map<Integer, Integer> flagFromOtherCollections = new HashMap<>();
            return flagFromOtherCollections;
        }
    }

    private boolean isHostInitiator(DTNHost thisHost) {
        return thisHost.getAddress() == initiator;
    }

    @Override
    public int getResidu() {
        return initialMarkFlag;
    }

    @Override
    public int getCounting() {
        return this.getEstimation();
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
