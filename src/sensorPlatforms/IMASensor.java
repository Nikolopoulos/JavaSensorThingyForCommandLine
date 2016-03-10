/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensorPlatforms;

import java.util.ArrayList;

/**
 *
 * @author pi
 */
public class IMASensor {
    private int id;
    private long latestActivity;
    private ArrayList<Service> services;

    public IMASensor(int id) {
        this.id = id;
        this.latestActivity = System.currentTimeMillis();
        this.services = new ArrayList<Service>();
    }
    
    public IMASensor(String id) {
        this.id = Integer.parseInt(id);
        this.latestActivity = System.currentTimeMillis();
        this.services = new ArrayList<Service>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLatestActivity() {
        return latestActivity;
    }

    public void setLatestActivity(long latestActivity) {
        this.latestActivity = latestActivity;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public void setServices(ArrayList<Service> services) {
        this.services = services;
    }
    
     @Override
    public String toString() {
        String toReturn = id + " sensor provides: \n\t";
        for(Service s : services ){
            toReturn += s.getName() + " service\n\t";
        }
        return toReturn;
    }
    
    
}
