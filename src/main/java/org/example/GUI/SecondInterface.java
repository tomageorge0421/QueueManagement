package org.example.GUI;

import org.example.Business_Logic.Simulation;
import org.example.Model.Server;
import org.example.Model.Task;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SecondInterface extends JFrame {
    protected JTextField textFieldSimTime;
    private JList QueueList;
    private JList ClientList;
    private JPanel panel1;

    private DefaultListModel QueueListModel = new DefaultListModel();
    private DefaultListModel ClientiListModel = new DefaultListModel();
    private Timer timer;
    private int simTime;

    private static PriorityQueue<Task> taskQueue = new PriorityQueue<>();

    private static List<Server> serverLanes = new ArrayList<>();

    public SecondInterface(int nrClienti, int nrCozi, int bifa, int simTime, int timeMin, int timeMax, int serviceMin, int serviceMax) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("MyInterface");
        setSize(600, 600);
        setContentPane(panel1);
        setVisible(true);
        pack();
        this.simTime = simTime;

        textFieldSimTime.setText("0");

        // Set up timer to update textFieldSimTime every second
        timer = new Timer(1000, new ActionListener() {
            int counter = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (counter < simTime) {
                    counter++;
                    textFieldSimTime.setText(String.valueOf(counter));
                    updateClienti(taskQueue);
                    updateQueues(serverLanes);
                } else {
                    timer.stop(); // Stop the timer when simTime is reached
                }
            }
        });
        timer.start();

        // Start simulation in a separate thread
        startSimulation(nrClienti, nrCozi, bifa, simTime, timeMin, timeMax, serviceMin, serviceMax);
    }

    private void startSimulation(int nrClienti, int nrCozi, int bifa, int simTime, int timeMin, int timeMax, int serviceMin, int serviceMax) {
        // Create a new thread to run the simulation
        Thread simulationThread = new Thread(() -> {
            Simulation simulation = new Simulation(this, simTime, bifa, nrClienti, nrCozi, timeMin, timeMax, serviceMin, serviceMax);
            try {
                simulation.Simulate();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        simulationThread.start(); // Start the simulation thread
    }

    public void updateClienti(PriorityQueue<Task> taskQueue) {
        SwingUtilities.invokeLater(() -> {
            ClientiListModel.removeAllElements();
            this.taskQueue = taskQueue;
            ClientList.setModel(ClientiListModel);
            for (Task client : taskQueue)
                ClientiListModel.addElement(client);
        });
    }

    public void updateQueues(List<Server> serverLanes) {
        SwingUtilities.invokeLater(() -> {
            QueueListModel.removeAllElements();
            this.serverLanes = serverLanes;
            QueueList.setModel(QueueListModel);
            for (Server server : serverLanes) {
                QueueListModel.addElement(server);
            }
        });
    }
}
