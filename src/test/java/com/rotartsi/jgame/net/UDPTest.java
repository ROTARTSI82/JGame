package com.rotartsi.jgame.net;

import com.rotartsi.jgame.net.udp.UDPClient;
import com.rotartsi.jgame.net.udp.UDPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;

import static com.rotartsi.jgame.Constants.JGameStr;
import static com.rotartsi.jgame.util.StringManager.fmt;

public class UDPTest {
    private Server testServ;
    private Client testCli;
    private Logger logger = LogManager.getLogger(UDPTest.class);

    @Test(timeOut = 2000)
    public void testUDP() throws Exception {
        testServ = new Server("", 3000);
        testCli = new Client("", 3000, System.nanoTime());
        while (!testCli.done) {
            //Thread.sleep(25);
            testServ.update();
            testCli.update();
        }
        testCli.shutdown();
        testServ.shutdown();
    }

    @AfterSuite
    public void tearDown() {
        testServ.shutdown();
        try {
            testCli.shutdown();
        } catch (IOException e) {
            logger.warn(JGameStr.getString("net.shutdownFail"), e);
        }
    }

    private class Server extends UDPServer {
        Server(String host, int port) throws Exception {
            super(host, port, 1);
        }

        @Override
        public void parse(HashMap<String, Object> datagram, DatagramPacket packet) {
            logger.info(fmt(JGameStr.getString("net.Test.serverGot"), datagram));
            HashMap<String, Object> dat = new HashMap<>();
            if (datagram.containsKey("id")) {
                dat.put("ack", datagram.get("id"));
            } else {
                return;
            }
            try {
                this.addVerifyPacket(dat, 1, 2, packet.getAddress(), packet.getPort());
                //this.send(dat, packet.getAddress(), packet.getPort());
            } catch (Exception e) {
                logger.warn(JGameStr.getString("net.UDPTest.addFail"), e);
            }
        }
    }

    private class Client extends UDPClient {
        long id;
        boolean done = false;

        Client(String host, int port, long myID) throws Exception {
            super(host, port);
            id = myID;

            HashMap<String, Object> dat = new HashMap<>();
            dat.put("id", id);
            this.addVerifyPacket(dat, 1, 2);
            //this.send(dat);
        }

        @Override
        public void parse(HashMap<String, Object> datagram, DatagramPacket packet) {
            logger.info(fmt(JGameStr.getString("net.Test.clientGot"), datagram));
            done = ((long) datagram.get("ack")) == id;
        }
    }
}
