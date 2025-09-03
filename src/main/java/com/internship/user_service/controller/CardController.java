package com.internship.user_service.controller;

import com.internship.user_service.dto.CardDTO;
import com.internship.user_service.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/101FM/cards")
public class CardController {
    private final CardService cardService;

    @PostMapping()
    ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardDTO cardDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(cardDTO));
    }

    @GetMapping("/{id}")
    ResponseEntity<CardDTO> getCard(@PathVariable UUID id){
        return ResponseEntity.ok(cardService.getCard(id));
    }

    @GetMapping(params = "ids")
    ResponseEntity<List<CardDTO>> getCards(@RequestParam("ids") List<UUID> ids){
        return ResponseEntity.ok(cardService.getCards(ids));
    }

    @PutMapping("/{id}")
    ResponseEntity<CardDTO> updateCard(@PathVariable UUID id, @Valid @RequestBody CardDTO cardDTO){
        return ResponseEntity.ok(cardService.updateCard(id, cardDTO));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCard(@PathVariable UUID id){
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}