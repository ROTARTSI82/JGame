package io.github.jgame.net.udp;

import io.github.jgame.Constants;
import io.github.jgame.logging.GenericLogger;
import io.github.jgame.net.NetUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.jgame.Constants.JGameStr;
import static io.github.jgame.util.StringManager.fmt;

public class UDPServer {
    private DatagramSocket socket;
    private InetAddress address;
    private Logger logger;
    private int port;

    /**
     * List of UUIDs of verified packets that have already been handled.
     * <p>
     * When we get a packet with a UUID in this list, we do not call the {@link #parse} handler
     * and simply send the confirm message.
     */
    private LinkedList<String> verifiedByMe = new LinkedList<>();

    /**
     * Active {@link VerifyPacket}s that need to be updated.
     */
    private HashMap<String, VerifyPacket> pendingPackets = new HashMap<>();

    /**
     * List of active clients.
     */
    private HashMap<String, UDPClientHandler> clients = new HashMap<>();

    /**
     * Table used to serialize actions. See {@link NetUtils}.serialize()
     */
    private HashMap<String, Integer> serialTable;

    /**
     * A reversed copy of the {@link #serialTable} (values are keys and keys are values).
     *
     * Therefore, the serial table must not contain any duplicate values for this to work properly.
     *
     * Used for deserialization of actions.
     */
    private HashMap<Integer, String> deserialTable;

    /**
     * How many clients can connect to the server at one time before we start kicking them.
     */
    private int clientLimit;

    /**
     * Create a new UDP server at the specified address
     *
     * @param host       Host to bind to
     * @param listenPort port to bind to
     * @param maxClients Maximum number of clients
     * @throws UnknownHostException     Invalid {@code hostname}
     * @throws SocketException          Error opening DatagramSocket
     * @throws IllegalArgumentException Invalid actionID
     */
    public UDPServer(String host, int listenPort, int maxClients) throws UnknownHostException, SocketException {
        address = InetAddress.getByName(host);
        port = listenPort;
        clientLimit = maxClients;
        socket = new DatagramSocket(listenPort, address);
        logger = Logger.getLogger(this.getClass().getName());

        serialTable = getActionTable();
        deserialTable = new HashMap<>();
        for (String action : serialTable.keySet()) {
            int actionID = serialTable.get(action);

            if (actionID > 0 && actionID <= 0xffff) {
                deserialTable.put(actionID, action);
            } else {
                throw new IllegalArgumentException("actionIDs need to be between 0x0000 and 0xffff");
            }
        }
    }

    /**
     * Used in constructor to determine action table. Action tables are used to serialize actions. See
     * {@link NetUtils}.serialize()
     *
     * @return Table
     */
    public HashMap<String, Integer> getActionTable() {
        return Constants.BUILTIN_ACTIONS;
    }

    /**
     * Send message
     *
     * @param datagram Message to send
     * @param datAddress Host to send to
     * @param datPort Port to send to
     * @throws IOException Sending message over connection may fail.
     */
    public void send(HashMap<String, Object> datagram, InetAddress datAddress, int datPort) throws IOException {
        logger.finest(fmt(JGameStr.getString("net.sendMSG"), socket.getInetAddress(), port,
                datAddress, datPort, datagram));
        byte[] bytes = NetUtils.serialize(datagram, serialTable);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, datAddress, datPort);
        socket.send(packet);
    }

    /**
     * Send the packet to all clients in client list.
     *
     * @param datagram Packet to send
     */
    public void sendToAll(HashMap<String, Object> datagram) {
        for (UDPClientHandler client : clients.values()) {
            try {
                send(datagram, client.address, client.port);
            } catch (IOException e) {
                logger.warning("Failed to send packet:\n" + GenericLogger.getStackTrace(e));
            }
        }
    }

    /**
     * Event handler for all messages sent here.
     *
     * @param datagram Message
     * @param packet raw packet
     */
    public void parse(HashMap<String, Object> datagram, DatagramPacket packet) {

    }

    /**
     * Accept new messages and add the address to our client list if the address is unkown and we have
     * not exceeded the {@link #clientLimit}.
     *
     * Also handle verifySends and shutdown events. Forward others to the {@link #parse} handler.
     *
     * @throws IOException Connection may fail
     */
    public void update() throws IOException {
        byte[] bytes = new byte[Constants.NET_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        socket.receive(packet);

        String packAddr = packet.getAddress().toString() + ":" + packet.getPort();
        if (!clients.containsKey(packAddr)) {
            if (clients.size() >= clientLimit) {
                HashMap<String, Object> kickMsg = new HashMap<>();
                kickMsg.put("action", "kick");
                kickMsg.put("reason", JGameStr.getString("net.alreadyFullMsg"));

                send(kickMsg, packet.getAddress(), packet.getPort());
                return;
            }
            clients.put(packAddr, new UDPClientHandler(packet.getAddress(), packet.getPort(), this));
            logger.info(fmt(JGameStr.getString("net.newClient"), address, port, packAddr));
        }

        HashMap<String, Object> packetDict = NetUtils.deserialize(packet.getData(), deserialTable);
        if (packetDict == null) {
            return;
        }
        logger.finest(fmt(JGameStr.getString("net.recvMSG"), socket.getInetAddress(), port,
                packet.getAddress(), packet.getPort(), packetDict));

        String action = (String) packetDict.get("action");
        if (action != null) {
            switch (action) {
                case "clientShutdown": {
                    onClientShutdown(packet);
                    return;
                }
                case "verifySend": {
                    String id = (String) packetDict.get("id");
                    if (!verifiedByMe.contains(id)) {
                        logger.finest(fmt(JGameStr.getString("net.UDPServer.confirmServer"),
                                address, port, id));
                        parse(NetUtils.datFromObject(packetDict.get("data")), packet);
                        verifiedByMe.add(id);
                    } else {
                        logger.fine(fmt(JGameStr.getString("net.UDP.duplicatePacket"), address, port, id));
                    }
                    HashMap<String, Object> rawSend = new HashMap<>();
                    rawSend.put("action", "confirmPacket");
                    rawSend.put("id", id);
                    send(rawSend, packet.getAddress(), packet.getPort());
                    return;
                }
                case "confirmPacket": {
                    String id = (String) packetDict.get("id");
                    if (pendingPackets.containsKey(id)) {
                        pendingPackets.get(id).onConfirm();
                        pendingPackets.remove(id);
                    }
                    logger.finest(fmt(JGameStr.getString("net.UDP.confirmedPacket"), address, port, id));
                    return;
                }
            }
        }
        clients.get(packAddr).parse(packetDict, packet);
        parse(packetDict, packet);
    }

    /**
     * Event Handler for clientShutdown. Removes the address from our list of clients.
     *
     * @param packet Shutdown packet (the packet the client sent declaring the shutdown event)
     */
    public void onClientShutdown(DatagramPacket packet) {
        clients.remove(packet.getAddress().toString() + ":" + packet.getPort());
    }

    /**
     * Calls all clients' {@link UDPClientHandler}.shutdown(). Stops all {@link #pendingPackets}
     */
    public void shutdown() {
        for (UDPClientHandler client : clients.values()) {
            try {
                client.shutdown();
            } catch (IOException e) {
                logger.log(Level.WARNING, JGameStr.getString("net.shutdownFail"), e);
            }
        }

        for (VerifyPacket p : pendingPackets.values()) {
            p.stop();
        }
    }

    /**
     * Add a VerifyPacket to {@link #pendingPackets}. (In other words, send the packet, and make sure it arrives!)
     * See {@link VerifyPacket}
     *
     * @param datagram Packet
     * @param frequency Initial delay
     * @param backoff exponential backoff
     * @param host host to send to
     * @param port port to send to
     */
    public void addVerifyPacket(HashMap<String, Object> datagram, int frequency, double backoff,
                                InetAddress host, int port) {
        VerifyPacket packet = new VerifyPacket(datagram, frequency, backoff, host, port, this);
        pendingPackets.put(packet.id, packet);
    }
}
