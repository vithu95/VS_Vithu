import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class PersistenceServer2 implements Runnable {
    private File server_file_2;
    private Map<String, String> map_serv1, map_serv2, map_serv3;
    private FileWriter writer;
    private boolean changed;
    String server_ip_1 = "localhost";
    String server_ip_3 = "localhost";
    int rcv_port_1 = 11004;
    int rcv_port_3 = 11008;
    int send_port_1 = 11005;
    int send_port_3 = 11006;

    public PersistenceServer2(){
        server_file_2 = new File ("ManufacturerServer1//Logging//logger.txt");
    }

    public void run(){
        while (true) {
            try {
                readFiles();
                sendMap();
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
                System.out.println("read from 1" + e);
            }

            map_serv1 = ManufacturerServer2.map;

            ServerSocket serverSocket3 = new ServerSocket(rcv_port_3);
            try (Socket s3 = serverSocket3.accept()) {
                ObjectInputStream in3 = new ObjectInputStream(s3.getInputStream());
                map_serv3 = (Map<String, String>) in3.readObject();
            }catch (Exception e){
                System.out.println("read from 3" + e);
            }
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
                ManufacturerServer2.map = map_serv2;
                
                writer = new FileWriter(server_file_2.getAbsoluteFile(), false);
                for (int i = 0; i < map_serv2.size(); i++) {
                    writer.write(map_serv2.get(i) + "\n");
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
            objectOutputStream.writeObject(ManufacturerServer2.map);
            objectOutputStream.flush();
            objectOutputStream.close();
            
            s = new Socket(server_ip_3, send_port_3);
            objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(ManufacturerServer2.map);
            objectOutputStream.flush();
            objectOutputStream.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
