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
                while (true) {
                    try {
                        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyUSB0"); //on unix based system
                        SerialPort serialPort = (SerialPort) portIdentifier.open("NameOfConnection-whatever", 0);

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
                                    System.out.println(s);
                                            
                                }
                                s = br.readLine();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        
        return t;

    }
    
    public void parseString(String s){
    
    }

}
