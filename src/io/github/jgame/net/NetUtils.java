package io.github.jgame.net;

import io.github.jgame.Version;
import io.github.jgame.logging.GenericLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class NetUtils {
    private static Logger logger = Logger.getLogger("io.github.jgame.net.NetUtils");
    public static String extractString(DatagramPacket packet) {
        return new String(packet.getData()).replace("\0", "");
    }

    /**
     * Serialize data for use in TCP or UDP
     *
     * @param data        A HashMap containing data (key 'action' would be serialized using {@code actionTable})
     * @param actionTable A HashMap of actions (integer actionIDs should be 4 digits of hex to be encoded
     *                    into 2 bytes appended at the front of the returned byte array)
     * @return serialized data in the form of a byte array
     */
    public static byte[] serialize(HashMap<String, Object> data, HashMap<String, Integer> actionTable) {
        try {
            String action = (String) data.get("action");
            data.remove("action");

            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream serializer = new ObjectOutputStream(bytesOut);
            serializer.writeObject(data);
            serializer.close();

            byte[] ret = new byte[bytesOut.size() + 2];
            byte[] rawSerial = bytesOut.toByteArray();
            System.arraycopy(rawSerial, 0, ret, 2, rawSerial.length);

            if (action != null && actionTable.containsKey(action)) {
                int actionID = actionTable.get(action);
                ret[1] = (byte) (actionID % 256);
                ret[0] = (byte) ((actionID / 256) % 256);
            } else {
                logger.info(String.format("Error serializing action: null or invalid action '%s'", action));
            }
            return ret;
        } catch (Exception e) {
            logger.info(String.format("Serialization failed: %s\n%s ", data.toString(),
                    GenericLogger.getStackTrace(e)));
        }
        return new byte[0];
    }

    public static HashMap<String, Object> datFromObject(Object o) {
        if (o instanceof HashMap) {
            HashMap<String, Object> finalOut = new HashMap<>();
            HashMap objMap = (HashMap) o;
            for (Object key : objMap.keySet()) {
                if (key instanceof String) {
                    finalOut.put((String) key, objMap.get(key));
                } else {
                    logger.info("Failed to deserialize key: " + key.toString());
                }
            }
            return finalOut;
        } else {
            logger.info("Deserialization didn't return HashMap: " + o.toString());
        }
        return null;
    }

    public static HashMap<String, Object> deserialize(byte[] bytes,
                                                      HashMap<Integer, String> actionTable) {
        try {
            byte[] readBytes = new byte[bytes.length - 2];
            System.arraycopy(bytes, 2, readBytes, 0, bytes.length - 2);

            int actionID = Version.unsigned(bytes[0]) * 256 + Version.unsigned(bytes[1]);

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(readBytes));
            Object o = ois.readObject();
            ois.close();

            HashMap<String, Object> ret = datFromObject(o);
            if (ret != null) {
                ret.put("action", actionTable.get(actionID));
                logger.finest("Got action=" + ret.get("action"));
            }
            return ret;
        } catch (Exception e) {
            logger.info(String.format("Deserialization failed: %s\n%s ",
                    Arrays.toString(bytes), GenericLogger.getStackTrace(e)));
        }
        return null;
    }
}
