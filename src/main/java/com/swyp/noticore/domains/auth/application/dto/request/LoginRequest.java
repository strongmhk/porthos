package com.swyp.noticore.domains.auth.application.dto.request;

public record LoginRequest(

    String email,
    String password
) {

}