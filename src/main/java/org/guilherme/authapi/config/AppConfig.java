package org.guilherme.authapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    private final Jwt jwt = new Jwt();
    private final Mail mail = new Mail();
    private final Security security = new Security();
    private final Verification verification = new Verification();
    
    public static class Jwt {
        private String secret;
        private long expiration;
        private long refreshExpiration;
        
        public String getSecret() {
            return secret;
        }
        
        public void setSecret(String secret) {
            this.secret = secret;
        }
        
        public long getExpiration() {
            return expiration;
        }
        
        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
        
        public long getRefreshExpiration() {
            return refreshExpiration;
        }
        
        public void setRefreshExpiration(long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }
    
    public static class Mail {
        private String host;
        private int port;
        private String username;
        private String password;
        private boolean smtpAuth;
        private boolean starttlsEnable;
        private String fromAddress;
        private String fromName;
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public boolean isSmtpAuth() {
            return smtpAuth;
        }
        
        public void setSmtpAuth(boolean smtpAuth) {
            this.smtpAuth = smtpAuth;
        }
        
        public boolean isStarttlsEnable() {
            return starttlsEnable;
        }
        
        public void setStarttlsEnable(boolean starttlsEnable) {
            this.starttlsEnable = starttlsEnable;
        }
        
        public String getFromAddress() {
            return fromAddress;
        }
        
        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }
        
        public String getFromName() {
            return fromName;
        }
        
        public void setFromName(String fromName) {
            this.fromName = fromName;
        }
    }
    
    public static class Security {
        private int passwordMinLength = 8;
        private int passwordMaxLength = 30;
        private int passwordMinUppercase = 1;
        private int passwordMinLowercase = 1;
        private int passwordMinDigit = 1;
        private int passwordMinSpecial = 1;
        
        public int getPasswordMinLength() {
            return passwordMinLength;
        }
        
        public void setPasswordMinLength(int passwordMinLength) {
            this.passwordMinLength = passwordMinLength;
        }
        
        public int getPasswordMaxLength() {
            return passwordMaxLength;
        }
        
        public void setPasswordMaxLength(int passwordMaxLength) {
            this.passwordMaxLength = passwordMaxLength;
        }
        
        public int getPasswordMinUppercase() {
            return passwordMinUppercase;
        }
        
        public void setPasswordMinUppercase(int passwordMinUppercase) {
            this.passwordMinUppercase = passwordMinUppercase;
        }
        
        public int getPasswordMinLowercase() {
            return passwordMinLowercase;
        }
        
        public void setPasswordMinLowercase(int passwordMinLowercase) {
            this.passwordMinLowercase = passwordMinLowercase;
        }
        
        public int getPasswordMinDigit() {
            return passwordMinDigit;
        }
        
        public void setPasswordMinDigit(int passwordMinDigit) {
            this.passwordMinDigit = passwordMinDigit;
        }
        
        public int getPasswordMinSpecial() {
            return passwordMinSpecial;
        }
        
        public void setPasswordMinSpecial(int passwordMinSpecial) {
            this.passwordMinSpecial = passwordMinSpecial;
        }
    }
    
    public static class Verification {
        private int tokenExpirationHours = 24;
        private String baseUrl = "http://localhost:8080";
        private String verificationEndpoint = "/api/auth/verify";
        
        public int getTokenExpirationHours() {
            return tokenExpirationHours;
        }
        
        public void setTokenExpirationHours(int tokenExpirationHours) {
            this.tokenExpirationHours = tokenExpirationHours;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getVerificationEndpoint() {
            return verificationEndpoint;
        }
        
        public void setVerificationEndpoint(String verificationEndpoint) {
            this.verificationEndpoint = verificationEndpoint;
        }
        
        public String getVerificationUrl(String token) {
            return baseUrl + verificationEndpoint + "?token=" + token;
        }
    }
    
    public Jwt getJwt() {
        return jwt;
    }
    
    public Mail getMail() {
        return mail;
    }
    
    public Security getSecurity() {
        return security;
    }
    
    public Verification getVerification() {
        return verification;
    }
} 