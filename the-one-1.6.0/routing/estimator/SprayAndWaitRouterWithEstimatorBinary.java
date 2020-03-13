/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, c hoose Tools | Templates
 * and open the template in the editor.
 */
package routing.estimator;

import core.*;
import java.util.*;
import report.MarkingNode;
import report.Status;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;
import static routing.estimator.MarkCopy.MARK_COUNT;
import static routing.estimator.SprayAndWaitRouterWithEstimator.DEFAULT_INTERVAL;
import static routing.estimator.SprayAndWaitRouterWithEstimator.RECAPTURE_INTERVAL;

/**
 *
 * @author
 */
public class SprayAndWaitRouterWithEstimatorBinary implements RoutingDecisionEngineImproved, MarkingNode, Status {

    public static final String MSG_COUNT_PROPERTY = "copies";
    public static final String BINARY_MODE = "binaryMode";
    public static final String RECAPTURE_INTERVAL = "recaptureInterval";
    public static final String INITIATOR_MARKING = "nodeInitiatior";
    public static final String CONVERGENCE_INTERVAL = "convergenInterval";
    public static final String MARK_COUNT = "markCount";
    private double lastUpdate = Double.MIN_VALUE;
    private int initiator;
    private int interval;
    private int estimation;
    private boolean isBinary;
    private Set<DTNHost> markingNode;
    private Set<DTNHost> recapturedNode;
    private int markCount;
    private int status;

    public static final int DEFAULT_INTERVAL = 36000;
    private static double convergenceTime = 0;

    public SprayAndWaitRouterWithEstimatorBinary(Settings s) {
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            isBinary = true;
        }
        if (s.contains(MARK_COUNT)) {
            markCount = s.getInt(MARK_COUNT);
        }
        if (s.contains(RECAPTURE_INTERVAL)) {
            interval = s.getInt(RECAPTURE_INTERVAL);
        } else {
            interval = DEFAULT_INTERVAL;
        }
        initiator = s.getInt(INITIATOR_MARKING);
        estimation = 0;
        markingNode = new HashSet<>();
        recapturedNode = new HashSet<>();

    }

    public SprayAndWaitRouterWithEstimatorBinary(SprayAndWaitRouterWithEstimatorBinary prototype) {
        this.interval = prototype.interval;
        this.isBinary = prototype.isBinary;
        this.initiator = prototype.initiator;
        this.estimation = prototype.estimation;
        markingNode = new HashSet<>();
        recapturedNode = new HashSet<>();
        this.markCount = prototype.markCount;
        this.status = 0;
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        SprayAndWaitRouterWithEstimatorBinary partner = getOtherEstimatorRouter(peer);
        if (thisHost.isRadioActive() == true && peer.isRadioActive() == true) {
            if (thisHost.getAddress() == initiator) {
                if (isBinary) {
                    if (!markingNode.contains(peer)) {
                        this.markingNode.add(peer);
                        markCount = (int) Math.ceil(markCount / 2);
                        // this.setMarkCount(getMarkCount() / 2);
                        status = 1;
                        partner.setStatusNode(status);
                        /**
                         * Exchange total node information from node initiator
                         * (thisHost)
                         */
                        partner.setEstimation(this.getEstimation());

                    } else if (peer.getAddress() == initiator) {
                        if (isBinary) {
                            if (!markingNode.contains(thisHost)) {
                                partner.markingNode.add(thisHost);
                                markCount = (int) Math.ceil(markCount / 2);
                                // this.setMarkCount(getMarkCount() / 2);
                                status = 1;
                                this.setStatusNode(status);
                                /**
                                 * Exchange total node information from node
                                 * initiator (peer)
                                 */
                                this.setEstimation(partner.getEstimation());

                            } else {
                                /**
                                 * Flooding the total estimation that has been
                                 * gathered
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
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer
    ) {

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer
    ) {

    }

    @Override
    public boolean newMessage(Message m
    ) {
        m.addProperty(MSG_COUNT_PROPERTY, new Integer(getEstimation()));
        return false;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost
    ) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost
    ) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (isBinary) {
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            nrofCopies = 1;
        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost
    ) {
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
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld
    ) {
        return false;
    }

    @Override
    public void update(DTNHost host
    ) {
        double currentTime = SimClock.getTime();
        if (host.getAddress() == initiator) {
            if (getMarkCount() < 1) {
                recapture();

            } else if (currentTime - lastUpdate >= interval) {
                if (host.getAddress() == initiator) {
                    recapture();
                    this.recapturedNode.clear();
                }
                this.lastUpdate = currentTime - currentTime % interval;
            }
        }
    }

//    @Override
//    public void transferDone(Connection con
//    ) {
//        Message m = con.getMessage();
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

    private SprayAndWaitRouterWithEstimatorBinary getOtherEstimatorRouter(DTNHost host) {
        MessageRouter otherRouter = host.getRouter();
        assert otherRouter instanceof DecisionEngineRouterImproved : "This router only works "
                + " with other routers of same type";

        return (SprayAndWaitRouterWithEstimatorBinary) ((DecisionEngineRouterImproved) otherRouter).getDecisionEngine();
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new SprayAndWaitRouterWithEstimatorBinary(this);
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

    private int getMarkCount() {
        return markCount;
    }

    private void setMarkCount(int markCount) {
        this.markCount = markCount;
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
