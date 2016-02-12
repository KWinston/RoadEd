/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roaded;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author WinstonK
 */
public class DataLoader extends SwingWorker<Integer, Void> {
    TicketObject ticketObj = new TicketObject();
    private final MainGUI gui;
    
    public DataLoader(MainGUI view) {
        gui = view;
    }

    // Connect to online 311 Explorer json data object
    public void loadTicketData() {
        try {
            this.ticketObj = passData();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Parses the JSON object
    public static TicketObject passData() throws IOException {
        TicketObject ticketobj = new TicketObject();
        ticketobj.parse(ticketobj.storeJSON());
        return ticketobj;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        int total = 0;
        gui.setStatusText("Loading data...");
        loadTicketData();
        // Process each entry in json file retrieved
        for (TicketData object : ticketObj.getData()) {            
            Thread.sleep(1);
            total++;
            if (total % 100 == 0) {
                int value = gui.getStatus().getValue();
                gui.getStatus().setValue(value+10);
            }
        }
        return total;
    }

    @Override
    protected void done() {
         try {
         gui.toggleBusyStatus(); // Reminds user data must load before data shows up
         gui.setTotalTicketsProcessed(get().toString()); // Set Total tickets processed
         gui.setTicketObj(ticketObj); // store reference to saved data
         gui.setStatusText("Done loading data!");
         gui.getStatus().setForeground(Color.green);
         } catch (InterruptedException | ExecutionException ex) {
         gui.setStatusText("Error!");
         Logger.getLogger(FileLoader.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
}
