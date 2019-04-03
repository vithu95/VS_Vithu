import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class PersistenceServer3 implements Runnable {
    private File server_file_3;
    private Map<String, String> map_serv1, map_serv2, map_serv3;
    private FileWriter writer;
    private boolean changed;
    String server_ip_1 = "localhost";
    String server_ip_2 = "localhost";
    int rcv_port_1 = 11003;
    int rcv_port_2 = 11006;
    int send_port_1 = 11007;
    int send_port_2 = 11008;

    public PersistenceServer3(){

        server_file_3 = new File ("ManufacturerServer1//Logging//logger.txt");
    }

    public void run(){
        while (true) {
            try {
                readFiles();
                readFiles();
                sendMap();
                compareFiles();
                writeFiles();
                if (changed) {
                    System.out.println("Servers synced, files were changed");
                } else {
                    System.out.println("Servers synced, no changes");
                }
                changed = false;
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    private void readFiles(){
        try {
            ServerSocket serverSocket1 = new ServerSocket(rcv_port_1);
            try (Socket s1 = serverSocket1.accept()) {
                ObjectInputStream in1 = new ObjectInputStream(s1.getInputStream());
                map_serv1 = (Map<String, String>) in1.readObject();
            }catch (Exception e){
                System.out.println("read from 1 " + e);
            }

            ServerSocket serverSocket2 = new ServerSocket(rcv_port_2);
            try (Socket s2 = serverSocket2.accept()) {
                ObjectInputStream in2 = new ObjectInputStream(s2.getInputStream());
                map_serv2 = (Map<String, String>) in2.readObject();
            }catch (Exception e){
                System.out.println("read from 2 " + e);
            }

            map_serv3 = ManufacturerServer3.map;

        }
        catch (Exception e){
            System.out.println(e);
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
                ManufacturerServer3.map = map_serv3;

                writer = new FileWriter(server_file_3.getAbsoluteFile(), false);
                for (int i = 0; i < map_serv3.size(); i++) {
                    writer.write(map_serv3.get(i) + "\n");
                }
                writer.flush();
            }
            catch (Exception e){
                System.out.println(e);
            }

        }
    }

    private void sendMap(){
        try {
            Socket s = new Socket(server_ip_1, send_port_1);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(ManufacturerServer3.map);
            objectOutputStream.flush();
            objectOutputStream.close();

            s = new Socket(server_ip_2, send_port_2);
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(ManufacturerServer3.map);
            objectOutputStream.flush();
            objectOutputStream.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
