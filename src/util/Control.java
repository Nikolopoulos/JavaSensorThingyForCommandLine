/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import affinitySupport.Core;
import affinitySupport.ThreadAffinity;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import oscilloscope.Messaging;
import sensorPlatforms.IMASensor;
import sensorPlatforms.MicazMote;
import sensorPlatforms.Service;

/**
 *
 * @author billaros
 */
public class Control {

    ArrayList<IMASensor> sensorsList;
    Messaging messages;
    Thread dropDaemon, populate;
    String uid = "";
    boolean debug;
    public ThreadAffinity threadAffinity;
    public final Core encryptionCore;
    public final Core HTTPCore;
    public final Core sensingCore;
    public final Core cronCore;
    public InetAddress addr = null;// = getFirstNonLoopbackAddress(true, false);
    public final String ip;// = addr.getHostAddress();
    public final String registryUnitIP;// = "127.0.0.1";
    public final int registryPort;// = 8383;
    public final int myPort;// = 8282;

    public Control(boolean debug) {
        try {
            addr = getFirstNonLoopbackAddress(true, false);
        } catch (SocketException ex) {
            System.exit(1);
        }
        ip = addr.getHostAddress();
        registryUnitIP = "127.0.0.1";
        registryPort = 8383;
        myPort = 8282;
        threadAffinity = new ThreadAffinity(this);
        this.debug = debug;
        String jsonReply = "";

        if (threadAffinity.cores().length == 4) {
            encryptionCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[1];
            sensingCore = threadAffinity.cores()[2];
            cronCore = threadAffinity.cores()[3];
        } else if (threadAffinity.cores().length == 2) {
            encryptionCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[1];
            sensingCore = threadAffinity.cores()[1];
            cronCore = threadAffinity.cores()[0];
        } else if (threadAffinity.cores().length == 1) {
            encryptionCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[0];
            sensingCore = threadAffinity.cores()[0];
            cronCore = threadAffinity.cores()[0];
        } else {
            encryptionCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[0];
            sensingCore = threadAffinity.cores()[0];
            cronCore = threadAffinity.cores()[0];
        }

        System.out.println("Available cores: " + threadAffinity.cores().length);
        System.out.println("encryptionCore: " + encryptionCore);
        System.out.println("HTTPCore: " + HTTPCore);
        System.out.println("sensingCore: " + sensingCore);
        System.out.println("cronCore: " + cronCore);
        encryptionCore.setC(this);
        HTTPCore.setC(this);
        sensingCore.setC(this);
        cronCore.setC(this);

        try {

            messages = new Messaging(this);
            jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("ip=" + ip + "&port=" + myPort + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"), "/register");
            //registers itself to the registry unit

            if (debug) {
                System.out.println("reply is: " + jsonReply);
            }
            JSONObject obj;

            obj = new JSONObject(jsonReply);

            if (!obj.get("result").equals("success")) {
                if (debug) {
                    System.out.println("jsonReply failed " + jsonReply);
                }
            } else {
                uid = obj.getString("uid");
                if (debug) {
                    System.out.println("myUID is " + uid);
                }
            }

        } catch (Exception e) {
            if (debug) {
                System.out.println(jsonReply);
            }
            e.printStackTrace();
        }
        sensorsList = new ArrayList<IMASensor>();
        dropDaemon = createDropDaemon();
        dropDaemon.start();
        populate = constructPollDaemon();
        populate.start();
    }

    private synchronized Thread createDropDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    cronCore.attachTo();
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
                ArrayList<IMASensor> toRemove = new ArrayList<IMASensor>();
                if (debug) {
                    System.out.println("Drop daemon started");
                }
                while (true) {
                    for (IMASensor m : sensorsList) {

                        if (m.getLatestActivity() < Util.getTime() - 10000) {
                            if (debug) {
                                System.out.println("dropin " + m);
                            }

                            toRemove.add(m);
                        } else if (debug) {
                            System.out.println("Latest activity " + m.getLatestActivity());
                        }
                    }
                    for (IMASensor m : toRemove) {
                        try {
                            String services = "{\"services\":[";

                            services += "{\"uri\" : \"/sensor/" + m.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + m.getId() + "\"}";
                            for (Service s : m.getServices()) {
                                services += ",{\"uri\" : \"/sensor/" + m.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "  " + m.getId() + "\"}";
                            }
                            services += "]}";
                            if (debug) {
                                System.out.println("My uid at update is " + uid);
                            }
                            String jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("uid=" + uid + "&services=" + services), "/delete");
                            if (debug) {
                                System.out.println("reply is: " + jsonReply);
                            }
                            JSONObject obj;

                            obj = new JSONObject(jsonReply);
                        } catch (Exception ex) {
                            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        sensorsList.remove(m);
                        //sendDeleteRequestToRU
                    }
                    toRemove.clear();
                    if (debug) {
                        //System.out.println("CurrentTime " + Util.getTime());
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        return t;
    }

    public void reportReadingOfSensor(final IMASensor sensor) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    sensingCore.attachTo();

                    boolean found = false;
                    IMASensor foundSensor = null;
                    for (IMASensor m : sensorsList) {
                        if (m.getId() == sensor.getId()) {
                            foundSensor = m;
                            m.setLatestActivity(Util.getTime());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        sensorsList.add(sensor);
                        String jsonReply;
                        if (uid.length() > 0) {
                            try {
                                String services = "{\"services\":[";

                                services += "{\"uri\" : \"/sensor/" + sensor.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + sensor.getId() + "\"}";
                                for (Service s : sensor.getServices()) {
                                    services += ",{\"uri\" : \"/sensor/" + sensor.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "  " + sensor.getId() + "\"}";
                                }
                                services += "]}";
                                if (debug) {
                                    System.out.println("My uid at update is " + uid);
                                }
                                jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("uid=" + uid + "&services=" + services), "/update");
                                if (debug) {
                                    System.out.println("reply is: " + jsonReply);
                                }
                                JSONObject obj;

                                obj = new JSONObject(jsonReply);
                            } catch (Exception ex) {
                                Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    } else {
                        for(Service s : foundSensor.getServices()){
                            boolean foundService=false;
                            for(Service ser : sensor.getServices()){
                                if(ser.getName().contentEquals(s.getName())){
                                    foundService = true;
                                    s.setDecimalValue(ser.getDecimalValue());
                                    if(ser.getName().contains("Bluetooth")){
                                        if(s.getHw().containsKey(ser.getDecimalValue().split(" ")[0])){
                                            s.getHw().put(ser, value)
                                        }//add to hashmap the tag read
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    private Thread constructPollDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    cronCore.attachTo();
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
                Thread serial = messages.ReadSerial();
                serial.start();
            }
        });
        return t;
    }

    public ArrayList<IMASensor> getMotesList() {
        return this.sensorsList;
    }

    //courtesy of How to get the ip of the computer on linux through Java? -> http://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }

}
