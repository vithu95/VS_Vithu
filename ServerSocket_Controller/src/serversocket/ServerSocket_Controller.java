package serversocket;

import java.io.*;
import java.net.*;

import org.json.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/*
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
*/
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.eclipse.paho.client.mqttv3.*;

@SuppressWarnings("Duplicates")
public class ServerSocket_Controller implements MqttCallback {

    public static JSONArray bathroomSensor = new JSONArray();
    public static JSONArray humiditySensor = new JSONArray();
    public static JSONArray temperatureSensor = new JSONArray();
    public static JSONArray windowSensor = new JSONArray();
    public static JSONObject weatherData = new JSONObject();

    private static ServerSocket serverSocket;

    public static void mqttPublish(String top, String cont, int qualServ, String clientIdent) {
        String topic = top;
        String content = cont;
        int qos = qualServ; //quality of service
        String broker = "tcp://iot.eclipse.org:1883";       //"tcp://localhost:1883"
        String clientId = clientIdent;
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected\n\n---");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        int sensorPort = 9993;
        int httpPort = 9000;
        int id = 0;
        boolean persistentLog = false; //while false it only saves the last 250 datapoints for each sensor, while true it appends
        byte[] buffer = new byte[1024];

        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        DatagramSocket socket = new DatagramSocket(sensorPort);

        socket.setSoTimeout(7000);

        JSONObject jsonObject;

        new ServerSocket_Controller().recieveWeatherAPI();


        try {
            serverSocket = new ServerSocket(httpPort);
        } catch (IOException e) {
            System.out.println("Error opening HTTP Server Socket: " + e);
        }

        //Start http Server Thread
        HTTPServer httpServer = new HTTPServer(serverSocket);
        httpServer.start();

        /**
         * Permanent loop for recieving information at socket, sorting and saving data.
         */

        while (true) {
            try {
                socket.receive(datagramPacket);

                String tmp = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                String ip = datagramPacket.getAddress().toString();
                int port = datagramPacket.getPort();

                jsonObject = new JSONObject(new JSONTokener(tmp));
                jsonObject.put("ip", ip);
                jsonObject.put("port", port);

                sortJSON(jsonObject);


            } catch (SocketTimeoutException e) {
                System.out.println("Sensor socket timeout!");
            }

            saveJSON(persistentLog);

            /**
             * Parsing String to publish via MQTT
             */

            String mqtt = id + "  ";
            if (bathroomSensor.length() > 0) {
                jsonObject = bathroomSensor.getJSONObject(bathroomSensor.length() - 1);
                mqtt += jsonObject.get("value") + "RPM ";
            }
            if (humiditySensor.length() > 0) {
                jsonObject = humiditySensor.getJSONObject(humiditySensor.length() - 1);
                mqtt += jsonObject.get("value") + "% ";
            }
            if (temperatureSensor.length() > 0) {
                jsonObject = temperatureSensor.getJSONObject(temperatureSensor.length() - 1);
                mqtt += jsonObject.get("value") + "°C ";
            }
            if (windowSensor.length() > 0) {
                jsonObject = windowSensor.getJSONObject(windowSensor.length() - 1);
                mqtt += "window:" + jsonObject.get("value") + "\n";
            }
            id++;

            System.out.println(mqtt);

            mqttPublish("Server", mqtt, 2, "1");

        }


    }

    /**
     * sorts recieved json data in the sensor arrays and print recieved data
     * @param object Json Object to sort
     */
    private static void sortJSON(JSONObject object) {
        try {
            switch (object.get("sensor").toString()) {
                case "0": {
                    object.remove("sensor");
                    bathroomSensor.put(object);

                    System.out.println("Bathroom fan RPM: " + object.get("value") + " \t Ip: " + object.get("ip") + "\t Port: " + object.get("port"));
                    break;
                }
                case "1": {
                    object.remove("sensor");
                    humiditySensor.put(object);

                    System.out.println("Humidity: " + object.get("value") + "% \t \t \t Ip: " + object.get("ip") + "\t Port: " + object.get("port"));
                    break;
                }
                case "2": {
                    object.remove("sensor");
                    temperatureSensor.put(object);

                    System.out.println("Temperature: " + object.get("value") + "°C \t \t Ip: " + object.get("ip") + "\t Port: " + object.get("port"));
                    break;
                }
                case "3": {
                    object.remove("sensor");
                    windowSensor.put(object);

                    System.out.println("Window is: " + object.get("value") + " \t \t Ip: " + object.get("ip") + "\t Port: " + object.get("port"));
                    break;
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * save JSON sensor array data to file if the array is >250
     *
     * @param persistentLog if true: save more than the last 250 elements
     */
    private static void saveJSON(boolean persistentLog) {

        if (bathroomSensor.length() > 250) {
            try (FileWriter fileWriter = new FileWriter(new File("ServerSocket_Controller//Logging//BathroomSensor.json").getAbsoluteFile(), persistentLog)) {
                fileWriter.write(bathroomSensor.toString());
                fileWriter.flush();

                bathroomSensor = new JSONArray();

            } catch (IOException e) {
                System.out.println(e);
            }
        }

        if (humiditySensor.length() > 250) {
            try (FileWriter fileWriter = new FileWriter(new File("ServerSocket_Controller//Logging//HumiditySensor.json").getAbsoluteFile(), persistentLog)) {
                fileWriter.write(humiditySensor.toString());
                fileWriter.flush();

                humiditySensor = new JSONArray();

            } catch (IOException e) {
                System.out.println(e);
            }
        }

        if (temperatureSensor.length() > 250) {
            try (FileWriter fileWriter = new FileWriter(new File("ServerSocket_Controller//Logging//TemperatureSensor.json").getAbsoluteFile(), persistentLog)) {
                fileWriter.write(temperatureSensor.toString());
                fileWriter.flush();

                temperatureSensor = new JSONArray();
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        if (windowSensor.length() > 250) {
            try (FileWriter fileWriter = new FileWriter(new File("ServerSocket_Controller//Logging//WindowSensor.json").getAbsoluteFile(), persistentLog)) {
                fileWriter.write(windowSensor.toString());
                fileWriter.flush();

                windowSensor = new JSONArray();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public void recieveWeatherAPI() throws MqttException {
        System.out.println("== START SUBSCRIBER FROM WEBSERVER ==");

        MqttClient client = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
        client.setCallback(this);
        client.connect();

        client.subscribe("weatherData");


    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String msgAPI = new String(mqttMessage.getPayload());
        System.out.println("Message received from publisherHersteller:\t" + msgAPI);
        processData(msgAPI);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    public void processData(String data) {
        System.out.println(data);
        try (FileWriter fileWriter = new FileWriter(new File("ServerSocket_Controller//Logging//WeatherData.json").getAbsoluteFile())) {
            fileWriter.write(data);
            fileWriter.flush();

            weatherData = new JSONObject(data);
        } catch (IOException e) {
            System.out.println(e);
        } catch (JSONException e) {
            System.out.println(e);

        }
    }
}

