package com.example.myapplication;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
    private String imgpro;
    private double weightKg;   // ✅ for calorie calculation
    private int    heightCm;   // ✅ for future pace zone features
    private int    age;        // ✅ for max HR calculation

    public User() {}

    public User(String firstName, String lastName, String email,
                String address, String phone, String imgpro) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.address   = address;
        this.phone     = phone;
        this.imgpro    = imgpro;
        this.weightKg  = 70.0;
        this.heightCm  = 170;
        this.age       = 25;
    }

    public User(String firstName, String lastName, String email,
                String address, String phone, String imgpro,
                double weightKg, int heightCm, int age) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.address   = address;
        this.phone     = phone;
        this.imgpro    = imgpro;
        this.weightKg  = weightKg;
        this.heightCm  = heightCm;
        this.age       = age;
    }

    public String getFirstName()         { return firstName; }
    public void setFirstName(String v)   { firstName = v; }
    public String getLastName()          { return lastName; }
    public void setLastName(String v)    { lastName = v; }
    public String getEmail()             { return email; }
    public void setEmail(String v)       { email = v; }
    public String getAddress()           { return address; }
    public void setAddress(String v)     { address = v; }
    public String getPhone()             { return phone; }
    public void setPhone(String v)       { phone = v; }
    public String getImgpro()            { return imgpro; }
    public void setImgpro(String v)      { imgpro = v; }
    public double getWeightKg()          { return weightKg; }
    public void setWeightKg(double v)    { weightKg = v; }
    public int getHeightCm()             { return heightCm; }
    public void setHeightCm(int v)       { heightCm = v; }
    public int getAge()                  { return age; }
    public void setAge(int v)            { age = v; }
}