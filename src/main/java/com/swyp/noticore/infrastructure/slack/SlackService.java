package com.swyp.noticore.infrastructure.slack;

import org.springframework.http.ResponseEntity;

public interface SlackService {
    ResponseEntity sendErrorNotification() throws Exception;
}
