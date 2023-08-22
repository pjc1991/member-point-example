package dev.pjc1991.commerce.member.point.component;

import dev.pjc1991.commerce.member.point.service.MemberPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberPointScheduledTasks {

    private final MemberPointService memberPointService;

    /**
     * 회원 적립금 만료 처리
     * 매일 00:00:00에 실행됩니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireMemberPoint() {
        // StopWatch를 사용하여 실행 시간을 측정합니다.
        StopWatch stopWatch = new StopWatch();

        log.info("expireMemberPoint start");
        stopWatch.start();
        memberPointService.expireMemberPoint();
        stopWatch.stop();
        log.info("expireMemberPoint end");

        log.info("expireMemberPoint 총 : {}ms", stopWatch.getTotalTimeMillis());
    }
}
