package com.elena.chat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientController {

    @FXML
    TextArea textArea;
    @FXML
    TextField textField;
    @FXML
    HBox upperPanel;
    @FXML
    HBox bottomPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    ListView<String> clientsList;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    boolean isAuth;

    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;

    public void setAuth(boolean isAuth) {
        this.isAuth = isAuth;
        if (!isAuth) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }
    }

    public void connectClient() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String msg = in.readUTF();
                            if (msg.equals("/authok")) {
                                setAuth(true);
                                break;
                            }
                            textArea.appendText(msg + "\n");
                        }

                        while (true) {
                            String msg = in.readUTF();
                            if (msg.startsWith("/")) {
                                if (msg.equals("/serverClosed")) {
                                    break;
                                }
                                if (msg.startsWith("/clist")) {
                                    String[] tokens = msg.split(" ");
                                    Platform.runLater(() -> {
                                        clientsList.getItems().clear();
                                        for (int i = 0; i < tokens.length; i++) {
                                            clientsList.getItems().add(tokens[i]);
                                        }
                                    });
                                }

                            } else {
                                textArea.appendText(msg + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuth(false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        System.out.println("Завершение работы");
        try {
            if (out != null) {
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connectClient();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
