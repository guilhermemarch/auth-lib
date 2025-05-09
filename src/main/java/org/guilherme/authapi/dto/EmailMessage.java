package org.guilherme.authapi.dto;

public class EmailMessage {
    private String to;
    private String subject;
    private String content;
    private String token;
    private String type;

    public EmailMessage() {
    }

    public EmailMessage(String to, String subject, String content, String token, String type) {
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.token = token;
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
} 