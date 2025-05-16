package com.swyp.noticore.domains.member.presentation;

import com.swyp.noticore.domains.member.application.dto.request.MemberRequest;
import com.swyp.noticore.domains.member.application.dto.response.MemberInfo;
import com.swyp.noticore.domains.member.application.usecase.MemberCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberCommandUseCase memberCommandUseCase;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid MemberRequest request) {
        memberCommandUseCase.create(request);
        return ResponseEntity.ok("Inserted");
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberInfo> get(@PathVariable Long memberId) {
        MemberInfo member = memberCommandUseCase.get(memberId);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<String> update(@PathVariable Long memberId, @RequestBody @Valid MemberRequest request) {
        memberCommandUseCase.update(request, memberId);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> delete(@PathVariable Long memberId) {
        memberCommandUseCase.delete(memberId);
        return ResponseEntity.ok("Deleted");
    }
}
