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
public class CheckForPersistence_test {
    private static File server_file_1, server_file_2, server_file_3;
    private static FileReader server_reader_1, server_reader_2, server_reader_3;
    private static List<String> arr_server_1, arr_server_2, arr_server_3;
    private static BufferedReader b_reader_1, b_reader_2, b_reader_3;

    public static void main(String[] args) {

        server_file_1 = new File("ManufacturerServer1//Logging//logger.txt");
        server_file_2 = new File("ManufacturerServer2//Logging//logger.txt");
        server_file_3 = new File("ManufacturerServer3//Logging//logger.txt");

        arr_server_1 = new ArrayList<String>();
        arr_server_2 = new ArrayList<String>();
        arr_server_3 = new ArrayList<String>();

            try {
                server_reader_1 = new FileReader(server_file_1.getAbsoluteFile());
                server_reader_2 = new FileReader(server_file_2.getAbsoluteFile());
                server_reader_3 = new FileReader(server_file_3.getAbsoluteFile());

                b_reader_1 = new BufferedReader(server_reader_1);
                b_reader_2 = new BufferedReader(server_reader_2);
                b_reader_3 = new BufferedReader(server_reader_3);

                String s;

                while ((s = b_reader_1.readLine()) != null){
                    arr_server_1.add(s);
                }

                while ((s = b_reader_2.readLine()) != null){
                    arr_server_2.add(s);
                }

                while ((s = b_reader_3.readLine()) != null){
                    arr_server_3.add(s);
                }

                if (arr_server_1.equals(arr_server_2) && arr_server_2.equals(arr_server_3)){
                    System.out.println("The Servers are Synchronized");
                }
                else {
                    System.out.println("The Servers are not Synchronized");
                }

            } catch (Exception e) {
                System.out.println(e);
            }


    }
}