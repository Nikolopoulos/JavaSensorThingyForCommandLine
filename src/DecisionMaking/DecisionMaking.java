/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMaking;

import Logging.MyLogger;
import java.util.ArrayList;
import java.util.Iterator;
import util.Control;

import java.util.Random;

/**
 *
 * @author billaros
 */
public class DecisionMaking {

    private  ArrayList<SuperSmahtThread> threads = new  ArrayList<SuperSmahtThread>();
    private Control myControl;
    private int id = 0;
    private Thread magic;
    private boolean run;

    public int add(String ServiceArguements) {
        id = (id + 1) % 10000;
        ServiceEstimation service = new ServiceEstimation(ServiceArguements);
        SuperSmahtThread thread = new SuperSmahtThread(id, service, myControl,ServiceArguements);
        
        threads.add(thread);
        return id;
    }

    public DecisionMaking(Control c) {
        this.myControl = c;
        run = true;
        startDecisionDaemon();
    }

    private void startDecisionDaemon() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(run){
                    for(Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();){
                        SuperSmahtThread thread = iter.next();
                            if(!thread.isRunning()){
                                MyLogger.log(thread + "not running");
                                if(thread.getTsoulou().getClEstimation()>2){
                                    //i should probably find a way to estimate per core load
                                    MyLogger.log("Attaching service of cl="+thread.getTsoulou().getClEstimation() +" to critical core");
                                    thread.setWhatCore(thread.getC().criticalSensingCore);
                                }
                                else{
                                    MyLogger.log("Attaching service of cl="+thread.getTsoulou().getClEstimation() +" to non-critical core");
                                    thread.setWhatCore(thread.getC().sensingCore);
                                }
                                
                                if(new Random().nextInt(10)>new Random().nextInt(5)+5){
                                    thread.run();
                                    MyLogger.log(thread + "ran");
                                }
                            }
                    }
                }
            }
        }).start();
        

    }

    public String getResultOf(int id) {
        SuperSmahtThread t = null;
        for (Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();) {
            SuperSmahtThread thread = iter.next();
            if (thread.getId() == id) {
                t = thread;
                //MyLogger.log(thread + "got result");
                break;
            }
        }
        return t==null?"":t.getReturnVal();
    }

}
