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

public class AverageConvergenceTimeReport extends Report implements UpdateListener {

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

    public AverageConvergenceTimeReport() {
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
        double rata = 0;
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouterImproved))
                continue;
            RoutingDecisionEngineImproved de = (RoutingDecisionEngineImproved) ((DecisionEngineRouterImproved) r).getDecisionEngine();
            MarkingNode n = (MarkingNode) de;

            int temp = (int)n.getCounting();
            rata += temp;
//            temp = (temp <= 100.0) ? (temp) : (100.0);
            }
        double AV_Rata = rata/hosts.size();
        String output = format((int)SimClock.getTime()) +" \t "+ format(AV_Rata);
        write(output);
        }
}