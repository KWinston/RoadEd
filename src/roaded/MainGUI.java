/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roaded;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 *
 * @author WinstonK
 */
public class MainGUI extends javax.swing.JFrame {

    DefaultPieDataset pieDataset = new DefaultPieDataset();
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    String wardNumber = "Incidents";
    boolean[] wardsSelected = new boolean[12];
    TicketObject ticketObj;
    boolean busyStatus = true;
    boolean firstDataLoadVisit = true;
    boolean firstMapDataVisit = true;
    ArrayList<Coordinates> coords = new ArrayList<>();
    Set<Waypoint> allWardsPinList = new HashSet<>();

    /**
     * Creates new form MainGUI
     */
    public MainGUI() {
        initComponents();
        Arrays.fill(wardsSelected, Boolean.FALSE);
        if (firstMapDataVisit) {
            infoBox("Welcome to RoadEd!"
                    + "\nYour helper to visualize Edmonton's311 Report Data."
                    + "\nPlease choose JSON DATA or CSV DATA."
                    + "\nJSON DATA will load the last 1000 entries submitted."
                    + "\nCSV DATA Will load all entries since data inception in the OpenData Portal.", "Message");
            firstDataLoadVisit = false;
        }
        this.jProgressBar2.setForeground(Color.red); // set red initially
    }

    public void setTotalTicketsProcessed(String num) {
        totalTextField.setText(num);
    }

    public void setStatusText(String progress) {
        loadIndicatorTextField.setText(progress);
    }

    public void setTicketObj(TicketObject obj) {
        ticketObj = obj;
    }

    ChartMouseListener CML = new ChartMouseListener() {
        public void chartMouseClicked(ChartMouseEvent event) {

            //should be able to open multiple windows for each ward when selected
            //to be integrated later
            JFrame infos = new JFrame("Info On Ward");
            infos.setResizable(false);
            ChartEntity entity = event.getEntity();
            JScrollPane jScrollPane1 = new JScrollPane();
            DefaultPieDataset pieWardDataset = new DefaultPieDataset();
            String ward = "";
            String mapDir = "";
            String title = "";
            for (int i = 0; i < 12; i++) {
                if (i < 10) {
                    if (entity.toString().endsWith("(Ward " + i + ")")) {
                        ward = "WARD 0" + i;
                        mapDir = "src/images/" + i + ".png";
                    }
                } else {
                    if (entity.toString().endsWith("(Ward " + i + ")")) {
                        ward = "WARD " + i;
                        mapDir = "src/images/" + i + ".png";
                    }
                }
                title = "Ward " + i;
            }
            if (ward.isEmpty() || mapDir.isEmpty() || title.isEmpty()) {
                System.out.println("Mouse clicked: null entity.");
                return;
            }

            pieWardDataset.setValue("Drainage Maintenance", incidentFreq("Drainage Maintenance", ward));
            pieWardDataset.setValue("Dead Animal Removal", incidentFreq("Dead Animal Removal", ward));
            pieWardDataset.setValue("Litter & Waste", incidentFreq("Litter & Waste", ward));
            pieWardDataset.setValue("Parks & Sportsfield Maintenance", incidentFreq("Parks & Sportsfield Maintenance", ward));
            pieWardDataset.setValue("Pest Management", incidentFreq("Pest Management", ward));
            pieWardDataset.setValue("Pothole", incidentFreq("Pothole", ward));
            pieWardDataset.setValue("Road/Sidewalk Maintenance", incidentFreq("Road/Sidewalk Maintenance", ward));
            pieWardDataset.setValue("Snow & Ice Maintenance", incidentFreq("Snow & Ice Maintenance", ward));
            pieWardDataset.setValue("Structure Maintenance", incidentFreq("Structure Maintenance", ward));
            pieWardDataset.setValue("Traffic Lights & Signs", incidentFreq("Traffic Lights & Signs", ward));
            pieWardDataset.setValue("Tree Maintenance", incidentFreq("Tree Maintenance", ward));
            pieWardDataset.setValue("Vandalism/Graffiti", incidentFreq("Vandalism/Graffiti", ward));

            // Creates pie chart
            JFreeChart chart = ChartFactory.createPieChart(title, pieWardDataset);
            chart.removeLegend();
            ChartPanel frame = new ChartPanel(chart);

            // Processes specific ward for map pins plotting
            Coordinates coordinateData = new Coordinates();
            ArrayList<Coordinates> wardCoords = new ArrayList<>();
            for (TicketData entry : ticketObj.getData()) {
                if (entry.getWard() != null && entry.getWard().equals(ward)) {
                    coordinateData = new Coordinates();
                    coordinateData.setCoordinates(entry.getLoc_lat(), entry.getLoc_long());
                    wardCoords.add(coordinateData);
                }
            }
            // Export to file code
            try {
                PrintStream out = new PrintStream(new FileOutputStream("plotWardData.txt"));
                for (Coordinates coord : wardCoords) {
                    out.println(coord);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            Scanner scanner = null;
            try {
                scanner = new Scanner(new File("plotWardData.txt"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            PinMapApp pinMap = new PinMapApp();
            FileLoader worker = new FileLoader(pinMap, scanner);
            worker.execute();

            infos.setSize(800, 400);
            JLabel jLabel1 = new javax.swing.JLabel();
            jLabel1.setSize(500, 500);

            //add image to scrollpane and then the set the ViewPort
            BufferedImage img2 = null;
            try {
                img2 = ImageIO.read(new File(mapDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Image resizedImage = img2.getScaledInstance(jLabel1.getWidth(), jLabel1.getHeight(), Image.SCALE_SMOOTH);

            jLabel1.setIcon(new ImageIcon(resizedImage)); // NOI18N

            //changed to the high resolution image location later
            jScrollPane1.setViewportView(jLabel1);
            jScrollPane1.getHorizontalScrollBar().setValue((jScrollPane1.getHorizontalScrollBar().getMaximum() - jScrollPane1.getViewport().getViewRect().width) / 2);
            jScrollPane1.getVerticalScrollBar().setValue((jScrollPane1.getVerticalScrollBar().getMaximum() - jScrollPane1.getViewport().getViewRect().height) / 2);

            //layout for the scrollpane
            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(infos.getContentPane());
            infos.getContentPane().setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(frame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                            .addContainerGap())
            );

            layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                                    .addComponent(frame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
            );

            infos.pack();
            infos.setVisible(true);
        }

        public void chartMouseMoved(ChartMouseEvent event) {

            int x = event.getTrigger().getX();
            int y = event.getTrigger().getY();
            ChartEntity entity = event.getEntity();
            if (entity != null) {
                //System.out.println("Mouse clicked: " + entity.toString());
            } else {
                //System.out.println(
                //"Mouse moved: " + x + ", " + y + ": null entity.");
            }

        }
    };

    // Method made to keep track of which wards are selected
    public void getWardsSelected(int ward) {
        if (wardsSelected[ward - 1]) {
            wardsSelected[ward - 1] = false;
        } else {
            wardsSelected[ward - 1] = true;
        }
        loadGraphInfo();

    }

    // Used to track when data is loaded for visualization of data
    public void toggleBusyStatus() {
        busyStatus = !busyStatus;
    }

    // Retrieve Progress Bar element
    public JProgressBar getStatus() {
        return jProgressBar2;
    }

    // MessageBox message to display information when needed
    public void infoBox(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, "RoadEd: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    // Controls the loading of graph data
    public void loadGraphInfo() {
        if (busyStatus) {
            infoBox("Please load 311 data first on the data tab.", "Message");
            return;
        }
        pieDataset.clear();

        // Store coordinates of each related ticket incident
        Coordinates coordinateData = new Coordinates();
        coords.clear();
        for (int i = 0; i < 12; i++) {
            int counter = 0;
            if (wardsSelected[i] == true) {
                for (TicketData entry : ticketObj.getData()) {
                    if (i < 10) {
                        if (entry.getWard() != null && entry.getWard().compareTo("WARD 0" + (i + 1)) == 0) {
                            counter++;
                            coordinateData = new Coordinates();
                            coordinateData.setCoordinates(entry.getLoc_lat(), entry.getLoc_long());
                            coords.add(coordinateData);
                        }
                    } else {
                        if (entry.getWard() != null && entry.getWard().compareTo("WARD 1" + (i - 9)) == 0) {
                            counter++;
                            coordinateData = new Coordinates();
                            coordinateData.setCoordinates(entry.getLoc_lat(), entry.getLoc_long());
                            coords.add(coordinateData);
                        }
                    }
                }
                pieDataset.setValue("Ward " + (i + 1), counter);
                counter = 0;
            }

        }

        // Export to file so that pin mapper can retrieve it
        try {
            PrintStream out = new PrintStream(new FileOutputStream("plotData.txt"));
            for (Coordinates coord : coords) {
                out.println(coord);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Loads the pie chart for all wards selected
        JFreeChart chart = ChartFactory.createPieChart(wardNumber, pieDataset);
        chart.removeLegend();
        PieChart.setLayout(new java.awt.BorderLayout());
        ChartPanel CP = new ChartPanel(chart);
        CP.addChartMouseListener(CML);
        CP.setPreferredSize(new Dimension(785, 440)); //size according to my window
        CP.setMouseWheelEnabled(true);
        PieChart.add(CP);

        //bargraph
        dataset.clear(); //resets bargraph when new wards are elected
        CategoryDataset dataset1 = createDataset1(); //uses dataset to create the bargraph
        JFreeChart chart1 = ChartFactory.createBarChart(
                // bargraph labels and orientation
                "Ward Incidents",
                "Wards",
                "Incidents",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        //bargraph settings
        DualAxis.setLayout(new java.awt.BorderLayout());
        chart1.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart1.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        plot.setRenderer(1, renderer2);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

        //bar graph chart panel creation 
        final ChartPanel chartPanel = new ChartPanel(chart1);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setMouseZoomable(true);
        chartPanel.setMouseWheelEnabled(true);

        DualAxis.add(chartPanel);
    }

    private CategoryDataset createDataset1() {

        // row keys set to the different issue types
        final String issue1 = "Dead Animal Removal";
        final String issue2 = "Drainage Maintenance";
        final String issue3 = "Litter & Waste";
        final String issue4 = "Parks & Sportsfield Maintenance";
        final String issue5 = "Pest Management";
        final String issue6 = "Pothole";
        final String issue7 = "Road/Sidewalk Maintenance";
        final String issue8 = "Snow & Ice Maintenance";
        final String issue9 = "Structure Maintenance";
        final String issue10 = "Traffic Lights & Signs";
        final String issue11 = "Tree Maintenance";
        final String issue12 = "Vandalism/Graffiti";

        //loop to set the ward name and bargraph
        for (int i = 0; i < 12; i++) {
            String ward = "";
            String ward2 = "";
            if (wardsSelected[i] == true) {
                if (i < 9) {
                    ward = "WARD 0" + Integer.toString(i + 1);
                    ward2 = Integer.toString(i + 1);
                } else {
                    ward = "WARD " + Integer.toString(i + 1);
                    ward2 = Integer.toString(i + 1);
                }
            }
            //uses the incidentFreq function to create the bar graph
            dataset.addValue(incidentFreq(issue1, ward), issue1, ward2);
            dataset.addValue(incidentFreq(issue2, ward), issue2, ward2);
            dataset.addValue(incidentFreq(issue3, ward), issue3, ward2);
            dataset.addValue(incidentFreq(issue4, ward), issue4, ward2);
            dataset.addValue(incidentFreq(issue5, ward), issue5, ward2);
            dataset.addValue(incidentFreq(issue6, ward), issue6, ward2);
            dataset.addValue(incidentFreq(issue7, ward), issue7, ward2);
            dataset.addValue(incidentFreq(issue8, ward), issue8, ward2);
            dataset.addValue(incidentFreq(issue9, ward), issue9, ward2);
            dataset.addValue(incidentFreq(issue10, ward), issue10, ward2);
            dataset.addValue(incidentFreq(issue11, ward), issue11, ward2);
            dataset.addValue(incidentFreq(issue12, ward), issue12, ward2);
        }

        return dataset;

    }

    //takes the issue type and ward strings to calculate incident freq
    private int incidentFreq(String issue_type, String ward) {
        int amount = 0;
        for (TicketData entry : ticketObj.getData()) {
            if (entry.getWard() != null && entry.getWard().equals(ward) && entry.getIssueType().equals(issue_type)) {
                amount++;
            }
        }
        return amount;

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        MapSelect = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        jToggleButton8 = new javax.swing.JToggleButton();
        jToggleButton9 = new javax.swing.JToggleButton();
        jToggleButton10 = new javax.swing.JToggleButton();
        jToggleButton11 = new javax.swing.JToggleButton();
        jToggleButton12 = new javax.swing.JToggleButton();
        selectAllWards = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        loadButton = new javax.swing.JButton();
        PieChart = new javax.swing.JPanel();
        DualAxis = new javax.swing.JPanel();
        jProgressBar2 = new javax.swing.JProgressBar();
        loadIndicatorTextField = new javax.swing.JTextField();
        totalTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("RoadEd");

        jPanel1.setPreferredSize(new java.awt.Dimension(850, 600));

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        MapSelect.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        MapSelect.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                MapSelectFocusGained(evt);
            }
        });
        MapSelect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MapSelectMouseClicked(evt);
            }
        });
        MapSelect.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                MapSelectPropertyChange(evt);
            }
        });

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w1.png"))); // NOI18N
        jToggleButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h1.png"))); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w2.png"))); // NOI18N
        jToggleButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton2.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h2.png"))); // NOI18N
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w3.png"))); // NOI18N
        jToggleButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton3.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h3.png"))); // NOI18N
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jToggleButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w4.png"))); // NOI18N
        jToggleButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton4.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h4.png"))); // NOI18N
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        jToggleButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w5.png"))); // NOI18N
        jToggleButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton5.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h5.png"))); // NOI18N
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        jToggleButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w6.png"))); // NOI18N
        jToggleButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton6.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h6.png"))); // NOI18N
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        jToggleButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w7.png"))); // NOI18N
        jToggleButton7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton7.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h7.png"))); // NOI18N
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });

        jToggleButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w8.png"))); // NOI18N
        jToggleButton8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton8.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h8.png"))); // NOI18N
        jToggleButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton8ActionPerformed(evt);
            }
        });

        jToggleButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w9.png"))); // NOI18N
        jToggleButton9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton9.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h9.png"))); // NOI18N
        jToggleButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton9ActionPerformed(evt);
            }
        });

        jToggleButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w10.png"))); // NOI18N
        jToggleButton10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton10.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h10.png"))); // NOI18N
        jToggleButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton10ActionPerformed(evt);
            }
        });

        jToggleButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w11.png"))); // NOI18N
        jToggleButton11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton11.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h11.png"))); // NOI18N
        jToggleButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton11ActionPerformed(evt);
            }
        });

        jToggleButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/w12.png"))); // NOI18N
        jToggleButton12.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jToggleButton12.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/h12.png"))); // NOI18N
        jToggleButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton12ActionPerformed(evt);
            }
        });

        selectAllWards.setText("Select All");
        selectAllWards.setMaximumSize(new java.awt.Dimension(65, 30));
        selectAllWards.setMinimumSize(new java.awt.Dimension(65, 30));
        selectAllWards.setPreferredSize(new java.awt.Dimension(65, 30));
        selectAllWards.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllWardsActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.setMaximumSize(new java.awt.Dimension(65, 30));
        resetButton.setMinimumSize(new java.awt.Dimension(65, 30));
        resetButton.setPreferredSize(new java.awt.Dimension(65, 30));
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        loadButton.setText("Map Incidents");
        loadButton.setMaximumSize(new java.awt.Dimension(65, 30));
        loadButton.setMinimumSize(new java.awt.Dimension(65, 30));
        loadButton.setPreferredSize(new java.awt.Dimension(65, 30));
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MapSelectLayout = new javax.swing.GroupLayout(MapSelect);
        MapSelect.setLayout(MapSelectLayout);
        MapSelectLayout.setHorizontalGroup(
            MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MapSelectLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MapSelectLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectAllWards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(77, 77, 77)
                        .addComponent(jToggleButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(78, 78, 78)))
                .addGap(6, 6, 6)
                .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton12)
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50))
        );
        MapSelectLayout.setVerticalGroup(
            MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MapSelectLayout.createSequentialGroup()
                .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MapSelectLayout.createSequentialGroup()
                        .addComponent(jToggleButton2)
                        .addGap(35, 35, 35))
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(MapSelectLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jToggleButton4)
                                    .addComponent(jToggleButton5)))
                            .addGroup(MapSelectLayout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(jToggleButton1))
                            .addGroup(MapSelectLayout.createSequentialGroup()
                                .addGap(54, 54, 54)
                                .addComponent(jToggleButton3)))
                        .addGap(8, 8, 8)))
                .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton6)
                            .addComponent(jToggleButton7)
                            .addComponent(jToggleButton8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(MapSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(MapSelectLayout.createSequentialGroup()
                                .addComponent(loadButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(resetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(selectAllWards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jToggleButton11)))
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addComponent(jToggleButton10)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(MapSelectLayout.createSequentialGroup()
                        .addComponent(jToggleButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButton12)))
                .addContainerGap())
        );

        loadButton.getAccessibleContext().setAccessibleName("");
        loadButton.getAccessibleContext().setAccessibleDescription("");

        jTabbedPane1.addTab("Map Select", MapSelect);

        PieChart.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout PieChartLayout = new javax.swing.GroupLayout(PieChart);
        PieChart.setLayout(PieChartLayout);
        PieChartLayout.setHorizontalGroup(
            PieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 798, Short.MAX_VALUE)
        );
        PieChartLayout.setVerticalGroup(
            PieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 504, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Total Incident Distr.", PieChart);

        javax.swing.GroupLayout DualAxisLayout = new javax.swing.GroupLayout(DualAxis);
        DualAxis.setLayout(DualAxisLayout);
        DualAxisLayout.setHorizontalGroup(
            DualAxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 802, Short.MAX_VALUE)
        );
        DualAxisLayout.setVerticalGroup(
            DualAxisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 508, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Ward Incident Distr.", DualAxis);

        totalTextField.setName(""); // NOI18N

        jLabel1.setText("Tickets Processed:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 811, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(29, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadIndicatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(totalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(loadIndicatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        totalTextField.getAccessibleContext().setAccessibleName("");
        jLabel1.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton12ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(12);
    }//GEN-LAST:event_jToggleButton12ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        getWardsSelected(1);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(2);
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(3);
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(4);
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(5);
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(6);
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(7);
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jToggleButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton8ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(8);
    }//GEN-LAST:event_jToggleButton8ActionPerformed

    private void jToggleButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton9ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(9);
    }//GEN-LAST:event_jToggleButton9ActionPerformed

    private void jToggleButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton10ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(10);
    }//GEN-LAST:event_jToggleButton10ActionPerformed

    private void jToggleButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton11ActionPerformed
        // TODO add your handling code here:
        getWardsSelected(11);
    }//GEN-LAST:event_jToggleButton11ActionPerformed

    private void selectAllWardsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllWardsActionPerformed
        // TODO add your handling code here:
        Arrays.fill(wardsSelected, Boolean.FALSE);
        jToggleButton1.setSelected(true);
        jToggleButton1ActionPerformed(evt);
        jToggleButton2.setSelected(true);
        jToggleButton2ActionPerformed(evt);
        jToggleButton3.setSelected(true);
        jToggleButton3ActionPerformed(evt);
        jToggleButton4.setSelected(true);
        jToggleButton4ActionPerformed(evt);
        jToggleButton5.setSelected(true);
        jToggleButton5ActionPerformed(evt);
        jToggleButton6.setSelected(true);
        jToggleButton6ActionPerformed(evt);
        jToggleButton7.setSelected(true);
        jToggleButton7ActionPerformed(evt);
        jToggleButton8.setSelected(true);
        jToggleButton8ActionPerformed(evt);
        jToggleButton9.setSelected(true);
        jToggleButton9ActionPerformed(evt);
        jToggleButton10.setSelected(true);
        jToggleButton10ActionPerformed(evt);
        jToggleButton11.setSelected(true);
        jToggleButton11ActionPerformed(evt);
        jToggleButton12ActionPerformed(evt);
        jToggleButton12.setSelected(true);
    }//GEN-LAST:event_selectAllWardsActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        // TODO add your handling code here:
        // Resets bargraph and pie chart when reset
        pieDataset.clear();
        dataset.clear();
        Arrays.fill(wardsSelected, Boolean.FALSE);
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(false);
        jToggleButton3.setSelected(false);
        jToggleButton4.setSelected(false);
        jToggleButton5.setSelected(false);
        jToggleButton6.setSelected(false);
        jToggleButton7.setSelected(false);
        jToggleButton8.setSelected(false);
        jToggleButton9.setSelected(false);
        jToggleButton10.setSelected(false);
        jToggleButton11.setSelected(false);
        jToggleButton12.setSelected(false);
        JFreeChart chart = ChartFactory.createPieChart(wardNumber, pieDataset);
    }//GEN-LAST:event_resetButtonActionPerformed

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void MapSelectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MapSelectMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_MapSelectMouseClicked

    private void MapSelectPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_MapSelectPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_MapSelectPropertyChange

    private void MapSelectFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_MapSelectFocusGained
        // TODO add your handling code here:

    }//GEN-LAST:event_MapSelectFocusGained

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        // TODO add your handling code here:
        if (busyStatus) {
            infoBox("Please wait for the data to load", "Message");
        } else {
            Scanner scanner = null;
            try {
                scanner = new Scanner(new File("plotData.txt"));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            PinMapApp pinMap = new PinMapApp();
            FileLoader worker = new FileLoader(pinMap, scanner);
            worker.execute();
        }

    }//GEN-LAST:event_loadButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel DualAxis;
    private javax.swing.JPanel MapSelect;
    private javax.swing.JPanel PieChart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton10;
    private javax.swing.JToggleButton jToggleButton11;
    private javax.swing.JToggleButton jToggleButton12;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JToggleButton jToggleButton8;
    private javax.swing.JToggleButton jToggleButton9;
    private javax.swing.JButton loadButton;
    private javax.swing.JTextField loadIndicatorTextField;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton selectAllWards;
    private javax.swing.JTextField totalTextField;
    // End of variables declaration//GEN-END:variables
}
