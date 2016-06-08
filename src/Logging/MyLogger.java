/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class MyLogger {

    public static File logfile;
    private static PrintWriter pw;

    public static void init() {
        logfile = new File("log.nope");
        if (logfile.exists()) {
            try {
                pw = new PrintWriter(logfile);
                pw.write("");
                pw.close();
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
    

    public static void log(String s) {
        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logfile,true));
            bw.write("["+new Date().toString()+"]:  " + s);
            bw.newLine();
            bw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }
 
    public static String readLog() {
        String toReturn = "<html><body>";
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(logfile)));
            String temp = "";
            toReturn += "<table>";
            while ((temp = bf.readLine()) != null) {
                toReturn += "<tr>";
                toReturn += "<td>" + temp;
                toReturn += "<tr>";
            }
            toReturn += "</table>";
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        toReturn += "</body></html>";
        return toReturn;
    }
}
