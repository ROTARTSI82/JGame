package io.github.jgame.net;

import io.github.jgame.Constants;
import io.github.jgame.logging.GenericLogger;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class TCPServer {
    private ServerSocket serverSocket;
    private HashMap<String, TCPClientHandler> clients = new HashMap<>();

    private InetAddress host;
    private int port;

    private int clientLimit;
    private Logger logger;

    public HashMap<String, Integer> serialTable;
    public HashMap<Integer, String> deserialTable;

    public TCPServer(String hostname, int portNum, int maxClients) throws Exception {
        clientLimit = maxClients;
        logger = Logger.getLogger(this.getClass().getName());
        host = InetAddress.getByName(hostname);
        port = portNum;
        serverSocket = new ServerSocket(port, 50, host);

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

    public void sendToAll(HashMap<String, Object> datagram) {
        for (TCPClientHandler handler : clients.values()) {
            try {
                handler.send(datagram);
            } catch (Exception e) {
                logger.warning("Failed to send packet:\n" + GenericLogger.getStackTrace(e));
            }
        }
    }

    public void acceptNewClients() throws Exception {
        Socket client = serverSocket.accept();
        String key = String.format("%s:%s", client.getInetAddress().toString(), client.getPort());
        TCPClientHandler handler = new TCPClientHandler(client, this);

        if (clients.size() >= clientLimit) {
            HashMap<String, Object> kickMsg = new HashMap<>();
            kickMsg.put("action", "kick");
            kickMsg.put("reason", "Server already full");
            handler.send(kickMsg);
            logger.info("Kicking client " + key + ": Server already full");
            return;
        }
        if (!clients.containsKey(key)) {
            clients.put(key, handler);
            logger.info("New client at " + key);
        } else {
            logger.warning("Client's address already exists! ignoring...");
        }
    }

    public HashMap<String, Integer> getActionTable() {
        return Constants.BUILTIN_ACTIONS;
    }

    public void removeClient(TCPClientHandler client) {
        clients.remove(client.toString());
    }

    public void update() {
        for (TCPClientHandler client : clients.values()) {
            try {
                client.update();
            } catch (Exception e) {
                logger.warning("Failed to update client:\n" + GenericLogger.getStackTrace(e));
            }
        }
    }

    public void parse(HashMap<String, Object> datagram, TCPClientHandler client) {

    }

    public void send(HashMap<String, Object> datagram, InetAddress datAddress, int datPort) {
        clients.get(datAddress.toString() + ":" + datPort).send(datagram);
    }

    public void shutdown() throws Exception {
        for (TCPClientHandler client : clients.values()) {
            try {
                client.shutdown();
            } catch (Exception e) {
                logger.warning("Failed to shutdown client:\n" + GenericLogger.getStackTrace(e));
            }
        }
        serverSocket.close();
    }
}
