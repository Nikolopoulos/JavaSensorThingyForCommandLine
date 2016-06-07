/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMaking;

import affinitySupport.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
import sensorPlatforms.IMASensor;
import util.Control;

/**
 *
 * @author billaros
 */
public class SuperSmahtThread implements Runnable {

    private int id;
    private String sirh;
    private ServiceEstimation tsoulou;
    private Core whatCore;
    private Control c;
    private boolean running;
    String ServiceArguements;

    public SuperSmahtThread(int id, ServiceEstimation tsoulou, Control myControl, String ServiceArguements) {
        this.id = id;
        this.sirh = "";
        this.tsoulou = tsoulou;
        this.ServiceArguements = ServiceArguements;
        c = myControl;
        running = false;
    }

    public int getId() {
        return id;
    }

    public ServiceEstimation getTsoulou() {
        return tsoulou;
    }

    public void setTsoulou(ServiceEstimation tsoulou) {
        this.tsoulou = tsoulou;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Core getWhatCore() {
        return whatCore;
    }

    public void setWhatCore(Core whatCore) {
        this.whatCore = whatCore;
    }

    public Control getC() {
        return c;
    }

    public void setC(Control c) {
        this.c = c;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReturnVal() {
        return sirh;
    }

    public void setReturnVal(String returnVal) {
        this.sirh = returnVal;
    }

    @Override
    public void run() {
        running = true;
        try {
            whatCore.attachTo();
        } catch (Exception ex) {
            Logger.getLogger(SuperSmahtThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        int ID = Integer.parseInt(ServiceArguements.split("/")[2]);
        String ServiceURI = "/" + ServiceArguements.split("/")[ServiceArguements.split("/").length - 1];
        for (IMASensor m : this.c.getMotesList()) {
            if (m.getId() == ID) {
                sirh += m.RequestServiceReading(ServiceURI);
                break;
            }
        }

    }

}
