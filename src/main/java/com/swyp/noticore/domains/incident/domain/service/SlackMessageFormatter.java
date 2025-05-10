package com.swyp.noticore.domains.incident.domain.service;

import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.TextObject;
import com.slack.api.webhook.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SlackMessageFormatter {

    public Payload formatGeneralErrorMessage(String title) {
        Payload payload = Payload.builder()
                .text("New Error Notification alerted")
                .blocks(List.of(
                        SectionBlock.builder()
                                .text(markdown("Title: " + title + "\nPlease check your mailbox for detailed information."))
                                .build(),
                        DividerBlock.builder().build()
                ))
                .build();
        return payload;
        }

    private static TextObject markdown(String text) {
        return BlockCompositions.markdownText(text);
    }
}
