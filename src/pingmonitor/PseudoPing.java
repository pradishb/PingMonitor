package pingmonitor;
import java.io.*;
import java.net.*;

public class PseudoPing{
    public static int ping(String host, int timeout) throws IOException{
        long currentTime = System.currentTimeMillis();
        boolean isPinged = InetAddress.getByName(host).isReachable(timeout);
        if(!isPinged){
            return -1;
        }
        return (int)(System.currentTimeMillis() - currentTime);
    }
}