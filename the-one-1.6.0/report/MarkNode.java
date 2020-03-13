/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import core.*;
import java.util.*;
import routing.*;

/**
 *
 * @author fans
 */
public class MarkNode extends Report implements UpdateListener {

    public static final String INTERVAL_COUNT = "Interval";
    public static final int DEFAULT_INTERVAL1_COUNT = 7200;
    public static final String ENGINE_SETTING = "decisionEngine";
    private double lastRecord = Double.MIN_VALUE;
    private int interval;
    private int trashold = 108000;

    private Map<DTNHost, Collection<Message>> mark = new HashMap<DTNHost,Collection<Message>>();

    public MarkNode() {
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
        if (SimClock.getTime() - lastRecord >= interval) {
            lastRecord = SimClock.getTime();
            printLine(hosts);
        }
    }

    private void printLine(List<DTNHost> hosts) {
        int markNode = 0;
        for (DTNHost h : hosts) {
//            MessageRouter r = h.getRouter();
//            if (!(r instanceof DecisionEngineRouterImproved)) {
//                continue;
//            }
//            RoutingDecisionEngineImproved de = ((DecisionEngineRouterImproved) r).getDecisionEngine();
            Collection<Message> messageColl = h.getMessageCollection();
//            for (Message m : h.getMessageCollection()) {
//                if (m.getId().equals("MARK1")) {
//                    markNode = 1;
//                    messageColl.add(h.getMessageCollection());
                    mark.put(h, messageColl);
                //} else {
                  //  mark.put(h, listHC);
                }
            }
//    }

    public void done() {
        for (Map.Entry<DTNHost, Collection<Message>> entry : mark.entrySet()) {
         
            String printHost = "Node " + entry.getKey().getAddress() + "\t";
            for (Message countList : entry.getValue()) {
                printHost = printHost + "\t" + countList;
            }

            write(printHost);
        }
        super.done();
    }
}
