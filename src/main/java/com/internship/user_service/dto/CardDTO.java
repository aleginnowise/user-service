package com.internship.user_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardDTO {
    @NotNull(message = "Card id may not be null")
    private UUID id;

    @NotNull(message = "User id may not be null")
    private UUID userId;

    @NotBlank(message = "Card number may not be blank")
    @Size(min = 16, max = 16, message = "Card number may not be greater or less than 16")
    private String number;

    @NotBlank(message = "Holder may not be blank")
    @Size(min = 2, max = 100, message = "Holder name may not be less than 2 and greater than 100")
    private String holder;

    @NotNull(message = "Expiration date may not be null")
    @Future(message = "Expiration date must be in future")
    private LocalDate expirationDate;
}