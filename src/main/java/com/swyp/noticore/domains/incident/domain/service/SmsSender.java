package com.swyp.noticore.domains.incident.domain.service;

public interface SmsSender {

    void sendSmsAlert(String subject, String phoneNumber);
}
