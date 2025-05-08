package com.swyp.noticore.domains.member.presentation;

import com.swyp.noticore.domains.member.application.dto.request.MemberRequest;
import com.swyp.noticore.domains.member.application.dto.request.MemberKeyRequest;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.application.usecase.MemberCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberCommandUseCase memberCommandUseCase;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid MemberRequest request) {
        memberCommandUseCase.create(request);
        return ResponseEntity.ok("Inserted");
    }

    @PostMapping("/get")
    public ResponseEntity<MemberInfo> get(@RequestBody @Valid MemberKeyRequest request) {
        MemberInfo member = memberCommandUseCase.get(request);
        return ResponseEntity.ok(member);
    }

    @PutMapping
    public ResponseEntity<String> update(@RequestBody @Valid MemberRequest request) {
        memberCommandUseCase.update(request);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping
    public ResponseEntity<String> delete(@RequestBody @Valid MemberKeyRequest request) {
        memberCommandUseCase.delete(request);
        return ResponseEntity.ok("Deleted");
    }
}
