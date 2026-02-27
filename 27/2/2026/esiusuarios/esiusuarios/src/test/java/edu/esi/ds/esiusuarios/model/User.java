package edu.esi.ds.esiusuarios.model;

public class User {

    private String name;
    private String password;
    private String token;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.token = null;
    }

    public User(String name, String password, String token) {
        this.name = name;
        this.password = password;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
