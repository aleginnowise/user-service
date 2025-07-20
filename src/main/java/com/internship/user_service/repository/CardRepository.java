package com.internship.user_service.repository;

import com.internship.user_service.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    @Query(value = "SELECT c FROM Card c WHERE c.id IN :ids")
    List<Card> getAllById(@Param("ids") Iterable<Long> ids);
}