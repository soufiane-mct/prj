package com.aliboo.book.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    //Optional kdar bsh thiyd exaptions
    Optional<Token> findByToken(String token);
}
