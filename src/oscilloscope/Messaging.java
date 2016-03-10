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
import lib.*;
import sensorPlatforms.MicazMote;
import util.Control;

/**
 *
 * @author billaros
 */
public class Messaging {

    public void doshit() {
        Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"); //on unix based system
            SerialPort serialPort = (SerialPort) portIdentifier.open("NameOfConnection-whatever", 0);

            serialPort.setSerialPortParams(
                    115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            boolean active = true;
            InputStream inputStream = serialPort.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String s = br.readLine();
            while (s
                    != null) {
                try {
                    if (s.charAt(0) != '#') {
                        System.out.println(System.currentTimeMillis() + ": " + s);
                    }
                    s = br.readLine();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
        }
    }

}
