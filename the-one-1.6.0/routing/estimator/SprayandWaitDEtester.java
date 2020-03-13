/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.estimator;

import routing.MessageRouter;
import core.*;
import input.MessageEventGenerator;
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
public class SprayandWaitDEtester implements RoutingDecisionEngineImproved, MarkResidu, MarkingNode, NodeInitiator {

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
    public final String markPrefix;
    private int estimation;
    private int interval;
    private int mark;
    private Set<DTNHost> markNode;
    private Set<DTNHost> recapturedNode;
    private Map<DTNHost, ArrayList<DTNHost>> markMessage;

    private double time = 1.0;

    public SprayandWaitDEtester(Settings s) {
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

    public SprayandWaitDEtester(SprayandWaitDEtester r) {
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
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {

    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
//        DTNHost thisHost = con.getOtherNode(peer);
//        SprayandWaitDEtester pr = getOtherSnFDecisionEngine(peer);
//        for (Message m : thisHost.getMessageCollection()) {
//               if (m.getPrefix().equals(markPrefix)) {
//                if (thisHost.equals(m.getFrom())) {
//                    pr.estimation = this.estimation;
//                }
//            }
//        }
//        for (Message m : peer.getMessageCollection()) {
//               if (m.getPrefix().equals(markPrefix)) {
//                if (peer.equals(m.getFrom())) {
//                    this.estimation = pr.estimation;
//                }
//            }
//        }

//        if (this.estimation == 0) {
//            pr.estimation = this.estimation;
//        } else {
//            this.estimation = pr.estimation;
//        }
//        if (thisHost.isRadioActive() == true && peer.isRadioActive() == true) {
//                if (this.getEstimation() + pr.getEstimation() != 0) {
//                    update(thisHost);
//                    this.setEstimation(estimation);
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
        if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
            m.addProperty(MSG_MARK_PROPERTY, initialNrofCopies);
        } else {
            m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        }
        //  System.out.println(m+" "+m.getPrefix());
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
            if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                nrofMark = (int) Math.ceil(nrofMark / 2.0);
            }
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
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
//        if (m.getPrefix().equals(markPrefix)) {
//            if (m.getTo() == otherHost) {
//                return true;
//            }
//            Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
//            if (nrofCopies > 1) {
//                return true;
//            }
//        }
        return false;
    }

    @Override
    public boolean shouldSendMarkToHost(Message m, DTNHost otherHost) {
        if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
            if (m.getTo() == otherHost) {
                return true;
            }
            Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
            if (nrofMark > 1) {
                return true;
            }
            DTNHost thisHost = null;
            List<DTNHost> listHop = m.getHops();
            Iterator it = listHop.iterator();
            while (it.hasNext()) {
                thisHost = (DTNHost) it.next();
            }
            Collection<Message> messageColl = otherHost.getMessageCollection();
            it = messageColl.iterator();
            while (it.hasNext()) {
                Message temp = (Message) it.next();
                if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                    if (thisHost.equals(temp.getFrom())) {
                        this.recapturedNode.add(otherHost);
                        this.markNode.add(otherHost);
                    }
                }
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
            if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                nrofMark /= 2;
            }
            nrofCopies /= 2;
        } else {
            if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
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
        return new SprayandWaitDEtester(this);
    }

    private SprayandWaitDEtester getOtherSnFDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouterImproved : "This router only works "
                + " with other routers of same type";

        return (SprayandWaitDEtester) ((DecisionEngineRouterImproved) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        double currentTime = SimClock.getTime();
//        MessageEventGenerator messageEvent = new MessageEventGenerator(s);

        String myMarkId = "";
        String markPrefix = Marker.getInstance().getMarkPrefix();
        Collection<Message> messageCollection = host.getMessageCollection();
        for (Iterator<Message> iterator = messageCollection.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();
            String messagePrefix = msg.getPrefix();
            //Integer nrofMark = (Integer) msg.getProperty(MSG_MARK_PROPERTY);

            if (messagePrefix.equals(markPrefix)) {
                if (msg.getFrom() == host) {
                    int messageTTL = msg.getTtl();

                    if (messageTTL <= 0) {
                        System.out.println("test");
                        host.deleteMessage(MSG_MARK_PROPERTY, false);
                        Message message = new Message(host, null, myMarkId, msg.getSize());
                        this.newMessage(msg);
                    }
                } else {
                    int messageTTL = msg.getTtl();
                    if (messageTTL <= 0) {
                        host.deleteMessage(MSG_MARK_PROPERTY, false);
                        this.newMessage(msg);
                    }
                }
            }
        }

        if (currentTime - lastUpdate >= interval) {
            // System.out.println("Interval : " + SimClock.getTime());
            for (Message m : host.getMessageCollection()) {
                if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                    if (host.equals(m.getFrom())) {
                        Integer nrofMark = (Integer) m.getProperty(MSG_MARK_PROPERTY);
                        if (nrofMark == 1) {
                            System.out.println("");
                            System.out.println("Node " + host.getAddress());
                            System.out.println("nrofMark " + nrofMark);
                            System.out.println("Interval : " + SimClock.getTime());
                            System.out.println("TTL     : " + m.getTtl());

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

}
