package serversocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HTTPServer extends Thread {

    private static String OUTPUT = "<html><head><title>Verteitle Systeme</title></head><body style=\"background-color: dimgrey; color:black; font-family: Calibri; font-size: 1.45em; margin: 1em; float:left;\"></body></html>";
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: text/html\r\n"
            + "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";

    private static ServerSocket serverSocket;

    public HTTPServer(ServerSocket socket) {
        serverSocket = socket;
    }

    public void run() {
        System.out.println("HTTP Server running");
        while (true) {
            try (Socket httpSocket = serverSocket.accept()) {
                InputStreamReader isr = new InputStreamReader(httpSocket.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine();
                String endpoint = "null";

                System.out.println(line);

                if ("GET / HTTP/1.1".equals(line)) {
                    endpoint = "/";
                }
                if ("GET /summary HTTP/1.1".equals(line)) {
                    endpoint = "summary";
                }
                if ("GET /bathroom HTTP/1.1".equals(line)) {
                    endpoint = "bathroom";
                }
                if ("GET /humidity HTTP/1.1".equals(line)) {
                    endpoint = "humidity";
                }
                if ("GET /temperature HTTP/1.1".equals(line)) {
                    endpoint = "temperature";
                }
                if ("GET /window HTTP/1.1".equals(line)) {
                    endpoint = "window";
                }

                while (!line.isEmpty()) { // continue reading line by line
                    System.out.println(line);
                    line = reader.readLine();
                }
                // send something back / response/res
                // Start sending our reply, using the HTTP 1.1

                String file = "";
                System.out.println("------------------------" + endpoint);

                if (endpoint == "/" || endpoint == "null") {
                    try {
                        FileReader fileReader = new FileReader("ServerSocket_Controller//index.html");
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        while ((line = bufferedReader.readLine()) != null) {
                            file = file + line;
                        }

                        // Always close files.
                        bufferedReader.close();
                    } catch (FileNotFoundException ex) {
                        System.out.println("Unable to open index.html");
                    }

                } else {
                    file = "";
                    file = buildTable(endpoint);
                }

                String weather = String.format("<p><center>%s</center></p>", buildWeatherOutput());


                OUTPUT = "<html><head><title>Verteilte Systeme</title></head><body>" + weather + "<p><meta charset=\"utf-8\" http-equiv=\"refresh\" content=\"5\" >" + file + "</p></body></html>";
                httpSocket.getOutputStream().write((OUTPUT_HEADERS + OUTPUT.length() + OUTPUT_END_OF_HEADERS + OUTPUT).getBytes(StandardCharsets.UTF_8));
                httpSocket.close();// if you don't close client's connectivity, response is not shown on client-side

            } catch (Exception e) {

            }
        }
    }

    private static String buildWeatherOutput() {

        String result = new String();
        try {

            String description_detail = ServerSocket_Controller.weatherData.get("description_detail").toString();
            String temperature = ServerSocket_Controller.weatherData.get("temperature").toString();
            //String  description =  ServerSocket_Controller.weatherData.get("description");
            String humidity = ServerSocket_Controller.weatherData.get("humidity").toString();


            result = String.format("description_detail: %s | temperature: %s | humidity: %s ", description_detail, temperature, humidity);
        } catch (JSONException e) {
            System.out.println(e);
        }

        return result;


    }

    /**
     * Builds HTML Table with all aviable Sensor Data
     *
     * @param endpoint Table to build: bathroom/summary/temperature etc
     * @return String with HTML <table></table>
     */

    private static String buildTable(String endpoint) {

        String tmp = "<table style=\"border: none; border-bottom: 1px solid black; width: 50%; padding: 15px; align-self: center; margin-left: 25%; margin-right: 25%;\" \n>";
        try {
            if (endpoint.equals("bathroom")) {
                tmp += "<th>Bathroom Fan RPM Information</th> \n";
                int length = ServerSocket_Controller.bathroomSensor.length();
                JSONObject object;
                int i = length - 1;
                while ((object = ServerSocket_Controller.bathroomSensor.getJSONObject(i)) != null) {

                    tmp += "<tr > \n" +
                            "<td> " + object.get("value") + " RPM </td> \n" +
                            "<td> From IP: " + object.get("ip") + "</td> \n" +
                            "<td> At Port: " + object.get("port") + "</td> \n" +
                            "</tr>";
                    i--;
                    if (i < 0) {
                        break;
                    }
                }
            }

            if (endpoint.equals("humidity")) {
                tmp += "<th>Humidity Information</th> \n";
                int length = ServerSocket_Controller.humiditySensor.length();
                JSONObject object;
                int i = length - 1;
                while ((object = ServerSocket_Controller.humiditySensor.getJSONObject((length - 1) - i)) != null) {

                    tmp += "<tr> \n" +
                            "<td> " + object.get("value") + "% </td> \n" +
                            "<td> From IP: " + object.get("ip") + "</td> \n" +
                            "<td> At Port: " + object.get("port") + "</td> \n" +
                            "</tr>";
                    i--;
                    if (i < 0) {
                        break;
                    }
                }
            }
            if (endpoint.equals("temperature")) {
                tmp += "<th>Bathroom Fan RPM Information</th> \n";
                int length = ServerSocket_Controller.temperatureSensor.length();
                JSONObject object;
                int i = length - 1;
                while ((object = ServerSocket_Controller.temperatureSensor.getJSONObject((length - 1) - i)) != null) {

                    tmp += "<tr> \n" +
                            "<td> " + object.get("value") + "°C </td> \n" +
                            "<td> From IP: " + object.get("ip") + "</td> \n" +
                            "<td> At Port: " + object.get("port") + "</td> \n" +
                            "</tr>";
                    i--;
                    if (i < 0) {
                        break;
                    }
                }
            }

            if (endpoint.equals("window")) {
                tmp += "<th>Bathroom Window Status</th> \n";
                int length = ServerSocket_Controller.windowSensor.length();
                JSONObject object;
                int i = length - 1;
                while ((object = ServerSocket_Controller.windowSensor.getJSONObject((length - 1) - i)) != null) {

                    tmp += "<tr> \n" +
                            "<td> The Window is" + object.get("value") + " </td> \n" +
                            "<td> From IP: " + object.get("ip") + "</td> \n" +
                            "<td> At Port: " + object.get("port") + "</td> \n" +
                            "</tr>";

                    i--;
                    if (i < 0) {
                        break;
                    }
                }
            }

            if (endpoint.equals("summary")) {
                tmp += "<th>Bathroom Fan RPM</th><th>Humidity Information</th><th>Temperature</th><th>Window is:</th> \n ";
                JSONObject obj;

                int lmax = ServerSocket_Controller.bathroomSensor.length();
                if (ServerSocket_Controller.humiditySensor.length() > lmax) {
                    lmax = ServerSocket_Controller.humiditySensor.length();
                }
                if (ServerSocket_Controller.temperatureSensor.length() > lmax) {
                    lmax = ServerSocket_Controller.temperatureSensor.length();
                }
                if (ServerSocket_Controller.windowSensor.length() > lmax) {
                    lmax = ServerSocket_Controller.windowSensor.length();
                }

                for (int i = 0; i < lmax; i++) {
                    tmp += "<tr>";

                    try {
                        obj = ServerSocket_Controller.bathroomSensor.getJSONObject((ServerSocket_Controller.bathroomSensor.length() - 1) - i);
                        tmp += "<td>" + obj.get("value") + " RPM</td>";
                    } catch (Exception e) {
                        tmp += "<td> Error </td>";
                    }

                    try {
                        obj = ServerSocket_Controller.humiditySensor.getJSONObject((ServerSocket_Controller.humiditySensor.length() - 1) - i);
                        tmp += "<td>" + obj.get("value") + "%</td>";
                    } catch (Exception e) {
                        tmp += "<td> Error </td>";
                    }

                    try {
                        obj = ServerSocket_Controller.temperatureSensor.getJSONObject((ServerSocket_Controller.temperatureSensor.length() - 1) - i);
                        tmp += "<td>" + obj.get("value") + "°C</td>";
                    } catch (Exception e) {
                        tmp += "<td> Error </td>";
                    }

                    try {
                        obj = ServerSocket_Controller.windowSensor.getJSONObject((ServerSocket_Controller.windowSensor.length() - 1) - i);
                        tmp += "<td>" + obj.get("value") + "</td>";
                    } catch (Exception e) {
                        tmp += "<td> Error </td>";
                    }

                    tmp += "</tr>";

                }

            }

            tmp += "</table>";
            //tmp += ServerSocket_Controller.weatherData;

        } catch (Exception e) {
            System.out.println(e);
        }
        return tmp;
    }

}
