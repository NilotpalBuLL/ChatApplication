
package com.chatapp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Optional;

/**
 * Modern ChatClient with multiple theme support added.
 * Themes are CSS files located in ../resources/styles/
 *
 * Theme names (ChoiceBox values):
 *  - Classic (WhatsApp-like)
 *  - DiscordDark (Discord-like)
 *  - Minimal (Clean)
 *  - Glass (Custom: glassmorphism blue gradient)
 *
 * The rest of networking is identical to the original client.
 */
public class ChatClient extends Application {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nick;
    private final String secretKey = "demo-key"; // must match server for demo XOR-encryption

    private TextArea chatArea;
    private ListView<String> userList;
    private TextField inputField;
    private Button sendBtn;
    private CheckBox privateToggle;
    private TextField hostField;
    private TextField portField;
    private ChoiceBox<String> themeChoice;

    @Override
    public void start(Stage stage) {
        nick = askNick();
        if (nick == null || nick.trim().isEmpty()) {
            Platform.exit();
            return;
        }

        stage.setTitle("JavaChatApp - " + nick);

        chatArea = new TextArea(); chatArea.setEditable(false); chatArea.setWrapText(true);
        chatArea.setFont(Font.font(14));
        userList = new ListView<>();
        inputField = new TextField();
        sendBtn = new Button("Send");
        privateToggle = new CheckBox("Private to selected user");
        hostField = new TextField("localhost");
        portField = new TextField("5000");
        portField.setMaxWidth(80);

        themeChoice = new ChoiceBox<>(FXCollections.observableArrayList("Classic","DiscordDark","Minimal","Glass"));
        themeChoice.setValue("DiscordDark"); // default
        themeChoice.setOnAction(e -> applyTheme(stage.getScene(), themeChoice.getValue()));

        HBox top = new HBox(8, new Label("Host:"), hostField, new Label("Port:"), portField,
                new Label("Theme:"), themeChoice);
        top.setPadding(new Insets(8));
        top.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(10, new Label("Users"), userList, privateToggle);
        left.setPrefWidth(200);
        left.setPadding(new Insets(8));

        VBox center = new VBox(8, chatArea, inputField);
        center.setPadding(new Insets(8));
        HBox bottom = new HBox(6, sendBtn);
        bottom.setPadding(new Insets(6));
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setLeft(left);
        root.setCenter(center);
        root.setBottom(bottom);
        root.setPadding(new Insets(6));

        // Add quick actions toolbar (mock icons using labels)
        ToolBar toolbar = new ToolBar(new Label("💬 Chat"), new Separator(), new Label("⚙ Settings"));
        root.setRight(toolbar);

        sendBtn.setOnAction(e -> sendMessage());
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendMessage();
        });

        Scene scene = new Scene(root, 900, 560);
        stage.setScene(scene);

        // apply initial theme css
        applyTheme(scene, themeChoice.getValue());

        stage.show();

        connect(hostField.getText(), Integer.parseInt(portField.getText()));
    }

    private String askNick() {
        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Choose a nickname");
        d.setContentText("Nickname:");
        Optional<String> res = d.showAndWait();
        return res.orElse(null);
    }

    private void applyTheme(Scene scene, String name) {
        scene.getStylesheets().clear();
        // Load CSS from resource path relative to classpath
        String css = "/styles/" + name + ".css";
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    private void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                // send nick
                out.println("NICK::" + nick);

                // listen
                String line;
                while ((line = in.readLine()) != null) {
                    final String l = line;
                    Platform.runLater(() -> handleServerLine(l));
                }
            } catch (IOException e) {
                Platform.runLater(() -> chatArea.appendText("[Error] " + e.getMessage() + "\\n"));
            }
        }).start();
    }

    private void handleServerLine(String line) {
        if (line.startsWith("USERLIST::")) {
            String list = line.substring(10);
            userList.getItems().setAll(list.isEmpty() ? new java.util.ArrayList<>() : java.util.Arrays.asList(list.split(",")));
        } else if (line.startsWith("MSG::")) {
            String[] p = line.split("::", 3);
            if (p.length == 3) {
                String from = p[1];
                String payload = p[2];
                String decrypted = decrypt(payload);
                appendMessage(from, decrypted, false);
            }
        } else {
            chatArea.appendText("[RAW] " + line + "\\n");
        }
    }

    private void appendMessage(String from, String text, boolean outgoing) {
        // Simple bubble-like formatting in text area
        if (outgoing) {
            chatArea.appendText("[You -> " + from + "] " + text + "\\n");
        } else {
            chatArea.appendText("[" + from + "] " + text + "\\n");
        }
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) return;
        String to = "*";
        if (privateToggle.isSelected()) {
            String sel = userList.getSelectionModel().getSelectedItem();
            if (sel == null) {
                chatArea.appendText("[System] Select a user for private message.\\n");
                return;
            }
            to = sel;
        }
        String enc = encrypt(text);
        out.println("MSG::" + to + "::" + enc);
        inputField.clear();
        if (!to.equals("*")) {
            appendMessage(to, text, true);
        } else {
            appendMessage("All", text, true);
        }
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
            return "[decrypt error]";
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null && !socket.isClosed()) socket.close();
    }

    public static void main(String[] args) {
        launch();
    }
}
