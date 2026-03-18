package com.swyp.noticore.domains.incident.domain.service;

public interface OncallSender {

    void triggerOnCall(String subject, String phoneNumber);
}
