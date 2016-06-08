/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import Logging.MyLogger;
import affinitySupport.Core;
import affinitySupport.ThreadAffinity;
import java.lang.management.ManagementFactory;
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
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.json.JSONObject;
import oscilloscope.Messaging;
import sensorPlatforms.AssociatedHardware;
import sensorPlatforms.IMASensor;
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
    public final Core criticalSensingCore;
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
        System.out.println("Percieved ip is "+ip+" first non loopbak is "+addr);
        registryUnitIP = "192.168.2.5";
        registryPort = 8383;
        myPort = 8282;
        threadAffinity = new ThreadAffinity(this);
        this.debug = debug;
        String jsonReply = "";

        if (threadAffinity.cores().length == 4) {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[1];
            sensingCore = threadAffinity.cores()[2];
            cronCore = threadAffinity.cores()[3];
        } else if (threadAffinity.cores().length == 2) {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[1];
            sensingCore = threadAffinity.cores()[1];
            cronCore = threadAffinity.cores()[0];
        } else if (threadAffinity.cores().length == 1) {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[0];
            sensingCore = threadAffinity.cores()[0];
            cronCore = threadAffinity.cores()[0];
        } else {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[0];
            sensingCore = threadAffinity.cores()[0];
            cronCore = threadAffinity.cores()[0];
        }

        MyLogger.log("Available cores: " + threadAffinity.cores().length);
        MyLogger.log("encryptionCore: " + criticalSensingCore);
        MyLogger.log("HTTPCore: " + HTTPCore);
        MyLogger.log("sensingCore: " + sensingCore);
        MyLogger.log("cronCore: " + cronCore);
        criticalSensingCore.setC(this);
        HTTPCore.setC(this);
        sensingCore.setC(this);
        cronCore.setC(this);

        try {

            messages = new Messaging(this);
            jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("ip=" + ip + "&port=" + myPort + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"), "/register",addr);
            //registers itself to the registry unit
            MyLogger.log("http://" + registryUnitIP+":"+registryPort+"/register"+ URLEncoder.encode("ip=" + ip + "&port=" + myPort + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"));
            if (debug) {
                MyLogger.log("reply is: " + jsonReply);
            }
            JSONObject obj;
            MyLogger.log("Error parsing this" + jsonReply);
            obj = new JSONObject(jsonReply);

            if (!obj.get("result").equals("success")) {
                if (debug) {
                    MyLogger.log("jsonReply failed " + jsonReply);
                }
            } else {
                uid = obj.getString("uid");
                if (debug) {
                    MyLogger.log("myUID is " + uid);
                }
            }

        } catch (Exception e) {
           
                MyLogger.log(jsonReply);
            
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
                    MyLogger.log("Drop daemon started");
                }
                while (true) {
                    for (IMASensor m : sensorsList) {

                        if (m.getLatestActivity() < Util.getTime() - 10000) {
                            if (debug) {
                                MyLogger.log("dropin " + m);
                            }

                            toRemove.add(m);
                        } else if (debug) {
                            MyLogger.log("Latest activity " + m.getLatestActivity());
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
                                MyLogger.log("My uid at update is " + uid);
                            }
                            String jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("uid=" + uid + "&services=" + services), "/delete",addr);
                            if (debug) {
                                MyLogger.log("reply is: " + jsonReply);
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
                        //MyLogger.log("CurrentTime " + Util.getTime());
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
                        String jsonReply="";
                        if (uid.length() > 0) {
                            try {
                                String services = "{\"services\":[";

                                services += "{\"uri\" : \"/sensor/" + sensor.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + sensor.getId() + "\"}";
                                for (Service s : sensor.getServices()) {
                                    services += ",{\"uri\" : \"/sensor/" + sensor.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "  " + sensor.getId() + "\"}";
                                }
                                services += "]}";
                                if (debug) {
                                    MyLogger.log("My uid at update is " + uid);
                                }
                                jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("uid=" + uid + "&services=" + services), "/update",addr);
                                if (debug) {
                                    MyLogger.log("reply is: " + jsonReply);
                                }
                                JSONObject obj;

                                MyLogger.log("Error parsing this" + jsonReply);
                                obj = new JSONObject(jsonReply);
                            } catch (Exception ex) {
                                Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                                MyLogger.log("Error parsing this" + jsonReply);
                            }
                        }

                    } else {
                        //Change existing services to latest standing
                        for (Service serviceFromFoundSensor : foundSensor.getServices()) {
                            for (Service ser : sensor.getServices()) {
                                if (ser.getName().contentEquals(serviceFromFoundSensor.getName())) {
                                    serviceFromFoundSensor.setDecimalValue(ser.getDecimalValue());
                                    if (ser.getName().contains("Bluetooth")) {
                                        //if (!serviceFromFoundSensor.getHw().containsKey(ser.getDecimalValue().split(" ")[0])) {
                                        serviceFromFoundSensor.getHw().put(ser.getDecimalValue().split(" ")[0], new AssociatedHardware(ser.getDecimalValue().split(" ")[0], ser.getDecimalValue().split(" ")[1]));
                                        //} else {
                                        //serviceFromFoundSensor.getHw().put(ser.getDecimalValue().split(" ")[0], new AssociatedHardware(ser.getDecimalValue().split(" ")[0], ser.getDecimalValue().split(" ")[1]));
                                        //}//add to hashmap the tag read
                                    }
                                }
                            }
                        }

                        //Add missing services
                        for (Service serviceFromReadSensor : sensor.getServices()) {
                            boolean exists = false;
                            for (Service ser : foundSensor.getServices()) {
                                if (ser.getName().contentEquals(serviceFromReadSensor.getName())) {
                                    exists = true;
                                }
                            }
                            if (!exists) {
                                foundSensor.getServices().add(serviceFromReadSensor);
                            }
                        }

                    }
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        );
        t.start();
    }

    private Thread constructPollDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    cronCore.attachTo();

                } catch (Exception ex) {
                    Logger.getLogger(Control.class
                            .getName()).log(Level.SEVERE, null, ex);
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

    public static double getProcessCpuLoad() throws Exception {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

        if (list.isEmpty()) {
            return Double.NaN;
        }

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0) {
            return Double.NaN;
        }
        // returns a percentage value with 1 decimal point precision
        return ((int) (value * 1000) / 10.0);
    }
}
