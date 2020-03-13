/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static report.ActiveInterface.BUFFER_REPORT_INTERVAL;
import static report.ActiveInterface.DEFAULT_BUFFER_REPORT_INTERVAL;
import routing.DecisionEngineRouter;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;

/**
 *
 * @author LAB JARKOM
 */
public class StatusNode extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String BUFFER_REPORT_INTERVAL = "Interval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 3600;
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private Map<DTNHost, ArrayList<Integer>> statusNode = new HashMap<DTNHost, ArrayList<Integer>>();

    public StatusNode() {
        super();

        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }
    }

    @Override
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

    private void printLine(List<DTNHost> hosts) {
         for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouterImproved)) {
                continue;
            }

             RoutingDecisionEngineImproved de = ((DecisionEngineRouterImproved) r).getDecisionEngine();
            if (!(de instanceof Status)) {
                continue;
            }

            Status n = (Status) de;
            ArrayList<Integer> countList = new ArrayList<Integer>();
//            LinkedList<Integer> countList = new LinkedList<Integer>();
            int temp = n.getStatusNode();

            if (statusNode.containsKey(h)) {
                countList = statusNode.get(h);
                countList.add(temp);
                statusNode.put(h, countList);
            } else {
                statusNode.put(h, countList);
            }
        }

    }

    public void done() {
         for (Map.Entry<DTNHost, ArrayList<Integer>> entry : statusNode.entrySet()) {
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