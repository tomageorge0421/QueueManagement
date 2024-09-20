package org.example.GUI;

import org.example.Business_Logic.Simulation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FirstInterface extends JFrame{
    private JPanel panel1;
    private JTextField textFieldNrClients;
    private JTextField textFieldNrQueues;
    private JTextField textFieldSimTime;
    private JButton RUNButton;
    private JCheckBox TIMECheckBox;
    private JCheckBox SHORTESTQUEUECheckBox;

    private JTextField textFieldArrivalTimeMin;

    private JTextField textFieldArrivalTimeMax;

    private JTextField textFieldServiceMin;

    private JTextField textFieldServiceMax;

    private int bifa;

    public FirstInterface() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("MyInterface");
        setSize(600, 600);
        setContentPane(panel1);
        setVisible(true);
        pack();
        RUNButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                int nrClienti = Integer.parseInt(textFieldNrClients.getText());
                int nrCozi = Integer.parseInt(textFieldNrQueues.getText());
                int simTime = Integer.parseInt(textFieldSimTime.getText());
                int timeMin = Integer.parseInt(textFieldArrivalTimeMin.getText());
                int timeMax = Integer.parseInt(textFieldArrivalTimeMax.getText());
                int serviceMin = Integer.parseInt(textFieldServiceMin.getText());
                int serviceMax = Integer.parseInt(textFieldServiceMax.getText());
                SecondInterface i2 = new SecondInterface(nrClienti,nrCozi,bifa,simTime,timeMin,timeMax,serviceMin,serviceMax);
            }
        });
        TIMECheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(TIMECheckBox.isSelected()) bifa =0;
            }
        });
        SHORTESTQUEUECheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(SHORTESTQUEUECheckBox.isSelected())bifa=1;
            }
        });
    }
}
