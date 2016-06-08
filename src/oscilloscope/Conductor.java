/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oscilloscope;

import Logging.MyLogger;
import affinitySupport.ThreadAffinity;
import util.Control;
import webServer.Server;

/**
 *
 * @author billaros
 */
public class Conductor {
    
    
    
    
    public static void main(String args[]) {
        
        MyLogger.init();

        Control c = new Control(false);
        
        Server myServer = new Server(c);
        myServer.startServer();
    }
}
