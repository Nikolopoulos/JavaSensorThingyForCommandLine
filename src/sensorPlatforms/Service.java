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
public class Service {
    private String name;
    private String description;
    private String URI; //URI is "/thething"
    
    private String units;
    private String decimalValue;

    public Service(String name, String description, String URI, String units) {
        this.name = name;
        this.description = description;
        this.URI = URI;
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(String decimalValue) {
        this.decimalValue = decimalValue;
    }
    
    public double getDoubleUnits(){
        return Double.parseDouble(decimalValue);
    }
    
}
