/*
 * PinMapApp.java
 * Adapted from - https://community.oracle.com/docs/DOC-983180
 * Building Maps into Your Swing Application with the JXMapViewer Blog
 */

package roaded;

import java.util.HashSet;
import java.util.Set;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.mapviewer.Waypoint;

/**
 * The main class of the application.
 */

public class PinMapApp extends SingleFrameApplication {
    Set<Waypoint> pinList = new HashSet<>(); 
    MapView newMap;
    
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        
        show(new MapView(PinMapApp.getApplication(), pinList));
    }
    
    public void receive(Set list) {
        this.pinList = list;
    }
    
    public PinMapApp getAppy() {
        return Application.getInstance(PinMapApp.class);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of MapApp
     */
    public static PinMapApp getApplication() {
        return Application.getInstance(PinMapApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(PinMapApp.class, args);
    }
}
