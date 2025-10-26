
package com.chatapp.server;

// Simple AI responder stub. Replace the reply(...) implementation with calls to an external API
// (e.g. OpenAI) if you want the server to perform richer AI interactions.
public class AIResponder {
    public String reply(String userMessage) {
        if (userMessage == null) return "I didn't get that.";
        String lower = userMessage.trim().toLowerCase();
        if (lower.startsWith("hi") || lower.startsWith("hello")) {
            return "Hello! I'm your chat assistant. Ask me anything or use /help.";
        } else if (lower.contains("time")) {
            return "Server time: " + new java.util.Date().toString();
        } else if (lower.contains("help")) {
            return "Try commands: 'time', 'tell me a joke', or ask a question.";
        } else if (lower.contains("joke")) {
            return "Why do programmers prefer dark mode? Because light attracts bugs!";
        } else {
            // Placeholder: call external API here and return result.
            return "AI (demo): I received — "" + userMessage + "". (Replace AIResponder.reply with an API call for real AI).";
        }
    }
}
