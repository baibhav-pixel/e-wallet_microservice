package com.example.microservices;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
    @NotNull
    private String name;

    @NotNull
    @Email
    private String email;
    private String phone;

    @NotNull
    private String country;
    @NotNull
    private String nationalId;

    @NotNull
    private String password;

    public User to(){
        return User.builder()
                .name(name)
                .email(email)
                .country(country)
                .nationalId(nationalId)
                .password(password)
                .phone(phone)
                .build();
    }
}
