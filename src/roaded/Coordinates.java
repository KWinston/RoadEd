/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roaded;

/**
 *
 * @author WinstonK
 */
public class Coordinates {
    private Double loc_lat;
    private Double loc_long;
    
    public void setCoordinates (Double loc_lat, Double loc_long) {
        this.loc_lat = new Double(loc_lat);
        this.loc_long = new Double(loc_long);
    }
    
    @Override
    public String toString() {
        return loc_lat + " " + loc_long;
    }
}
