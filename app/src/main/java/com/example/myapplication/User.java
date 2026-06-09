package com.example.myapplication;

public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
    private String imgpro;

    public User() {
    }

    public User(String username, String firstName, String lastName, String email, String address, String phone, String imgpro) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.imgpro = imgpro;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImgpro() {
        return imgpro;
    }

    public void setImgpro(String imgpro) {
        this.imgpro = imgpro;
    }
}
