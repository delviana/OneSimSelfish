/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.estimator;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashSet;
import java.util.Set;
import report.MarkingNode;
import report.Status;
import routing.DecisionEngineRouter;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;
import static routing.estimator.SprayAndWaitRouterWithEstimator.INITIATOR_MARKING;
import static routing.estimator.SprayAndWaitRouterWithEstimator.MARK_COUNT;

/**
 *
 * @author User
 */
public class MarkRecapture implements RoutingDecisionEngineImproved, MarkingNode, Status {

    /**
     * identifier for the initial number of copies setting ({@value})
     */
    public static final String NROF_COPIES = "nrofCopies";
    /**
     * identifier for the binary-mode setting ({@value})
     */
    public static final String BINARY_MODE = "binaryMode";
    /**
     * SprayAndWait router's settings name space ({@value})
     */
    public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouterManoah";
    /**
     * Message property key
     */
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";

    public static final String INITIATOR_MARKING = "nodeInitiatior";
    public static final String MARK_COUNT = "markCount";

    protected int initialNrofCopies;
    protected boolean isBinary;
    private int initiator;
    private int interval;
    private int estimation;
    private Set<DTNHost> markingNode;
    private Set<DTNHost> recapturedNode;
    private int markCount;
    private double lastUpdate = Double.MIN_VALUE;
    private int status;
    private boolean isMark = true;

    public MarkRecapture(Settings s) {

        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            isBinary = this.isBinary;
        }
        if (s.contains(NROF_COPIES)) {
            initialNrofCopies = s.getInt(NROF_COPIES);
        }
        if (s.contains(MARK_COUNT)) {
            markCount = s.getInt(MARK_COUNT);
        }
        if (s.contains(MARK_COUNT)) {
            markCount = s.getInt(MARK_COUNT);
        }
        initiator = s.getInt(INITIATOR_MARKING);
        estimation = 0;
        markingNode = new HashSet<>();
        recapturedNode = new HashSet<>();
    }

    protected MarkRecapture(MarkRecapture r) {
        this.initialNrofCopies = r.initialNrofCopies;
        this.isBinary = r.isBinary;
        this.interval = r.interval;
        this.initiator = r.initiator;
        this.estimation = r.estimation;
        markingNode = new HashSet<>();
        recapturedNode = new HashSet<>();
        this.markCount = r.markCount;
        this.status = 0;

    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        MarkRecapture partner = getOtherEstimatorRouter(peer);
        //double currentTime = SimClock.getTime();
        markCount = 0;
        if (thisHost.isRadioActive() == true && peer.isRadioActive() == true) {
            //  if (markCount > 1) {
            if (isMark && thisHost.getAddress() == initiator) {
                if (!markingNode.contains(peer)) {
                    if (isBinary) {
                        markCount = (int) Math.ceil(markCount / 2.0);
                        this.markingNode.add(peer);
                        this.setMarkCount(getMarkCount() - 1);
                        status = 1;
                        partner.setStatusNode(status);
                    } else {
                        markCount = 1;
                        this.markingNode.add(peer);
                        this.setMarkCount(getMarkCount() - 1);
                        status = 1;
                        partner.setStatusNode(status);
                    }

                    if (peer.getAddress() == initiator) {
                        if (isBinary) {
                            markCount = (int) Math.ceil(markCount / 2.0);
                            status = 1;
                            partner.setStatusNode(status);
                        } else {
                            markCount = 1;
                            status = 1;
                            partner.setStatusNode(status);
                        }
                        /**
                         * Flooding the total estimation that has been gathered
                         */
                        if (this.getEstimation() > partner.getEstimation()) {
                            partner.setEstimation(this.getEstimation());
                        } else {
                            this.setEstimation(partner.getEstimation());
                        }
                    }
                }
            }
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
        if (isMark) {
            m.addProperty(MARK_COUNT, markCount);
            return true;
        }
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return true;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) { //receiver

        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);

        } else {
            nrofCopies = 1;
        }

        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
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
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public void update(DTNHost host) {
        if (host.getAddress() == initiator) {
            if (getMarkCount() < 1) {
                recapture();
            }
            double currentTime = SimClock.getTime();
            if (currentTime - lastUpdate >= interval) {
                recapture();
            }
            this.lastUpdate = currentTime - currentTime % interval;
        }
    }

//    @Override
//    public void transferDone(Connection con) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new MarkRecapture(this);
    }

    private void recapture() {
        Set<DTNHost> newComerHosts = new HashSet<>();
        for (DTNHost h : this.markingNode) {
            if (!recapturedNode.contains(h)) {
                recapturedNode.add(h);
                newComerHosts.add(h);
            }
        }
        int markedNode = recapturedNode.size() - newComerHosts.size();
        /**
         * For the first time recapture phase, the markedNode size always 0
         * because recapturedNode size and newComerHosts size are same
         */
        if (markedNode != 0) {
            int totalEstimation = markCount * recapturedNode.size()
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

    public int getMarkCount() {
        return markCount;
    }

    public void setMarkCount(int markCount) {
        this.markCount = markCount;
    }

    private MarkRecapture getOtherEstimatorRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouterImproved : "This router only works "
                + " with other routers of same type";

        return (MarkRecapture) ((DecisionEngineRouterImproved) otherRouter).getDecisionEngine();
    }

    @Override
    public int getCounting() {
        return this.getEstimation();
    }

    @Override
    public int getStatusNode() {
        return this.status;
    }

    @Override
    public void setStatusNode(int status) {
        this.status = status;
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
