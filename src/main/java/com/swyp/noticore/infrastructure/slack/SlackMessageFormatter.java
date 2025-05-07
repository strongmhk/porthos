package com.swyp.noticore.infrastructure.slack;

import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.TextObject;
import com.slack.api.webhook.Payload;

import java.util.List;


public class SlackMessageFormatter {

    private Payload payload;
    private final String subject;
    private final String sender;
    private final String body;

    public SlackMessageFormatter (String subject, String sender, String body) {
        this.subject = subject;
        this.sender = sender;
        this.body = body;
    }

    public Payload generalErrorMessageFormat () {
        payload = Payload.builder()
                .text("New Error Notification alerted")
                .blocks(List.of(
                        SectionBlock.builder()
                                .text(markdown("Subject: " + subject)).build(),
                        SectionBlock.builder()
                                .text(markdown("Sender: " + sender)).build(),
                        SectionBlock.builder()
                                .text(markdown("Body: " + body)).build(),
                        DividerBlock.builder().build()
                        ))
                .build();
        return payload;
    }

    private static TextObject markdown(String text) {
        return BlockCompositions.markdownText(text);
    }
}
