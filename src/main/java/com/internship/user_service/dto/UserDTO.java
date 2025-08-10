package com.internship.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    @NotNull(message = "User id may not be null")
    private UUID id;

    @NotBlank(message = "Name may not be blank")
    @Size(min = 2, max = 100, message = "Name may not be less than 2 and greater than 100")
    private String name;

    @NotBlank(message = "Surname may not be blank")
    @Size(min = 2, max = 100, message = "Surname may not be less than 2 and greater than 100")
    private String surname;

    @NotNull(message = "Birth date may not be null")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Email may not be blank")
    @Email(message = "Incorrect email")
    private String email;

    private List<CardDTO> cards;
}