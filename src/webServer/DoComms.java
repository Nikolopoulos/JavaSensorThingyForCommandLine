/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webServer;

import DecisionMaking.DecisionMaking;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Control;
import sensorPlatforms.IMASensor;
import Logging.MyLogger;

/**
 *
 * @author billaros
 */
class DoComms implements Runnable {

    private Socket server;
    private String line, input, requestedURL, noBreakInput;
    private final Control con;

    private final DecisionMaking dm;

    DoComms(Socket server, Control c) {
        this.server = server;
        this.con = c;
        dm = new DecisionMaking(c);
    }

    public void run() {

        try {
            con.HTTPCore.attachTo();
        } catch (Exception ex) {
            Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
        }
        input = "";
        noBreakInput = "";
        requestedURL = "";
        try {
            // Get input from the client
            DataInputStream in = new DataInputStream(server.getInputStream());
            PrintStream out = new PrintStream(server.getOutputStream());
            int lineNumber = 1;
            while ((line = in.readLine()) != null && line.length() > 0) {
                if (lineNumber == 1) {
                    String[] parts = line.split(" ");
                    requestedURL = parts[1];
                    MyLogger.log("REQURL IS " + requestedURL);
                }
                input = input + line + "\n";
                noBreakInput = noBreakInput + line;
                lineNumber++;
            }
            String reply = "";
            for (String part : requestedURL.split("/")) {
                MyLogger.log(part + " " + requestedURL.split("/").length);
            }
            if (requestedURL.equals("/")) {
                reply = con.ip + ":" + con.myPort + "/sensors -> returns a list of sensors available\n"
                        + con.ip + ":" + con.myPort + "/sensor/ID -> returns data of specific sensor with id = ID\n"
                        + con.ip + ":" + con.myPort + "/sensor/ID/ServiceName -> returns data from ServiceName runnign on Sensor ID\n";
            } else if (requestedURL.startsWith("/sensors")) {
                reply = "{\"sensors\":{[";
                for (IMASensor m : con.getMotesList()) {
                    reply += m.JSONDescription() + ",";
                }
                if (reply.length() > 10) {
                    reply = reply.substring(0, reply.length() - 1);
                }
                reply += "]}";

            }else if (requestedURL.startsWith("/log")) {
                reply = MyLogger.readLog();

            } else if (requestedURL.startsWith("/sensor/") && requestedURL.split("/").length < 4) {

                int ID = Integer.parseInt(requestedURL.split("/")[2]);
                reply = "{\"sensor\":{";
                for (IMASensor m : con.getMotesList()) {
                    if (m.getId() == ID) {
                        reply += m.JSONObject();
                    }
                }
                reply += "}}";

            } else if (requestedURL.startsWith("/sensor/")) {

                reply = "{\"sensor\":{";
                int threadID = dm.add(requestedURL);
                String returnVal = "";
                while (returnVal.equalsIgnoreCase("")) {
                    returnVal = dm.getResultOf(threadID);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                reply += returnVal + "}}";

            }

            out.println(reply);
            out.flush();
            out.close();
            server.close();
        } catch (IOException ioe) {
            MyLogger.log("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }
}
