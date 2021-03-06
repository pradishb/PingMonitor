package pingmonitor;

import java.io.*;
import java.net.*;

public class PseudoPing {
    public static int ping(String host, int timeout) throws IOException {
        long currentTime = System.currentTimeMillis();
        boolean isPinged = InetAddress.getByName(host).isReachable(timeout);
        int ping = (int) (System.currentTimeMillis() - currentTime);
        if (!isPinged) {
            throw new IOException("Ping timeout.");
        }
        if(ping > timeout)
            return timeout;
        return ping;
    }
}