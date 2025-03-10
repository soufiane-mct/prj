package com.aliboo.book.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum BusinessErrorCode {//hna atakhd l information code d err ka int description o a takd ta l http li aredoh l user

    NO_CODE(0, NOT_IMPLEMENTED, "No code"), //3emrna dok vars li ltht b hd no_code
    INCORRECT_CURRENT_PASSWORD(300 , BAD_REQUEST, "Current password is incorrect"),//nfs blan o hdshi ra ankhdmo bih mn bead
    NEW_PASSWORD_DOES_NOT_MATCH(301 , BAD_REQUEST, "The new password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"), //nfs blan f ACCOUNT_LOCKED
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Login and / or pass in incorrect") //o hado ankhdmo bihom f globalexceptionhandler bch ytl3o l user


    ;
    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;


    BusinessErrorCode(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
