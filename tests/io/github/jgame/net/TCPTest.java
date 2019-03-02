package io.github.jgame.net;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.logging.Logger;

import static io.github.jgame.util.StringManager.fmt;
import static io.github.jgame.util.UniversalResources.JGameStr;

public class TCPTest {
    private static Logger logger;

    @BeforeSuite
    public void setUp() {
        logger = Logger.getLogger(this.getClass().getName());
    }

    @Test(timeOut = 2000)
    public void testTCP() throws Exception {
        TestServer server = new TestServer();
        TestClient client = new TestClient();
        while (!client.done) {
            server.acceptNewClients();
            server.update();
            client.update();
        }
        server.shutdown();
        client.shutdown();
    }

    static class TestServer extends TCPServer {
        public TestServer() throws Exception {
            super("127.0.0.1", 3000, 1);
        }

        @Override
        public void parse(HashMap<String, Object> datagram, TCPClientHandler client) {
            logger.info(fmt(JGameStr.getString("net.Test.serverGot"), datagram));
            HashMap<String, Object> ack = new HashMap<>();
            ack.put("ack", datagram.get("id"));
            client.send(ack);
        }
    }

    static class TestClient extends TCPClient {
        long id;
        private boolean done = false;

        public TestClient() throws Exception {
            super("127.0.0.1", 3000);
            id = System.nanoTime();
            HashMap<String, Object> dat = new HashMap<>();
            dat.put("id", id);
            send(dat);
        }

        @Override
        public void parse(HashMap<String, Object> datagram) {
            logger.info(fmt(JGameStr.getString("net.Test.clientGot"), datagram));
            if (((long) datagram.get("ack")) == id) {
                done = true;
            }
        }
    }
}