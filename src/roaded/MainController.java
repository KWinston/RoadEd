package roaded;

/**
 * Created by WinstonK on 11/10/2015.
 */
public class MainController {
    private final MainGUI gui; // View

    public MainController(MainGUI gui) {
        this.gui = gui;
        gui.setResizable(false); // Disable resizing of window
        DataLoader worker = new DataLoader(gui);
        worker.execute();
    }

    public void showGUI() {
        
             // This method is invoked on the EDT thread
        gui.setVisible(true);
    }
}