import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.json.*;

public class clientsocket_humidity {

    public static void main(String[] args) throws Exception {

        String ip;
        if (args.length == 0){
            ip = "localhost";
        }
        else {
            ip = args[0];
        }

        JSONObject obj = new JSONObject();

        try (DatagramSocket s = new DatagramSocket()) {
            byte[] buf = new byte[1000];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            InetAddress hostAddress = InetAddress.getByName(ip);

            while (true) {
                int random = (int) ((Math.random()) * 100 + 1); //random numbers from 7 to 30

                obj.put("sensor", 1);
                obj.put("value", random);

                buf = obj.toString().getBytes("utf-8");
                DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, 9993);
                s.send(out);

                System.out.println(obj.toString());

                TimeUnit.SECONDS.sleep(1);
            }
        }
        catch (Exception e){
            System.out.println(e);
        }

    }

}
