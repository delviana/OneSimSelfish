/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import core.DTNHost;
import core.Marker;
import core.Message;
import core.World;
import routing.DecisionEngineRouterImproved;
import routing.estimator.SprayandWaitDEtester;
import static routing.estimator.SprayandWaitDEtester.MARK_PREFIX;

/**
 * External event for creating a message.
 */
public class MessageCreateEvent extends MessageEvent {

    public static final String MARK_PREFIX = "markPrefix";
    private int size;
    private int responseSize;
    protected String markPrefix;

    /**
     * Creates a message creation event with a optional response request
     *
     * @param from The creator of the message
     * @param to Where the message is destined to
     * @param id ID of the message
     * @param size Size of the message
     * @param responseSize Size of the requested response message or 0 if no
     * response is requested
     * @param time Time, when the message is created
     */
    public MessageCreateEvent(int from, int to, String id, int size,
            int responseSize, double time) {
        super(from, to, id, time);
        this.size = size;
        this.responseSize = responseSize;
        this.markPrefix = Marker.getInstance().getMarkPrefix();
    }


    /**
     * Creates the message this event represents.
     */
    @Override
    public void processEvent(World world) {
        DTNHost to = world.getNodeByAddress(this.toAddr);
        DTNHost from = world.getNodeByAddress(this.fromAddr);

        Message m = null;
        String messagePrefix = this.id;
        if (messagePrefix.equals(markPrefix)) {
            m = new Message(from, null, this.id, this.size);
        } else {
            m = new Message(from, to, this.id, this.size);
        }

        m.setResponseSize(this.responseSize);
        from.createNewMessage(m);
    }

    @Override
    public String toString() {
        return super.toString() + " [" + fromAddr + "->" + toAddr + "] "
                + "size:" + size + " CREATE";
    }
}
