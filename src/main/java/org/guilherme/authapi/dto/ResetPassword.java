package org.guilherme.authapi.dto;

public class ForgotPassword {

    private String email;
    private String newPassword;

    public ForgotPassword() {
    }
    public ForgotPassword(String email, String newPassword) {
        this.email = email;
        this.newPassword = newPassword;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }


}
