package report;

/**
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * <Simulation time> <average buffer occupancy % [0..100]> <variance>
 * </p>
 *
 */
import java.util.*;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import routing.DecisionEngineRouter;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;

public class ResiduReport extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";
    /**
     * Default value for the snapshot interval
     */
//    public static final int DEFAULT_WAKTU = 1800;
    public static final int DEFAULT_WAKTU = 3600;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    public ResiduReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(NODE_PERWAKTU)) {
            interval = settings.getInt(NODE_PERWAKTU);
        } else {
            interval = DEFAULT_WAKTU;
        }
    }

    public void updated(List<DTNHost> hosts) {
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    private void printLine(List<DTNHost> hosts) {
         Settings s = new Settings();
        int numbernode1 = s.getInt("Group1.nrofHosts");
        int numbernode2 = s.getInt("Group2.nrofHosts");
//        int numbernode3 = s.getInt("Group3.nrofHosts");
//        int numbernode4 = s.getInt("Group4.nrofHosts");
//        int numbernode5 = s.getInt("Group5.nrofHosts");
//        int numbernode6 = s.getInt("Group6.nrofHosts");
//        int numbernode7 = s.getInt("Group7.nrofHosts");
//        int numbernode8 = s.getInt("Group8.nrofHosts");
//        int numbernode9 = s.getInt("Group9.nrofHosts");
//        int numbernode10 = s.getInt("Group10.nrofHosts");
        
        int nrofNode = numbernode1 + numbernode2; 

          int residu = 0;
          double rata = 0;
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouterImproved))
                continue;
            RoutingDecisionEngineImproved de = ((DecisionEngineRouterImproved) r).getDecisionEngine();
           MarkingNode n = (MarkingNode) de;

            int temp = (int)n.getCounting();
            if(temp < nrofNode){
                residu++;
//                System.out.println("rata-rata residu = " + residu++);
//            }else if(temp > nrofNode){
//                residu++;
            }
//            temp = (temp <= 100.0) ? (temp) : (100.0);
            }
        int TotalResidu = residu;
        String output = format((int)SimClock.getTime()) +" \t "+ format(TotalResidu);
        write(output);
        }
}