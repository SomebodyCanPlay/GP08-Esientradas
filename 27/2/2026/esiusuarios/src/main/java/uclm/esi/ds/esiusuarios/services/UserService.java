package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private List<User> users;

    public UserService() {
        this.users = List.of(
            new User("Pepe", "pepe123"),
            new User("Ana", "ana123"));
    }

    public String login(String name, String password) {
        for (User user : this.users) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                return "Login successful";
            }
        }
        return null;
    }

    public String checkToken(String token) {
        for (User user : this.users) {
            if (user.getToken().equals(token)) {
                return user.getName();
            }
        }
        return null;
    }
}