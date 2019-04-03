import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;


@SuppressWarnings("Duplicates")
public class ManufacturerServer_test implements MqttCallback, Runnable {

    //File allsensors = new File("ManufacturerServer1//Logging//logger.txt");
    //FileWriter writer_table;
    String id;
    static volatile Map<String, String> map = new HashMap<String, String>();
    MqttClient mqttClient;

    public void run() {

        try {
            new ManufacturerServer_test().subscribe("Server");
        } catch (InterruptedException ex) {
            Logger.getLogger(ManufacturerServer_test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ManufacturerServer_test() {

    }

    public static void main(String[] args) throws InterruptedException, UnknownHostException {

    }

    public void subscribe(String manufacturer) throws InterruptedException {
        try {
            mqttClient = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
            mqttClient.connect();
            mqttClient.setCallback(this);
            mqttClient.subscribe(manufacturer);
        } catch (MqttException ex) {
            System.out.print(ex);

        }
        while (true) {
            try {
                fetchApiData();
                TimeUnit.SECONDS.sleep(30);
                fetchApiData();
                TimeUnit.SECONDS.sleep(30);

                //TimeUnit.MINUTES.sleep(10); //10 Minuten Timeout, da die OpenWeatherMap Daten nur alle 10 Minuten aktualisiert werden.
                //TimeUnit.MINUTES.sleep(1);
                System.out.println("1 minute over");
                Thread.currentThread().interrupt();

            }catch (Exception e){
                System.out.println("Over");
                System.out.println(map.size());
                map.clear();
            }

        }

    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        System.out.println("connection lost");
/*        try {
            writer_table.close();
        } catch (IOException ex) {
            Logger.getLogger(ManufacturerServer_test.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        while (true) {

        }
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        //writer_table = new FileWriter(allsensors.getAbsoluteFile(), true);
        String message = mm.toString();
        //writer_table.write(message);
        //writer_table.close();

        if (message.contains(" ")) {
            id = message.substring(0, message.indexOf(" "));
        }

        map.put(id, message);
        System.err.println("Manufacturer Test Empfangen: " + message);


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

    private void fetchApiData() {
        try {
            JSONObject api_data = new JSONObject();
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?id=2938912&APPID=2cde722f12d029f9103d5b1318add30d&units=metric");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            JSONObject jsonObject = new JSONObject(content.toString());

            JSONArray weather_arr = jsonObject.getJSONArray("weather");
            JSONObject weather_desc = weather_arr.getJSONObject(0);
            JSONObject weather_info = jsonObject.getJSONObject("main");

            api_data.put("description", weather_desc.get("main"));
            api_data.put("description_detail", weather_desc.get("description"));
            api_data.put("temperature", weather_info.get("temp"));
            api_data.put("humidity", weather_info.get("humidity"));

            System.err.println("\nWeather JSON Empfangen: " + api_data);

            sendDataToMQTT(api_data.toString());


        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void sendDataToMQTT(String msg) throws MqttException {

        System.out.println("== START PUBLISHER ==");


        MqttClient client = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
        client.connect();
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes());
        client.publish("weatherData", message);

        System.out.println("\tWeatherData published from Hersteller '" + msg);

        client.disconnect();

        System.out.println("== END PUBLISHER ==");

    }

}
