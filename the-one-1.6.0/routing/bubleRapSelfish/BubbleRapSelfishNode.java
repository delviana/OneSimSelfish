/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.bubleRapSelfish;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Centrality;
import routing.community.CommunityDetection;
import routing.community.CommunityDetectionEngine;
import routing.community.Duration;
import routing.community.SimpleCommunityDetection;


/**
 *
 * @author Jarkom
 */
public class BubbleRapSelfishNode implements RoutingDecisionEngine, CommunityDetectionEngine {

    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;
    protected CommunityDetection community;
    public static final String MSG_TTL_S = "msgTTL";
    protected int msgTTL;
    protected Centrality centrality;
    protected DTNHost thisHosts;
    protected Map<DTNHost, List<TupleForwardReceive>> exChange;
    private double currentEnergy;
    
    
    

    public BubbleRapSelfishNode(Settings s) {
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
        } else {
            this.community = new SimpleCommunityDetection(s);
        }
    }

    public BubbleRapSelfishNode(BubbleRapSelfishNode proto) {
        this.community = proto.community.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
        exChange = new HashMap<>();
    }
       
    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        thisHosts = thisHost;
        double TupleForward_O = startTimestamps.get(peer);
        double TupleReceive_I = SimClock.getIntTime();

        if ((getEnergy(thisHost) > 7000) && (getEnergy(peer) > 7000)) {
            CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;

            List<TupleForwardReceive> FR = null;
            if (!exChange.containsKey(peer)) {
                FR = new LinkedList<TupleForwardReceive>();
                exChange.put(peer, FR);
            } else {
                FR = exChange.get(peer);
            }
     
            ListPastForwards_O O = new ListPastForwards_O(thisHost, peer, this.community, peerCD, SimClock.getTime());
            ListPastReceive_I I = new ListPastReceive_I(thisHost, peer, this.community, peerCD, SimClock.getTime());
            FR.add(new TupleForwardReceive(O, I));
 
            if(TupleForward_O - TupleReceive_I > 0){
            FR.add(new TupleForwardReceive(O, I));
        }
            
        } else {
//            double time = startTimestamps.get(peer);
//            double etime = SimClock.getTime();
        }
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();
        System.out.println(getInitialEnergy(thisHost));

        List<Duration> history = null;
        if (!connHistory.containsKey(peer)) {
            history = new LinkedList<Duration>();
            connHistory.put(peer, history);
        } else {
            history = connHistory.get(peer);
        }

        if (etime - time > 0) {
            history.add(new Duration(time, etime));
        }

        CommunityDetection peerCD = this.getOtherDecisionEngine(peer).community;
        community.connectionLost(thisHost, peer, peerCD, history);
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        BubbleRapSelfishNode de = this.getOtherDecisionEngine(peer);

        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());

        this.community.newConnection(myHost, peer, de.community);

    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        return m.getTo() != thisHost;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        BubbleRapSelfishNode de = getOtherDecisionEngine(otherHost);

        boolean peerInCommunity = de.commumesWithHost(dest);
        boolean meInCommunity = this.commumesWithHost(dest);

        if (peerInCommunity && !meInCommunity) {
            return true;
        } else if (!peerInCommunity && meInCommunity) {
            return false;
        } else if (peerInCommunity) {

        }
        Double me = getEnergy(thisHosts);
        Double peer = getEnergy(otherHost);
        System.out.println("me = " + me + " peer = " + peer);

        System.out.println(getInitialEnergy(dest));
        System.out.println(getBuffer(dest));
        System.out.println(getEnergy(dest));
        System.out.println(getResidualEnergy(dest));
        System.out.println(getResidualBuffer(dest));
        return ((me > 9000) && (peer > 9000));
//        return true;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        BubbleRapSelfishNode de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new BubbleRapSelfishNode(this);
    }

    public void moduleValueChanged(String key, Object newValue) {
        this.currentEnergy = (Double) newValue;
    }

    private BubbleRapSelfishNode getOtherDecisionEngine(DTNHost peer) {
        MessageRouter otherRouter = peer.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (BubbleRapSelfishNode) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    private Double getEnergy(DTNHost host) {
        return (Double) host.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) {
        System.out.println(getInitialEnergy(h));
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }

    private Double getBuffer(DTNHost h) {
        return Double.valueOf(h.getRouter().getFreeBufferSize());

    }

    private Double getResidualBuffer(DTNHost h) {
        Double bfAwal = Double.valueOf(h.getRouter().getBufferSize());
        Double bfAkhir = getBuffer(h);
        Double residualBuffer = bfAkhir / bfAwal;

        return residualBuffer;
    }

    private Double getResidualEnergy(DTNHost h) {
        Double eAwal = getInitialEnergy(h);
        Double eAkhir = getEnergy(h);
        Double residualEnergy = eAkhir / eAwal;
        return residualEnergy;
    }

    public Double getAltruism(DTNHost h, Message m, DTNHost peer, Map<ListPastForwards_O, ListPastReceive_I> exChange) {
       
        h = m.getFrom();

        Double altruism;
        altruism = thisHosts.getAddress() + peer.getAddress() * getEnergy(h);

        return altruism;
    }

    public boolean getAltruismValue(DTNHost h, Message m, DTNHost peer, Map<ListPastForwards_O, ListPastReceive_I> exChange) {
        return true;
       
    }

    protected boolean commumesWithHost(DTNHost dest) {
        return community.isHostInCommunity(dest);
    }

    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }

    @Override
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }

    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        BubbleRapSelfishNode de = getOtherDecisionEngine(otherHost);

        boolean peerInCommunity = de.commumesWithHost(dest);
        boolean meInCommunity = this.commumesWithHost(dest);

        if (peerInCommunity && !meInCommunity) {
            return true;
        } else if (!peerInCommunity && meInCommunity) {
            return false;
        } else if (peerInCommunity) {

        }
        Double me = de.getEnergy(otherHost);
        Double peer = this.getEnergy(otherHost);
        System.out.println("me = " + me + " peer = " + peer);
        System.out.println(getInitialEnergy(dest));

        return true;
    }

   }
