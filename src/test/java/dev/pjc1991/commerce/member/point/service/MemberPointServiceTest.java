package dev.pjc1991.commerce.member.point.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(true)
class MemberPointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MemberPointServiceTest.class);

    @Autowired
    MemberPointService memberPointService;

    @Test
    void getMemberPointTotal() {
    }

    @Test
    void getMemberPointLogList() {
    }

    @Test
    void earnMemberPoint() {
    }

    @Test
    void useMemberPoint() {
    }
}