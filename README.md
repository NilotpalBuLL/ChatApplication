
```markdown
# 💬 Java Chat Application (JavaFX + Socket Programming)

## 📘 Overview
The **Java Chat Application** is a real-time, peer-to-peer communication system built using **Java, JavaFX, and Socket Programming**.  
It allows multiple clients to connect to a central server and exchange messages instantly through both **group** and **private chat** modes.  
The application also features a **modern graphical user interface** with multiple themes and basic message encryption for secure communication.

---

## ✨ Features
- 🔹 Real-time group and private chat  
- 🔹 Client–Server communication via TCP sockets  
- 🔹 Multi-threaded server using `ClientHandler`  
- 🔹 Basic XOR encryption for secure message transfer  
- 🔹 Customizable UI/UX with multiple visual themes  
- 🔹 Simple, modular code structure for future AI or DB integration  

---

## 🛠️ Tools & Technologies Used
- **Java (JDK 17+)**
- **JavaFX Framework** – for GUI
- **Socket Programming (TCP)**
- **Threads (ClientHandler class)** – for concurrency
- **Gradle Build Tool** – for dependencies and project execution
- **XOR Encryption Algorithm**

---

## 🧩 Project Structure
```

JavaChatApp_UI/
│
├── src/
│   ├── server/
│   │   └── Server.java
│   ├── client/
│   │   ├── ChatClient.java
│   │   ├── ClientHandler.java
│   │   └── controllers/
│   │       └── ChatController.java
│   ├── ui/
│   │   └── resources/
│   │       ├── themes/
│   │       │   ├── Classic.css
│   │       │   ├── DiscordDark.css
│   │       │   ├── Minimal.css
│   │       │   └── Glass.css
│   │       └── fxml/
│   │           └── chat.fxml
│   └── utils/
│       └── Encryption.java
│
├── build.gradle
├── settings.gradle
└── README.md

````

---

## ⚙️ How to Run the Project

### **1. Prerequisites**
Ensure you have:
- **JDK 17 or higher**
- **Gradle** (if not bundled)
- **JavaFX SDK** (if not included with your JDK)
- A Java IDE (like **IntelliJ IDEA**, **Eclipse**, or **VS Code**) or command-line setup

---

### **2. Running the Server**
1. Open a terminal or run configuration for the server.
2. Navigate to the `server` package.
3. Run the `Server.java` file:
   ```bash
   java Server
````

The server will start and wait for client connections.

---

### **3. Running the Client**

1. Open another terminal or IDE window.
2. Navigate to the `client` package.
3. Run the `ChatClient.java` file:

   ```bash
   java ChatClient
   ```
4. Enter a username when prompted.
5. Start chatting — messages will appear in real-time across all connected clients.

---

### **4. Using Gradle (Optional)**

If the project uses **Gradle**, you can run it directly:

```bash
gradle run
```

or build a JAR:

```bash
gradle build
```

Then run:

```bash
java -jar build/libs/JavaChatApp_UI.jar
```

---

## 🎨 Themes Available

You can switch between four UI themes directly in the application:

1. **Classic**
2. **DiscordDark**
3. **Minimal**
4. **Glass**

---

## 🔐 Encryption

Messages are encrypted and decrypted using a **basic XOR algorithm** before transmission.
This provides lightweight security while demonstrating encryption principles.

---

## 🚀 Future Enhancements

* Database integration for message history
* File sharing and media attachments
* AI-powered auto-reply system
* Cloud server deployment

---

## 👨‍💻 Author

**Developed by:** Nilotpal sarma
**Technologies:** Java, JavaFX, Socket Programming, Gradle
**Purpose:** Academic project showcasing real-time communication and UI design

---

## 🏁 Conclusion

The **Java Chat Application** successfully combines networking and interface design to create a secure and user-friendly chat system.
It serves as a foundation for building scalable, modern real-time communication tools.

---

```
