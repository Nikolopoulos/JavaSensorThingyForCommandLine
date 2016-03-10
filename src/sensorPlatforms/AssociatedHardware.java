/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensorPlatforms;

/**
 *
 * @author pi
 */
public class AssociatedHardware {
    String id;
    String reading;

    public AssociatedHardware(String id) {
        this.id = id;
    }
    
    public AssociatedHardware(String id,String reading) {
        this.id = id;
        this.reading = reading;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }
    
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
}
