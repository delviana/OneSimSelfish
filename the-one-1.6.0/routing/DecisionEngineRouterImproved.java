package routing;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import core.*;
import java.util.*;
import util.Tuple;

/**
 * This class overrides ActiveRouter in order to inject calls to a
 * DecisionEngine object where needed add extract as much code from the update()
 * method as possible.
 *
 * <strong>Forwarding Logic:</strong>
 *
 * A DecisionEngineRouterImproved maintains a List of Tuple<Message, Connection>
 * in support of a call to ActiveRouter.tryMessagesForConnected() in
 * DecisionEngineRouterImproved.update(). Since update() is called so
 * frequently, we'd like as little computation done in it as possible; hence the
 * List that gets updated when events happen. Four events cause the List to be
 * updated: a new message from this host, a new received message, a connection
 * goes up, or a connection goes down. On a new message (either from this host
 * or received from a peer), the collection of open connections is examined to
 * see if the message should be forwarded along them. If so, a new Tuple is
 * added to the List. When a connection goes up, the collection of messages is
 * examined to determine to determine if any should be sent to this new peer,
 * adding a Tuple to the list if so. When a connection goes down, any Tuple in
 * the list associated with that connection is removed from the List.
 *
 * <strong>Decision Engines</strong>
 *
 * Most (if not all) routing decision making is provided by a
 * RoutingDecisionEngineImproved object. The DecisionEngine Interface defines
 * methods that enact computation and return decisions as follows:
 *
 * <ul>
 * <li>In createNewMessage(), a call to
 * RoutingDecisionEngineImproved.newMessage() is made. A return value of true
 * indicates that the message should be added to the message store for routing.
 * A false value indicates the message should be discarded.
 * </li>
 * <li>changedConnection() indicates either a connection went up or down. The
 * appropriate connectionUp() or connectionDown() method is called on the
 * RoutingDecisionEngineImproved object. Also, on connection up events, this
 * first peer to call changedConnection() will also call
 * RoutingDecisionEngineImproved.doExchangeForNewConnection() so that the two
 * decision engine objects can simultaneously exchange information and update
 * their routing tables (without fear of this method being called a second
 * time).
 * </li>
 * <li>Starting a Message transfer, a protocol first asks the neighboring peer
 * if it's okay to send the Message. If the peer indicates that the Message is
 * OLD or DELIVERED, call to
 * RoutingDecisionEngineImproved.shouldDeleteOldMessage() is made to determine
 * if the Message should be removed from the message store.
 * <em>Note: if tombstones are enabled or deleteDelivered is disabled, the
 * Message will be deleted and no call to this method will be made.</em>
 * </li>
 * <li>When a message is received (in messageTransferred), a call to
 * RoutingDecisionEngineImproved.isFinalDest() to determine if the receiving
 * (this) host is an intended recipient of the Message. Next, a call to
 * RoutingDecisionEngineImproved.shouldSaveReceivedMessage() is made to
 * determine if the new message should be stored and attempts to forward it on
 * should be made. If so, the set of Connections is examined for transfer
 * opportunities as described above.
 * </li>
 * <li> When a message is sent (in transferDone()), a call to
 * RoutingDecisionEngineImproved.shouldDeleteSentMessage() is made to ask if the
 * departed Message now residing on a peer should be removed from the message
 * store.
 * </li>
 * </ul>
 *
 * <strong>Tombstones</strong>
 *
 * The ONE has the the deleteDelivered option that lets a host delete a message
 * if it comes in contact with the message's destination. More aggressive
 * approach lets a host remember that a given message was already delivered by
 * storing the message ID in a list of delivered messages (which is called the
 * tombstone list here). Whenever any node tries to send a message to a host
 * that has a tombstone for the message, the sending node receives the
 * tombstone.
 *
 * @author PJ Dillon, University of Pittsburgh
 * @modified by Gregorius Bima, Sanata Dharma University
 */
/*
 * When Connection goes up, a call to RoutingDecisionEngineImproved.shouldDeleteMessage()
 * is made to prevent the message sent in other connection. So the message
 * can be deleted after no connection request it.
 */
public class DecisionEngineRouterImproved extends ActiveRouter {

    public static final String PUBSUB_NS = "DecisionEngineRouterImproved";
    public static final String MARK_TTL = "markTtl";
    public static final String MARK_PREFIX = "markPrefix";
    public static final String ENGINE_SETTING = "decisionEngine";
    public static final String TOMBSTONE_SETTING = "tombstones";
    public static final String CONNECTION_STATE_SETTING = "";
    protected boolean tombstoning;
    protected RoutingDecisionEngineImproved improvedDecider;
    protected List<Tuple<Message, Connection>> outgoingMessages;
    protected Set<String> tombstones;
    protected int markTtl;

    /**
     * Used to save state machine when new connections are made. See comment in
     * changedConnection()
     */
    protected Map<Connection, Integer> conStates;

    public DecisionEngineRouterImproved(Settings s) {
        super(s);

        Settings routeSettings = new Settings(PUBSUB_NS);

        outgoingMessages = new LinkedList<Tuple<Message, Connection>>();

        improvedDecider = (RoutingDecisionEngineImproved) routeSettings.createIntializedObject(
                "routing." + routeSettings.getSetting(ENGINE_SETTING));

        if (routeSettings.contains(TOMBSTONE_SETTING)) {
            tombstoning = routeSettings.getBoolean(TOMBSTONE_SETTING);
        } else {
            tombstoning = false;
        }
        
        if (routeSettings.contains(MARK_TTL)) {
            markTtl = routeSettings.getInt(MARK_TTL);
        } else {
            markTtl = 0;
        }

        if (tombstoning) {
            tombstones = new HashSet<String>(10);
        }
        conStates = new HashMap<Connection, Integer>(4);
    }

    public DecisionEngineRouterImproved(DecisionEngineRouterImproved r) {
        super(r);
        outgoingMessages = new LinkedList<Tuple<Message, Connection>>();
        improvedDecider = (RoutingDecisionEngineImproved) r.improvedDecider.replicate();
        tombstoning = r.tombstoning;
        this.markTtl = r.markTtl;

        if (this.tombstoning) {
            tombstones = new HashSet<String>(10);
        }
        conStates = new HashMap<Connection, Integer>(4);
    }

    @Override
    public MessageRouter replicate() {
        return new DecisionEngineRouterImproved(this);
    }

    @Override
    public boolean createNewMessage(Message m) {
        if (improvedDecider.newMessage(m)) {
         if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                m.setTtl(this.markTtl);
            } else {
                m.setTtl(this.msgTtl);
            }
            makeRoomForNewMessage(m.getSize());
            //revised by Matthew
            

            addToMessages(m, true);

            findConnectionsForNewMessage(m, getHost());
            return true;
        }
        return false;
    }

    @Override
    public void changedConnection(Connection con) {
        DTNHost myHost = getHost();
        DTNHost otherNode = con.getOtherNode(myHost);
        DecisionEngineRouterImproved otherRouter = (DecisionEngineRouterImproved) otherNode.getRouter();
        if (con.isUp()) {
            improvedDecider.connectionUp(myHost, otherNode);

            /*
			 * This part is a little confusing because there's a problem we have to
			 * avoid. When a connection comes up, we're assuming here that the two 
			 * hosts who are now connected will exchange some routing information and
			 * update their own based on what the get from the peer. So host A updates
			 * its routing table with info from host B, and vice versa. In the real
			 * world, A would send its *old* routing information to B and compute new
			 * routing information later after receiving B's *old* routing information.
			 * In ONE, changedConnection() is called twice, once for each host A and
			 * B, in a serial fashion. If it's called for A first, A uses B's old info
			 * to compute its new info, but B later uses A's *new* info to compute its
			 * new info.... and this can lead to some nasty problems. 
			 * 
			 * To combat this, whichever host calls changedConnection() first calls
			 * doExchange() once. doExchange() interacts with the DecisionEngine to
			 * initiate the exchange of information, and it's assumed that this code
			 * will update the information on both peers simultaneously using the old
			 * information from both peers.
             */
            if (shouldNotifyPeer(con)) {
                this.doExchange(con, otherNode);
                otherRouter.didExchange(con);
            }

            /*
			 * Once we have new information computed for the peer, we figure out if
			 * there are any messages that should get sent to this peer.
             */
            Collection<Message> msgs = getMessageCollection();
            for (Message m : msgs) {
         if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                    if (improvedDecider.shouldSendMarkToHost(m, otherNode)) {
                        outgoingMessages.add(new Tuple<Message, Connection>(m, con));
                    }
                }
                if (improvedDecider.shouldSendMessageToHost(m, otherNode)) {
                    outgoingMessages.add(new Tuple<Message, Connection>(m, con));
                }
            }
        } else {
            improvedDecider.connectionDown(myHost, otherNode);

            conStates.remove(con);

            /*
			 * If we  were trying to send message to this peer, we need to remove them
			 * from the outgoing List.
             */
            for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                    i.hasNext();) {
                Tuple<Message, Connection> t = i.next();
                if (t.getValue() == con) {
                    i.remove();
                }
            }
        }
    }

    protected void doExchange(Connection con, DTNHost otherHost) {
        conStates.put(con, 1);
        improvedDecider.doExchangeForNewConnection(con, otherHost);
    }

    /**
     * Called by a peer DecisionEngineRouter to indicated that it already
     * performed an information exchange for the given connection.
     *
     * @param con Connection on which the exchange was performed
     */
    protected void didExchange(Connection con) {
        conStates.put(con, 1);
    }

    @Override
    protected int startTransfer(Message m, Connection con) {
        int retVal;

        if (!con.isReadyForTransfer()) {
            return TRY_LATER_BUSY;
        }

        retVal = con.startTransfer(getHost(), m);
        if (retVal == RCV_OK) { // started transfer
            addToSendingConnections(con);
        } else if (tombstoning && retVal == DENIED_DELIVERED) {
            this.deleteMessage(m.getId(), false);
            tombstones.add(m.getId());
        } else if (deleteDelivered && (retVal == DENIED_OLD || retVal == DENIED_DELIVERED)
                && improvedDecider.shouldDeleteOldMessage(m, con.getOtherNode(getHost()))) {
            /* final recipient has already received the msg -> delete it */
//			if(m.getId().equals("M7"))
//				System.out.println("Host: " + getHost() + " told to delete M7");
            this.deleteMessage(m.getId(), false);
        }

        return retVal;
    }

    @Override
    public int receiveMessage(Message m, DTNHost from) {
        if (isDeliveredMessage(m) || (tombstoning && tombstones.contains(m.getId()))) {
            return DENIED_DELIVERED;
        }

        return super.receiveMessage(m, from);
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message incoming = removeFromIncomingBuffer(id, from);

        if (incoming == null) {
            throw new SimError("No message with ID " + id + " in the incoming "
                    + "buffer of " + getHost());
        }

        incoming.setReceiveTime(SimClock.getTime());

        Message outgoing = incoming;
        for (Application app : getApplications(incoming.getAppID())) {
            // Note that the order of applications is significant
            // since the next one gets the output of the previous.
            outgoing = app.handle(outgoing, getHost());
            if (outgoing == null) {
                break; // Some app wanted to drop the message
            }
        }

        Message aMessage = (outgoing == null) ? (incoming) : (outgoing);

        boolean isFinalRecipient = improvedDecider.isFinalDest(aMessage, getHost());
        boolean isFirstDelivery = isFinalRecipient
                && !isDeliveredMessage(aMessage);

        if (outgoing != null && improvedDecider.shouldSaveReceivedMessage(aMessage, getHost())) {
            // not the final recipient and app doesn't want to drop the message
            // -> put to buffer
            addToMessages(aMessage, false);

            // Determine any other connections to which to forward a message
            findConnectionsForNewMessage(aMessage, from);
        }

        if (isFirstDelivery) {
            this.deliveredMessages.put(id, aMessage);
        }

        for (MessageListener ml : this.mListeners) {
            ml.messageTransferred(aMessage, from, getHost(),
                    isFirstDelivery);
        }

        return aMessage;
    }

    @Override
    protected void transferDone(Connection con) {
        Message transferred = this.getMessage(con.getMessage().getId());

        for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                i.hasNext();) {
            Tuple<Message, Connection> t = i.next();
            if (t.getKey().getId().equals(transferred.getId())
                    && t.getValue().equals(con)) {
                i.remove();
                break;
            }
        }

        //    improvedDecider.transferDone(con);
        if (improvedDecider.shouldDeleteSentMessage(transferred, con.getOtherNode(getHost()))) {
            if (transferred.getId().equals("M7")) {
                this.deleteMessage(transferred.getId(), false);
            }

        }
    }

    @Override
    public void update() {
        super.update();
        improvedDecider.update(getHost());

        if (!canStartTransfer() || isTransferring()) {
            return; // nothing to transfer or is currently transferring 
        }

        tryMessagesForConnected(outgoingMessages);

        for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                i.hasNext();) {
            Tuple<Message, Connection> t = i.next();
            if (!this.hasMessage(t.getKey().getId())) {
                i.remove();
            }
        }
    }

    @Override
    public void deleteMessage(String id, boolean drop) {
        super.deleteMessage(id, drop);

        for (Iterator<Tuple<Message, Connection>> i = outgoingMessages.iterator();
                i.hasNext();) {
            Tuple<Message, Connection> t = i.next();
            if (t.getKey().getId().equals(id)) {
                i.remove();
            }
        }
    }

    public RoutingDecisionEngineImproved getDecisionEngine() {
        return this.improvedDecider;
    }

    protected boolean shouldNotifyPeer(Connection con) {
        Integer i = conStates.get(con);
        return i == null || i < 1;
    }

    protected void findConnectionsForNewMessage(Message m, DTNHost from) {
//		for(Connection c : getHost()) 
        for (Connection c : getConnections()) {
            DTNHost other = c.getOtherNode(getHost());
             if (m.getPrefix().equals(Marker.getInstance().getMarkPrefix())) {
                if (other != from && improvedDecider.shouldSendMarkToHost(m, other)) {
                outgoingMessages.add(new Tuple<Message, Connection>(m, c));
                }
            }
            if (other != from && improvedDecider.shouldSendMessageToHost(m, other)) {
//				if(m.getId().equals("M7"))
//					System.out.println("Adding attempt for M7 from: " + getHost() + " to: " + other);
                outgoingMessages.add(new Tuple<Message, Connection>(m, c));
            }
        }
    }
    
}
