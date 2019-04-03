import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class PersistenceServer1 implements Runnable {
    private File server_file_1;
    private Map<String, String> map_serv1, map_serv2, map_serv3;
    private FileWriter writer;
    private boolean changed;
    String server_ip_2 = "localhost";
    String server_ip_3 = "localhost";
    int rcv_port_2 = 11005;
    int rcv_port_3 = 11007;
    int send_port_2 = 11004;
    int send_port_3 = 11003;

    public PersistenceServer1(){
        server_file_1 = new File ("ManufacturerServer1//Logging//logger.txt");
    }

    public void run(){
        try{
            TimeUnit.SECONDS.sleep(10);
        }catch (Exception e){
            System.out.println(e);
        }
        while (true) {
            try {
                sendMap();
                readFiles();
                readFiles();
                compareFiles();
                writeFiles();
                if (changed) {
                    System.out.println("Servers synced, files were changed");
                } else {
                    System.out.println("Servers synced, no changes");
                }
                changed = false;
            } catch (Exception e) {
                System.out.println("error in main loop " + e);
            }
        }

    }

    private void readFiles(){
        try {
            map_serv1 = ManufacturerServer.map;

            ServerSocket serverSocket2 = new ServerSocket(rcv_port_2);
            try (Socket s2 = serverSocket2.accept()) {
                ObjectInputStream in2 = new ObjectInputStream(s2.getInputStream());
                map_serv2 = (Map<String, String>) in2.readObject();
            }catch (Exception e){
                System.out.println("server 1 read from 2" + e);
            }

            ServerSocket serverSocket3 = new ServerSocket(rcv_port_3);
            try (Socket s3 = serverSocket3.accept()){
                ObjectInputStream in3 = new ObjectInputStream(s3.getInputStream());
                map_serv3 = (Map<String, String>) in3.readObject();
            }catch (Exception e){
                System.out.println("server 1 read from 3" + e);
            }

    }
        catch (Exception e){
            System.out.println("error creating socket " + e);
        }
    }

    private void compareFiles() {
        if (!map_serv1.equals(map_serv2) || !map_serv2.equals(map_serv3)) {
            changed = true;
            if (map_serv1.equals(map_serv2)){
                map_serv3 = map_serv1;
            }
            if (map_serv2.equals(map_serv3)){
                map_serv1 = map_serv2;
            }
            if (map_serv3.equals(map_serv1)){
                map_serv2 = map_serv3;
            }
        }
        else {
            changed = false;
        }
    }

    private void writeFiles(){
        if (changed){
            try {
                ManufacturerServer.map = map_serv1;

                writer = new FileWriter(server_file_1.getAbsoluteFile(), false);
                for (int i = 0; i < map_serv1.size(); i++) {
                    writer.write(map_serv1.get(i) + "\n");
                }
                writer.flush();
            }
            catch (Exception e){
                System.out.println("error writing files" + e);
            }

        }
    }

    private void sendMap(){
        try {
            Socket s = new Socket(server_ip_2, send_port_2);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(ManufacturerServer.map);
            objectOutputStream.flush();
            objectOutputStream.close();

            s = new Socket(server_ip_3, send_port_3);
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(ManufacturerServer.map);
            objectOutputStream.flush();
            objectOutputStream.close();
        }
        catch (Exception e){
            System.out.println("error sending files " + e);
        }
    }
}
