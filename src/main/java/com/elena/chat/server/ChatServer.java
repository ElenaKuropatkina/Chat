package com.elena.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Vector;
import java.util.logging.*;

public class ChatServer {

    private Vector<ClientHandler> clients;
    public static final Logger log = Logger.getLogger("Log");

    public ChatServer() {

        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            server = new ServerSocket(8189);

            logging();
            log.log(Level.SEVERE, "Сервер запущен");

            while (true) {
                socket = server.accept();

                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(nickTo)) {
                c.sendMsg("From " + from.getNick() + ": " + msg);
                from.sendMsg("To " + nickTo + ": " + msg);
                break;
            }
        }
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        for (ClientHandler c : clients) {
            if (!c.checkBlackList(from.getNick())) {
                c.sendMsg(msg);
            }
        }
    }

    public void broadcastClientsList() {
        StringBuilder s = new StringBuilder();
        s.append("/clist ");
        for (ClientHandler c : clients) {
            s.append(c.getNick() + " ");
        }
        String clist = s.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(clist);
        }
    }

    public void subscribe(ClientHandler client) throws SQLException {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) throws SQLException {
        clients.remove(client);
        broadcastClientsList();
    }

    public void logging() {
        log.setLevel(Level.ALL);
        Handler handler = null;
        try {
            handler = new FileHandler("mylog.log", true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        handler.setLevel(Level.ALL);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getLevel() + "\t" + record.getMessage() + "\t" + record.getSourceClassName() + "\t" + LocalDateTime.now() + "\n";
            }
        });
        log.addHandler(handler);
    }
}
