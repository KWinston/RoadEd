package roaded;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import javax.swing.*;
import java.util.Scanner;
import java.util.Set;
import org.jdesktop.swingx.mapviewer.Waypoint;

// Load map data plot points
public class FileLoader extends SwingWorker<Integer, Void> {

    private final Scanner scanner;
    private final PinMapApp pinExt;
    private MainGUI view = null;
    private MapView mapView;
    // Store the waypoint list
    Set<Waypoint> pinList = new HashSet<>(); 

    public FileLoader(PinMapApp pinMap, Scanner fileScanner) {
        scanner = fileScanner;
        pinExt = pinMap;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        int total = 0;
        // Adds waypoint data, equivalent to GeoPosition data points
            while (scanner.hasNextDouble()) {
                pinList.add(new Waypoint(scanner.nextDouble(), scanner.nextDouble()));
                pinList.add(new Waypoint(scanner.nextDouble(), scanner.nextDouble()));
            }
        return total;
    }

    @Override
    protected void done() {
        
        // Display the map view with pins after data retrieval
        mapView = new MapView(PinMapApp.getApplication(), pinList);
        mapView.getFrame().setSize(500, 500);
        mapView.getFrame().setLocation(690, 0);
        mapView.getFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mapView.getFrame().addWindowListener(new WindowAdapter() {
            // Override on window close so that the application does not terminate
            public void windowClosing(WindowEvent ev) {
                mapView.getFrame().setVisible(false);
            }
        });
        mapView.getFrame().setVisible(true);
    }
    
    public MapView getMapView() {
        return mapView;
    }
}
