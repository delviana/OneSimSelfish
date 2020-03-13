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
import java.util.List;
import routing.DecisionEngineRouter;
import routing.DecisionEngineRouterImproved;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.RoutingDecisionEngineImproved;

/**
 *
 * @author fans
 */
public class NodeTotalActive extends Report implements UpdateListener{
    public static final String NODE_TOTAL_REPORT_INTERVAL = "nodeTotal";
    public static final int DEFAULT_NODE_TOTAL_HEADCOUNT_REPORT_INTERVAL = 3600;
    private double  lastRecord = Double.MIN_VALUE;
    private int interval;
    
    public NodeTotalActive(){
        super();
        Settings settings = getSettings();
        if(settings.contains(NODE_TOTAL_REPORT_INTERVAL)){
            interval = settings.getInt(NODE_TOTAL_REPORT_INTERVAL);
        }else{
            interval = DEFAULT_NODE_TOTAL_HEADCOUNT_REPORT_INTERVAL;
        }
    }
    @Override
    public void updated(List<DTNHost> hosts) {
        if(SimClock.getTime() - lastRecord >= interval){
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }
    private void printLine(List <DTNHost> hosts){
        int active = 0;
        for (DTNHost host : hosts) {
            MessageRouter r = host.getRouter();
            if(!(r instanceof DecisionEngineRouterImproved))
                continue;
            RoutingDecisionEngineImproved de = ((DecisionEngineRouterImproved) r).getDecisionEngine();
            boolean inter = host.isRadioActive();
            if(inter == true){
                active++;
            }
        }
        String print = format(SimClock.getTime())+"\t"+ active;
        write(print);
    }
    
}
