package com.swyp.noticore.infrastructure.slack;

import org.springframework.http.ResponseEntity;

public interface SlackService {
    ResponseEntity<String> sendErrorNotification();
}
