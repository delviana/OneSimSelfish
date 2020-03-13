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
import core.UpdateListener;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;

public class ConvergenceTimeReport extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String NODE_PERWAKTU = "nodepersatuanwaktu";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_WAKTU = 3600;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Integer>> ListNode;

    public ConvergenceTimeReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(NODE_PERWAKTU)) {
            interval = settings.getInt(NODE_PERWAKTU);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_WAKTU;
        }
        ListNode = new HashMap<>();
    }

    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }

        if (simTime - lastRecord >= interval) {
            //lastRecord = SimClock.getTime();
            printLine(hosts);
            this.lastRecord = simTime - simTime % interval;
        }
        /**
         * for (DTNHost ho : hosts ) { double temp = ho.getBufferOccupancy();
         * temp = (temp<=100.0)?(temp):(100.0); if
         * (bufferCounts.containsKey(ho.getAddress()))
         * bufferCounts.put(ho.getAddress(),
         * (bufferCounts.get(ho.getAddress()+temp))/2); else
         * bufferCounts.put(ho.getAddress(), temp); } }
         */
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    private void printLine(List<DTNHost> hosts) {
        /**
         * double bufferOccupancy = 0.0; double bo2 = 0.0;
         *
         * for (DTNHost h : hosts) { double tmp = h.getBufferOccupancy(); tmp =
         * (tmp<=100.0)?(tmp):(100.0); bufferOccupancy += tmp; bo2 +=
         * (tmp*tmp)/100.0; } double E_X = bufferOccupancy / hosts.size();
         * double Var_X = bo2 / hosts.size() - (E_X*E_X)/100.0;
         *
         * String output = format(SimClock.getTime()) + " " + format(E_X) + " "
         * + format(Var_X); write(output);
         */
        for (DTNHost h : hosts) {

            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouterImproved)) {
                continue;
            }

            RoutingDecisionEngineImproved de = ((DecisionEngineRouterImproved) r).getDecisionEngine();
            if (!(de instanceof MarkingNode)) {
                continue;
            }

            MarkingNode n = (MarkingNode) de;

            ArrayList<Integer> NodeList = new ArrayList<Integer>();
            Integer temp = n.getCounting();
            //   Map<Integer, DTNHost> temp = (Map<Integer, DTNHost>) n.getListNode();
//   
            if (ListNode.containsKey(h)) {
                //bufferCounts.put(h, (bufferCounts.get(h)+temp)/2); seems WRONG
                //bufferCounts.put(h, bufferCounts.get(h)+temp);
                //write (""+ bufferCounts.get(h));
                NodeList = ListNode.get(h);
                NodeList.add(temp);
                ListNode.put(h, NodeList);
            } else {
                ListNode.put(h, NodeList);

            }
        }
    }

    @Override
    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Integer>> entry : ListNode.entrySet()) {
            /*DTNHost a = entry.getKey();
			Integer b = a.getAddress();
			Double avgBuffer = entry.getValue()/updateCounter;*/
            String printHost = entry.getKey().getAddress() + "\t";
            for (Integer NodeList : entry.getValue()) {
                printHost = printHost + "\t" + NodeList;
            }
            write(printHost);
            //write("" + b + ' ' + entry.getValue());
        }

        
        super.done();
    }

}
