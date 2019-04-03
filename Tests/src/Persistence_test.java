import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class Persistence_test implements Runnable {
    private File server_file_1, server_file_2, server_file_3;
    private FileReader server_reader_1, server_reader_2, server_reader_3;
    private List<String> arr_server_1, arr_server_2, arr_server_3;
    private FileWriter writer;
    private boolean changed;
    public static String msg;

    public Persistence_test(){
        server_file_1 = new File ("ManufacturerServer1//Logging//logger.txt");
        server_file_2 = new File ("ManufacturerServer2//Logging//logger.txt");
        server_file_3 = new File ("ManufacturerServer3//Logging//logger.txt");

        arr_server_1 = new ArrayList<String>();
        arr_server_2 = new ArrayList<String>();
        arr_server_3 = new ArrayList<String>();
    }

    public void run(){
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(1);
                readFiles();
                compareFiles();
                writeFiles();
                if (changed) {
                    msg = "Servers synced, files were changed";
                } else {
                    msg = "Servers synced, no changes";
                }
                System.out.println(msg);
                changed = false;
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    private void readFiles(){
        try {

            server_reader_1 = new FileReader(server_file_1.getAbsoluteFile());
            server_reader_2 = new FileReader(server_file_2.getAbsoluteFile());
            server_reader_3 = new FileReader(server_file_3.getAbsoluteFile());


            BufferedReader b = new BufferedReader(server_reader_1);
            String line;
            while ((line = b.readLine()) != null){
                arr_server_1.add(line);
            }

            b = new BufferedReader(server_reader_2);
            while ((line = b.readLine()) != null){
                arr_server_2.add(line);
            }

            b = new BufferedReader(server_reader_3);
            while ((line = b.readLine()) != null){
                arr_server_3.add(line);
            }

        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    private void compareFiles() {
        if (!arr_server_1.equals(arr_server_2) || !arr_server_2.equals(arr_server_3)) {
            changed = true;
            if (arr_server_1.equals(arr_server_2)){
                arr_server_3 = arr_server_1;
            }
            if (arr_server_2.equals(arr_server_3)){
                arr_server_1 = arr_server_2;
            }
            if (arr_server_3.equals(arr_server_1)){
                arr_server_2 = arr_server_3;
            }
        }
        else {
            changed = false;
        }
    }

    private void writeFiles(){
        if (changed){
            try {
                writer = new FileWriter(server_file_1.getAbsoluteFile(), false);
                writer.write(arr_server_1.toString());
                writer.flush();

                writer = new FileWriter(server_file_2.getAbsoluteFile(), false);
                writer.write(arr_server_2.toString());
                writer.flush();

                writer = new FileWriter(server_file_3.getAbsoluteFile(), false);
                writer.write(arr_server_3.toString());
                writer.flush();
            }
            catch (Exception e){
                System.out.println(e);
            }

        }
    }

    public String getMsg(){
        return msg;
    }
}
