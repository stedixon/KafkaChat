package com.testapp.domain;

import lombok.Data;

@Data
public class RegisterUser {

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
}
