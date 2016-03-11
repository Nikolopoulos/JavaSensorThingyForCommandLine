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
        this.id = Integer.parseInt(id, 16);
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
        for (Service s : services) {
            toReturn += s.getName() + " service\n\t";
        }
        return toReturn;
    }

    public String JSONDescription() {
        //return "{"+id+"\t Micaz provides: " + (tempService?"temperature service ":"")+ (photoService?"photo service ":"") + (switchService?"switch service ":"") ;
        String reply = "{\"ID\":\"" + getId() + "\", ";
        for (Service s : services) {
            reply += "\"" + s.getName() + "\":\"true\"";
        }
        reply += "\"} ";
        return reply;
    }

    public String JSONObject() {
        //String reply = "\"ID\":\"" + getId() + "\", \"TemperatureService\":\"" + this.isTempService() + "\", \"LightService\":\"" + this.isPhotoService() + "\", \"SwitchService\":\"" + this.isSwitchService() + "\", \"Temperature\":\"" + util.Util.a2d2celsius((int) this.getTempReading()) + "\", \"Light\":\"" + this.getPhotoReading() + "\", \"Switch\":\"" + this.getSwitchState() + "\" ";

        String reply = "{\"ID\":\"" + getId() + "\", ";
        for (Service s : services) {
            reply += "\"" + s.getName() + "\":\"true\"";
        }
        for (Service s : services) {
            reply += "\"" + s.getName() + "\":\"" + s.getDecimalValue() + "\"";
        }
        reply += "} ";

        return reply;
    }

    public String RequestServiceReading(String ServiceURI) {
        String reply = "genericError";
        for (Service s : services) {
            if (s.getURI().contentEquals(ServiceURI) && !ServiceURI.contentEquals("/bluetooth")) {
                reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
            } else if (s.getURI().contentEquals(ServiceURI) && ServiceURI.contentEquals("/bluetooth")) {
                reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":[";
                    for(AssociatedHardware tag : s.getHw().values()){
                        reply += "{\""+tag.id+"\",\""+tag.reading+"\"},";
                    }
                    if(reply.endsWith(","))
                    {
                           reply =  reply.substring(0, reply.length()-1);
                    }
                reply += "] ";
            }
        }

        return reply;
    }

}
