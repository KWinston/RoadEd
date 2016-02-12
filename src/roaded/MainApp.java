package roaded;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            MainController controller = new MainController(gui);
            controller.showGUI();
        });
    }
}
