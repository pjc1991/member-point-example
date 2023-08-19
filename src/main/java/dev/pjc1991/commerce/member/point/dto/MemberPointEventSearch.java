package dev.pjc1991.commerce.member.point.dto;

import lombok.Getter;

@Getter
public class MemberPointEventSearch {

    // 회원 아이디
    private int memberId;
    // 페이지 번호 (0부터 시작)
    private int page = 0;
    // 페이지 크기
    private int size = 10;
    // 조회된 전체 데이터 수
    private int total;
    // 조회 시작 위치
    private int offset = 0;

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    /**
     * 페이지 번호나 페이지 크기를 설정하면 조회 시작 위치를 계산합니다.
     * @param page
     * 페이지 번호 (0부터 시작)
     */
    public void setPage(int page) {
        this.page = page;
        calculateOffset();
    }

    /**
     * 페이지 번호나 페이지 크기를 설정하면 조회 시작 위치를 계산합니다.
     * @param size
     * 페이지 크기
     */
    public void setSize(int size) {
        this.size = size;
        calculateOffset();
    }

    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 조회 시작 위치를 계산합니다.
     */
    private void calculateOffset() {
        this.offset = page * size;
    }

}
