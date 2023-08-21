package dev.pjc1991.commerce.dto;

import lombok.Getter;

@Getter
public class PageSearch {
    // 페이지 번호 (0부터 시작)
    private int page = 0;
    // 페이지 크기
    private int size = 10;
    // 조회 시작 위치
    private int offset = 0;

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

    /**
     * 조회 시작 위치를 계산합니다.
     */
    private void calculateOffset() {
        this.offset = page * size;
    }
}
