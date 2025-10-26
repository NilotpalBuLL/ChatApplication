
package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private final int port;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final File logFile = new File("server_connections.log");

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server started on port " + port);
            // AI responder (local)
            AIResponder ai = new AIResponder();

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this, ai);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastUserList() {
        String users = String.join(",", clients.keySet());
        clients.values().forEach(h -> h.sendRaw("USERLIST::" + users));
    }

    public void registerClient(String nick, ClientHandler handler) {
        clients.put(nick, handler);
        log("CONNECTED: " + nick + " from " + handler.getRemoteAddress());
        broadcastUserList();
    }

    public void unregisterClient(String nick) {
        clients.remove(nick);
        log("DISCONNECTED: " + nick);
        broadcastUserList();
    }

    public void routeMessage(String from, String to, String encryptedPayload) {
        if ("*".equals(to)) {
            // broadcast
            clients.values().forEach(h -> {
                h.sendRaw("MSG::" + from + "::" + encryptedPayload);
            });
        } else if ("AI".equalsIgnoreCase(to)) {
            // AI assistant request
            String decrypted = clients.get(from).decrypt(encryptedPayload);
            String aiReply = clients.get(from).getAiResponder().reply(decrypted);
            String enc = clients.get(from).encrypt(aiReply);
            clients.get(from).sendRaw("MSG::AI::" + enc);
        } else {
            ClientHandler target = clients.get(to);
            if (target != null) {
                target.sendRaw("MSG::" + from + "::" + encryptedPayload);
            } else {
                // notify sender that user not found
                ClientHandler sender = clients.get(from);
                if (sender != null) {
                    String err = "[Server] User '" + to + "' not found.";
                    sender.sendRaw("MSG::Server::" + sender.encrypt(err));
                }
            }
        }
    }

    private synchronized void log(String line) {
        String ts = new Date().toString();
        String entry = ts + " - " + line;
        System.out.println(entry);
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(entry);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 5000;
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
