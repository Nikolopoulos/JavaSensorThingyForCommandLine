/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oscilloscope;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import sensorPlatforms.AssociatedHardware;
import sensorPlatforms.IMASensor;
import sensorPlatforms.Service;
import util.Control;

/**
 *
 * @author billaros
 */
public class Messaging {

    private final Control c;

    public Messaging(Control c) {
        this.c = c;
    }

    public Thread ReadSerial() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                CommPortIdentifier portIdentifier;
                SerialPort serialPort=null;

                while (true) {
                    try {
                        portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"); //on unix based system
                        serialPort = (SerialPort) portIdentifier.open("NameOfConnection-whatever", 0);

                        serialPort.setSerialPortParams(
                                115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                        boolean active = true;
                        InputStream inputStream = serialPort.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String s = br.readLine();
                        while (true) {
                            try {
                                if (s.charAt(0) != '#') {
                                    parseString(s);
                                    //System.out.println(s);

                                }
                                s = br.readLine();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        serialPort.close();
                    }

                }
            }
        });

        return t;

    }

    public void parseString(String s) {
        IMASensor dealer = null;
        if (s.startsWith("TH")) {
            String parts[] = s.split(" ");
            String id = parts[1];
            String Temperature = parts[2];
            String Humdity = parts[3];
            String Pressure = parts[4];
            dealer = new IMASensor(id);

            Service temperature = new Service("Temperature", "Provides the temperature in some units or something", "/temperature", "Kelvious");
            temperature.setDecimalValue(Temperature);
            Service humidity = new Service("Humidity", "Provides the humidity in some units or something", "/humidity", "MistUnits");
            humidity.setDecimalValue(Humdity);
            Service pressure = new Service("Pressure", "Provides the atmospheric pressure in some units or something", "/pressure", "Easters");
            pressure.setDecimalValue(Pressure);

            dealer.getServices().add(temperature);
            dealer.getServices().add(humidity);
            dealer.getServices().add(pressure);
        } else if (s.startsWith("BLE")) {
            String parts[] = s.split(" ");
            String SensorID = parts[1];
            String tagID = parts[2].split(",")[0];
            String powerParts[] = s.split(",");
            String power = powerParts[powerParts.length - 1];

            dealer = new IMASensor(SensorID);

            Service tagReading = new Service("Bluetooth Tag Finder", "Provides data for bluetooth hardware", "/bluetooth", "Watt?");
            tagReading.setDecimalValue(tagID + " " + power);
            tagReading.getHw().put(tagID, new AssociatedHardware(tagID, power));

            dealer.getServices().add(tagReading);
        } else if (s.startsWith("CO")) {
            String parts[] = s.split(" ");
            String id = parts[1];
            String COLevel = parts[2];

            dealer = new IMASensor(id);

            Service service = new Service("CO levels", "Provides data for CO levels", "/COLevels", "COppm?");
            service.setDecimalValue(COLevel);

            dealer.getServices().add(service);
        }

        c.reportReadingOfSensor(dealer);
    }

}
