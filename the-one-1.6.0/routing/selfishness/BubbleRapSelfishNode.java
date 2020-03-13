/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing.selfishness;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.ModuleCommunicationListener;
import core.Settings;
import core.SimClock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.Centrality;
import routing.community.CommunityDetection;
import routing.community.Duration;
import routing.community.SimpleCommunityDetection;

/**
 *
 * @author Jarkom
 */
public class BubbleRapSelfishNode implements RoutingDecisionEngine, ModuleCommunicationListener {

    //setting ID untuk setting algoritma deteksi Komunitas : setting id {@value{}
    public static final String COMMUNITY_ALG_SETTING = "communityDetectAlg";
    //setting ID untuk setting algoritma komputasi centrality 
    public static final String CENTRALITY_ALG_SETTING = "centralityAlg";
    //Membuat Map untuk startTimestamps
    protected Map<DTNHost, Double> startTimestamps;
    //Membuat Map untuk Conection history, Duration menggunakan List
    protected Map<DTNHost, List<Duration>> connHistory;
    //inisialisasi komunity
    protected CommunityDetection community;
    //Mengambil TTL dari EnergyModel
    public static final String MSG_TTL_S = "msgTTL";
    //Mengambil messaget TTL
    protected int msgTTL;
    //inisialisasi centrality
    protected Centrality centrality;
    protected DTNHost thisHosts;
    
    protected Map<ListPastForwards_O, ListPastReceive_I> exChange;
    protected PastForwards_O past_O;
    protected PastReceive_I past_I;
    protected ListPastForwards_O pastO;
    protected ListPastReceive_I pastI;
//    protected DTNHost thishost;

    private double currentEnergy;

    //membangun distributedbublerap decision engine berbasis settings 
    //menggambarkan settings objek parameter. Class mencari Class Nama 
    //Deteksi komunitas dan algoritma centrality yang seharusnya digunakan untuk 
    //melakukan routing
    public BubbleRapSelfishNode(Settings s) {

        //contains : mengembalikan nilai true jika nama settingan memiliki beberapa nilai tertentu 
        if (s.contains(COMMUNITY_ALG_SETTING)) {
            //jika Community Bernilai, maka nilai comunity akan dilemparkan ke comunity detection (kelas interface)
            //maka buat objek dari community_alg_setting 
            this.community = (CommunityDetection) s.createIntializedObject(s.getSetting(COMMUNITY_ALG_SETTING));
            //jika tidak maka
        } else {
            //comunity detection yang digunakan adalah simplecomunitydetection
            this.community = new SimpleCommunityDetection(s);
        }
    }
    
    public BubbleRapSelfishNode(BubbleRapSelfishNode proto) {
        this.community = proto.community.replicate();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }
    
    @Override
    //pertama kali koneksi dibangun
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        thisHosts = thisHost;
        //cek baterai level dari kedua node baik thisHost maupun peer apakah diatas thr=misalnya 7000
//        if ((getEnergy(thisHost) > 7000) && (getEnergy(peer) > 7000)) {
        //exchange ListPastForwards_O dan ListPastReceive_I
//            this.exChange.put(pastO, pastI);
        //exchange = keep only the most recent contact

        //exhange battery level and metadata of message carried
        //compute utilities of each message (perceived altruism value of the message
        //with he regard to the peer.
        //bila batery level tidak lebih besar dari 7000/threshold, maka            
//        } else {
        //tidak terjadi pertukaran atau Conectiondown
//            double time = startTimestamps.get(peer);
//            double etime = SimClock.getTime();
//        }
    }
    
    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
        double time = startTimestamps.get(peer);
        double etime = SimClock.getTime();
//        System.out.println(getInitialEnergy(thisHost));
    }
    
    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
        DTNHost myHost = con.getOtherNode(peer);
        BubbleRapSelfishNode de = this.getOtherDecisionEngine(peer);
        
        this.startTimestamps.put(peer, SimClock.getTime());
        de.startTimestamps.put(myHost, SimClock.getTime());
        
        this.community.newConnection(myHost, peer, de.community);

        //pastO(myHost,peer, this.community, de.community)
//        this.past_O.newConnection(myHost, peer, de.past_O);
//        de.past_I.newConnection(peer, myHost, this.past_I);
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
    
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        BubbleRapSelfishNode de = getOtherDecisionEngine(otherHost);
        
        boolean peerInCommunity = de.commumesWithHost(dest); // apakh peer di dlm community nya dest
        boolean meInCommunity = this.commumesWithHost(dest); // apakh THIS ada di community nya dest

        if (peerInCommunity && !meInCommunity) // peer is in dest's community, but THIS is not
        {
            return true;
        } else if (!peerInCommunity && meInCommunity) // THIS is in dest'community, but peer is not
        {
            return false;
        } else if (peerInCommunity) // We're both in dest'community
        {
            // Forward to the one with the higher local centrality (in dest'community)

        }
        Double me = de.getEnergy(otherHost);
        Double peer = this.getEnergy(otherHost);
//        System.out.println("me = " + me+" peer = "+peer);
        
//        System.out.println(getInitialEnergy(dest));
//        System.out.println(getBuffer(dest));
//        System.out.println(getEnergy(dest));
//        System.out.println(getResidualEnergy(dest));
//        System.out.println(getResidualBuffer(dest));

        return ((me > 7000) && (peer > 7000));
//        return true;
    }
    
    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        // delete the message once it is forwarded to the node in the dest'community
        BubbleRapSelfishNode de = this.getOtherDecisionEngine(otherHost);
        return de.commumesWithHost(m.getTo())
                && !this.commumesWithHost(m.getTo());
    }
    
    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
//        BubbleRapSelfishNode de = this.getOtherDecisionEngine(hostReportingOld);
//        return de.commumesWithHost(m.getTo())
//                && !this.commumesWithHost(m.getTo());

        return true;
    }
    
    @Override
    public RoutingDecisionEngine replicate() {
        return new BubbleRapSelfishNode(this);
    }
    
    @Override
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
//        System.out.println(getInitialEnergy(h));
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

    //hitung nilai altruism
    public Double getAltruism(DTNHost h) {
        //altruism (N,M) = type (M, o.m * thr (o.b) dijumlahkan dengan type(M, i.m * thr (i.b)
        return null;
    }
    
    public Double getAltruismValue(DTNHost thisHost, DTNHost peer) {
        //cek apakah nilai altruism yang dihasilkan lebih dari treshold
        //if (altruism (peer,M) > threshold
        //        return true
        //maka forward message

        return null;
    }
    
    protected boolean commumesWithHost(DTNHost dest) {
        return community.isHostInCommunity(dest);
    }
    
    protected double getLocalCentrality() {
        return this.centrality.getLocalCentrality(connHistory, community);
    }
    
    public Set<DTNHost> getLocalCommunity() {
        return this.community.getLocalCommunity();
    }
    
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost, DTNHost thisHost) {
        if (m.getTo() == otherHost) {
            return true;
        }
        DTNHost dest = m.getTo();
        BubbleRapSelfishNode de = getOtherDecisionEngine(otherHost);
        
        boolean peerInCommunity = de.commumesWithHost(dest); // apakh peer di dlm community nya dest
        boolean meInCommunity = this.commumesWithHost(dest); // apakh THIS ada di community nya dest

        if (peerInCommunity && !meInCommunity) // peer is in dest's community, but THIS is not
        {
            return true;
        } else if (!peerInCommunity && meInCommunity) // THIS is in dest'community, but peer is not
        {
            return false;
        } else if (peerInCommunity) // We're both in dest'community
        {
            // Forward to the one with the higher local centrality (in dest'community)

        }
        Double me = de.getEnergy(otherHost);
        Double peer = this.getEnergy(otherHost);
        System.out.println("me = " + me + " peer = " + peer);
// System.out.println(getInitialEnergy(dest));
//        return (me
        return true;
    }
    
}
