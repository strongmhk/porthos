# 알림 채널 추가 스킬

`IncidentInfoUseCase.processAndForward()`의 알림 플로우에 새로운 알림 채널을 추가한다.

## 현재 알림 채널

1. **Email** → `EmailService` (AWS SES)
2. **SMS** → `SmsService` (AWS SNS)
3. **OnCall** → `OncallService` (AWS Lambda)
4. **Slack** → `SlackService` (Slack Webhook)

## 작업 순서

1. `MemberEntity`에 새 채널의 연락처 필드 추가 (없는 경우)
2. `MemberInfo` DTO에 해당 필드 추가
3. `MemberInfoMapper`에 새 채널의 수신자 추출 메서드 추가
4. `{Channel}Service` 생성 (domain/service 아래)
5. `IncidentInfoUseCase`에 새 서비스 의존성 주입 및 호출 추가

## 알림 채널 서비스 패턴

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class {Channel}Service {

    public void send{Channel}Alert(String subject, String recipient) {
        try {
            // 외부 API 호출
        } catch (Exception e) {
            log.error("Failed to send {Channel} alert to {}: {}", recipient, e.getMessage());
        }
    }
}
```

## IncidentInfoUseCase 호출 위치

`processAndForward()` 메서드의 10번 이후 스텝에 추가:

```java
// N. {Channel} 전송
MemberInfoMapper.mapTo{Channel}Recipients(allMembers).stream()
    .forEach(recipient -> {channel}Service.send{Channel}Alert(subject, recipient));
```

## 알림 수신 설정 (MemberMetadata)

`MemberMetadataEntity`에 채널별 수신 여부 플래그가 있는지 확인 후 필터링 로직 추가 필요.
