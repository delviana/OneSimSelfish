/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngineImproved;

/**
 *
 * @author LAB JARKOM
 */
public class InitiatorCheck extends Report implements UpdateListener{
    
    public static final String INTERVAL_COUNT = "Interval";
    public static final int DEFAULT_INTERVAL1_COUNT = 900;
    public static final String ENGINE_SETTING = "decisionEngine";
    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    private Map<DTNHost, ArrayList<Boolean>> check = new HashMap<DTNHost, ArrayList<Boolean>>();

    public InitiatorCheck() {
        super();
        Settings settings = getSettings();
        if (settings.contains(INTERVAL_COUNT)) {
            interval = settings.getInt(INTERVAL_COUNT);
        } else {
            interval = -1;
        }
        if (interval < 0) {
            interval = DEFAULT_INTERVAL1_COUNT;
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        if(SimClock.getTime() - lastRecord >= interval){
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }
    private void printLine(List<DTNHost> hosts) {
        for (DTNHost h : hosts) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouterImproved)) {
                continue;
            }
            RoutingDecisionEngineImproved de = ((DecisionEngineRouterImproved) r).getDecisionEngine();
            NodeInitiator n = (NodeInitiator) de;
            ArrayList <Boolean> listHC = new ArrayList<>();
            boolean temp = n.getNodeInitiator();
            if(check.containsKey(h)){
                listHC = check.get(h);
                listHC.add(temp);
                check.put(h, listHC);
            }else{
               check.put(h, listHC);
            }
        }
    }
    public void done() {
        for (Map.Entry<DTNHost, ArrayList<Boolean>> entry : check.entrySet()) {
            String printHost = "Node " + entry.getKey().getAddress() + "\t";
            for (Boolean countList : entry.getValue()) {
                printHost = printHost + "\t" + countList;
            }
            write(printHost);
        }
        super.done();
    }
}

