/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.estimator;

import routing.MessageRouter;
import core.*;
import java.util.*;
import report.MarkResidu;
import report.MarkingNode;
import report.NodeInitiator;
import routing.DecisionEngineRouterImproved;
import routing.RoutingDecisionEngineImproved;

/**
 *
 * @author vander
 */
public class SprayandWaitDE implements RoutingDecisionEngineImproved, MarkResidu, MarkingNode, NodeInitiator {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String MARK_PREFIX = "markPrefix";
    public static final String NROF_MARK = "nrofMark";
    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "SprayandWaitDE";
    public static final String RECAPTURE_INTERVAL = "recaptureInterval";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";
    public static final String MSG_MARK_PROPERTY = SPRAYANDWAIT_NS + "."
            + "copies";
    public static final int DEFAULT_INTERVAL = 3600;
    private double lastUpdate = Double.MIN_VALUE;

    public int initialNrofCopies;
    public int initialNrofMark;
    public boolean isBinary;
    protected boolean nodeMark;
    protected String markPrefix;
    private int estimation;
    private int interval;
    private int mark;
    private Set<DTNHost> markNode;
    private Set<DTNHost> recapturedNode;
    private Map<DTNHost, ArrayList<DTNHost>> markMessage;

    private double time = 1.0;

    public SprayandWaitDE(Settings s) {
        if (s.contains(BINARY_MODE)) {
            isBinary = s.getBoolean(BINARY_MODE);
        } else {
            this.isBinary = false;
        }
        if (s.contains(NROF_COPIES)) {
            initialNrofCopies = s.getInt(NROF_COPIES);
        }
        if (s.contains(NROF_MARK)) {
            initialNrofMark = s.getInt(NROF_MARK);
        }
        if (s.contains(RECAPTURE_INTERVAL)) {
            interval = s.getInt(RECAPTURE_INTERVAL);
        } else {
            interval = DEFAULT_INTERVAL;
        }
        this.markPrefix = s.getSetting(MARK_PREFIX);
        this.nodeMark = false;
        this.mark = 0;
        this.estimation = 0;
    }

    public SprayandWaitDE(SprayandWaitDE r) {
        this.initialNrofCopies = r.initialNrofCopies;
        this.initialNrofMark = r.initialNrofMark;
        this.isBinary = r.isBinary;
        this.nodeMark = r.nodeMark;
        this.mark = r.mark;
        this.markPrefix = r.markPrefix;
        this.markNode = new HashSet<DTNHost>();
        this.recapturedNode = new HashSet<DTNHost>();
        this.interval = r.interval;
        this.estimation = r.estimation;
        this.markMessage = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {

//        if (peer.getRouter().hasMessage("MARK1")) {
//            for (Message m : peer.getMessageCollection()) {
//                Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
//                if (!(nrofMark > 1)) {
//                    if (m.getId().equals("MARK1")) {
//                        if (thisHost == m.getFrom()) {
//                            this.recapturedNode.add(peer);
//                            this.markNode.add(peer);
//                        }
//                    }
//                }
//            }
//        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
//        DTNHost thisHost = con.getOtherNode(peer);
//        SprayandWaitDE pr = getOtherSnFDecisionEngine(peer);
//        for (Message m : thisHost.getMessageCollection()) {
//            if (m.getId().equals("MARK1")) {
//                if (thisHost.equals(m.getFrom())) {
//                    pr.estimation = this.estimation;
//                }
//            }
//        }
//        for (Message m : peer.getMessageCollection()) {
//            if (m.getId().equals("MARK1")) {
//                if (peer.equals(m.getFrom())) {
//                    this.estimation = pr.estimation;
//                }
//            }
//        }

//        if (this.estimation != 0) {
//            pr.estimation = this.estimation;
//        } else {
//            this.estimation = pr.estimation;
//        }
//        if (thisHost.isRadioActive() == true && peer.isRadioActive() == true) {
//                if (this.getCounting()+ pr.getCounting()!= 0) {
//                    update(thisHost);
//                    this.setMark(estimation);
//                    pr.setEstimation(estimation);
//
//                    if (this.getEstimation() > pr.getEstimation()) {
//                        pr.setEstimation(this.getEstimation());
//                    } else {
//                        this.setEstimation(pr.getEstimation());
//                    }
//                }
//            }
    }

    @Override
    public boolean newMessage(Message m) {
        if (m.getPrefix().equals(markPrefix)) {
            m.addProperty(MSG_MARK_PROPERTY, initialNrofCopies);
        } else {
            m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        }
        String messagePrefix = m.getPrefix();
//        System.out.println(m + " has prefix = " + messagePrefix);
        //System.out.println("Message : " + m.getId());
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
        if (isBinary) {

            if (m.getPrefix().equals(markPrefix)) {
                nrofMark = (int) Math.ceil(nrofMark / 2.0);
            }
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            if (m.getPrefix().equals(markPrefix)) {
                nrofMark = 1;
            }
            nrofCopies = 1;
        }
        m.updateProperty(MSG_MARK_PROPERTY, nrofMark);
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getPrefix().equals(markPrefix)) {
            if (m.getTo() == otherHost) {
                return true;
            }
            Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
            if (nrofCopies > 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        ArrayList<DTNHost> tempPeerMark = new ArrayList<>();
        if (m.getPrefix().equals(markPrefix)) {

            Collection<Message> messageColl = otherHost.getMessageCollection();
            Iterator it = messageColl.iterator();
            while (it.hasNext()) {
                Message temp = (Message) it.next();
                if (m.getPrefix().equals(markPrefix)) {
                    tempPeerMark.add(m.getFrom());
                }
            }
            markMessage.put(otherHost, tempPeerMark);

            if (m.getTo() == otherHost) {
                return true;
            }
            Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
            if (nrofMark > 1) {
                return true;
            }

            DTNHost thisHost = null;
            List<DTNHost> listHop = m.getHops();
            Iterator dt = listHop.iterator();
            while (dt.hasNext()) {
                thisHost = (DTNHost) dt.next();
            }

            tempPeerMark = getPeerMarkList(otherHost);
            if (tempPeerMark.contains(thisHost)) {
                this.recapturedNode.add(otherHost);
                this.markNode.add(otherHost);
            }

        }
        this.recapturedNode.add(otherHost);

        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        if (m.getTo() == otherHost) {
            return false;
        }
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);

        if (isBinary) {
            if (m.getPrefix().equals(markPrefix)) {

                nrofMark /= 2;
//                System.out.println(m.getId() + " " + m.getFrom() + " " + nrofMark);
            }
            nrofCopies /= 2;
        } else {

            if (m.getPrefix().equals(markPrefix)) {
                nrofMark--;
            }
            nrofCopies--;
        }
        m.updateProperty(MSG_MARK_PROPERTY, nrofMark);
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return m.getTo() == hostReportingOld;
    }

    @Override
    public RoutingDecisionEngineImproved replicate() {
        return new SprayandWaitDE(this);
    }

    private SprayandWaitDE getOtherSnFDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouterImproved : "This router only works "
                + " with other routers of same type";

        return (SprayandWaitDE) ((DecisionEngineRouterImproved) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        double currentTime = SimClock.getTime();
        if (currentTime - lastUpdate >= interval) {
            // System.out.println("Interval : " + SimClock.getTime());
            for (Message m : host.getMessageCollection()) {
                if (m.getPrefix().equals(markPrefix)) {
                    if (host.equals(m.getFrom())) {
                        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
                        if (nrofMark == 1) {
                            System.out.println("");
                            System.out.println("Node Inisiator " + host.getAddress());
                            // System.out.println("nrofMark " + nrofMark);
                            System.out.println("Interval : " + SimClock.getTime());
                            System.out.println("TTL " + m.getTtl());

                            if (!this.markNode.isEmpty()) {
                                estimation = (initialNrofCopies * this.recapturedNode.size()) / this.markNode.size();
                                System.out.println("Mark        : " + initialNrofCopies);
                                System.out.println("Recapture   : " + this.recapturedNode.size());
                                System.out.println("m           : " + this.markNode.size());
                                System.out.println("Estimasi    : " + estimation);
                                System.out.println("");
                            }
                        }
                    }
                }
            }
        }
        this.lastUpdate = currentTime - currentTime % interval;
    }

    @Override
    public int getResidu() {
        return this.mark;
    }

    @Override
    public int getCounting() {
        return this.estimation;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    @Override
    public boolean getNodeInitiator() {
        return this.nodeMark;
    }

    private ArrayList<DTNHost> getPeerMarkList(DTNHost host) {
        if (markMessage.containsKey(host)) {
            return markMessage.get(host);
        } else {
            ArrayList<DTNHost> tempPeerMark = new ArrayList<>();
            return tempPeerMark;
        }
    }

}
