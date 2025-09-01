package com.internship.user_service.repository;

import com.internship.user_service.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    @Query(value = "SELECT c FROM Card c WHERE c.id IN :ids")
    List<Card> getAllById(@Param("ids") Iterable<UUID> ids);
}