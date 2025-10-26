
package com.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Base64;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private final AIResponder aiResponder;
    private BufferedReader in;
    private PrintWriter out;
    private String nick = "unknown";
    private final String secretKey = "demo-key"; // demo symmetric key used by clients (XOR+Base64)

    public ClientHandler(Socket socket, ChatServer server, AIResponder aiResponder) {
        this.socket = socket;
        this.server = server;
        this.aiResponder = aiResponder;
    }

    public String getRemoteAddress() {
        return socket.getRemoteSocketAddress().toString();
    }

    public AIResponder getAiResponder() { return aiResponder; }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Expect NICK::<nickname>
            String line = in.readLine();
            if (line != null && line.startsWith("NICK::")) {
                nick = line.substring(6).trim();
                server.registerClient(nick, this);
            } else {
                sendRaw("MSG::Server::" + encrypt("No nickname provided. Connection closing."));
                close();
                return;
            }

            String input;
            while ((input = in.readLine()) != null) {
                // Commands: MSG::<recipient>::<base64>
                if (input.startsWith("MSG::")) {
                    String[] parts = input.split("::", 3);
                    if (parts.length == 3) {
                        String to = parts[1];
                        String payload = parts[2];
                        server.routeMessage(nick, to, payload);
                    }
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
        } finally {
            close();
        }
    }

    public void sendRaw(String raw) {
        out.println(raw);
    }

    public String encrypt(String plain) {
        try {
            byte[] p = plain.getBytes("UTF-8");
            byte[] k = secretKey.getBytes("UTF-8");
            byte[] r = new byte[p.length];
            for (int i=0;i<p.length;i++) r[i] = (byte)(p[i] ^ k[i % k.length]);
            return Base64.getEncoder().encodeToString(r);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public String decrypt(String base64) {
        try {
            byte[] c = Base64.getDecoder().decode(base64);
            byte[] k = secretKey.getBytes("UTF-8");
            byte[] r = new byte[c.length];
            for (int i=0;i<c.length;i++) r[i] = (byte)(c[i] ^ k[i % k.length]);
            return new String(r, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private void close() {
        try {
            server.unregisterClient(nick);
            socket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
