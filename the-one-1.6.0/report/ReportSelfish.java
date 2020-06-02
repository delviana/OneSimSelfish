package report;

import core.DTNHost;
import core.SimScenario;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.bubleRapSelfish.TupleDecisionEngine;
import routing.bubleRapSelfish.TupleForwardReceive;
import routing.community.DegreeDetectionEngine;

/**
 *
 * @author Acer
 */
public class ReportSelfish extends Report{
    
    public ReportSelfish(){
        init();
    }
    
    @Override
    public void done(){
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        
        for (DTNHost h : nodes){
            MessageRouter r = h.getRouter();
            if(!(r instanceof DecisionEngineRouter)){
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if(!(de instanceof DegreeDetectionEngine)){
                continue;
            }
            
            TupleDecisionEngine tp = (TupleDecisionEngine) de;
            Map<DTNHost, List<TupleForwardReceive>> aTuple = tp.getTuple();

        }
        super.done();
    }
    
}