package com.swyp.noticore.domains.errorinfo.application.usecase;

import com.swyp.noticore.domains.errorinfo.domain.service.EmlDownloadService;
import com.swyp.noticore.domains.errorinfo.domain.service.ErrorInfoParsingService;
import com.swyp.noticore.global.annotation.architecture.UseCase;
import java.io.InputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@UseCase
@RequiredArgsConstructor
public class ErrorInfoUseCase {

    private final EmlDownloadService emlDownloadService;
    private final ErrorInfoParsingService errorInfoParsingService;

    public void processAndForward(Map<String, String> payload) {
        InputStream inputStream = emlDownloadService.download(payload);
        errorInfoParsingService.parseErrorInfoAndForward(inputStream);
    }
}
