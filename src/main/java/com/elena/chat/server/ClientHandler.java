package com.elena.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.elena.chat.server.ChatServer.log;

public class ClientHandler {
    private Socket socket;
    DataInputStream in;
    DataOutputStream out;
    ChatServer server;
    String nick;
    private String login;
    List<String> blackList;

    public ClientHandler(ChatServer server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String msg = in.readUTF();
                            if (msg.startsWith("/auth")) {
                                String[] tokens = msg.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                login = tokens[1];
                                if (newNick != null) {
                                    if (!server.isNickBusy(newNick)) {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        log.log(Level.INFO, "Клиент " + ClientHandler.this.getLogin() + " подключен");
                                        AuthService.insertLogs(ClientHandler.this.getLogin(), "connect");
                                        break;
                                    } else {
                                        sendMsg("Эта учетная запись уже используется");
                                        log.log(Level.SEVERE, "Учетная запись для " + ClientHandler.this.getLogin() + " уже используется");
                                    }
                                } else {
                                    sendMsg("Неверный логин/пароль");
                                    log.log(Level.SEVERE, "Для " + ClientHandler.this.getLogin() + " введен неправильный логин/пароль");
                                    AuthService.insertLogs(ClientHandler.this.getLogin(), "wrong login/password");
                                }
                            }
                        }

                        while (true) {
                            String msg = in.readUTF();
                            log.log(Level.INFO, "Пользователь " + ClientHandler.this.getLogin() + " отправил сообщение " + msg);
                            if (msg.startsWith("/")) {
                                if (msg.equals("/end")) {
                                    out.writeUTF("/serverClosed");
                                    break;
                                }
                                if (msg.startsWith("/w")) {
                                    String[] tokens = msg.split(" ", 3);
                                    server.sendPersonalMsg(ClientHandler.this, tokens[1], tokens[2]);
                                    String loginClient = AuthService.getLoginByNick(tokens[1]);
                                    log.log(Level.INFO, "Пользователь " + ClientHandler.this.getLogin() + " отправил личное сообщение " + loginClient);
                                }
                                if (msg.startsWith("/bl")) {
                                    String[] tokens = msg.split(" ");
                                    blackList.add(tokens[1]);
                                    sendMsg("Пользователь " + tokens[1] + " добавлен в черный список");
                                    String loginClient = AuthService.getLoginByNick(tokens[1]);
                                    log.log(Level.INFO, "Пользователь " + ClientHandler.this.getLogin() + " прислал команду на добавление в черный список " + loginClient);
                                }
                            } else
                                server.broadcastMsg(ClientHandler.this, nick + ": " + msg);
                        }
                        server.unsubscribe(ClientHandler.this);
                        log.log(Level.INFO, "Клиент " + ClientHandler.this.getLogin() + " отключился");
                        AuthService.insertLogs(ClientHandler.this.getLogin(), "disconnect");

                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }


            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

    public boolean checkBlackList(String nick) {
        return blackList.contains(nick);
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}