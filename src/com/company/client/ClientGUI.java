package com.company.client;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

// STILL IN DEVELOPMENT.

public class ClientGUI {
    JFrame frame = new JFrame("Typing Relay Race");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(40, 50);

    public ClientGUI() {
        textField.setEditable(false);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(e -> {

            messageArea.append(" [" + new java.util.Date() + "] You: " + textField.getText() + "\n");
            if (textField.getText().startsWith(":clear")) {
                messageArea.setText("");
            }

            textField.setText("");
        });
    }

    private void run() {
        this.frame.setTitle("Typing Relay Race");
        textField.setEditable(true);
    }

    public static void main(String[] args)  {
        var client = new ClientGUI();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}