package dev.pjc1991.commerce.member.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.pjc1991.commerce.dto.ErrorResponse;
import dev.pjc1991.commerce.member.point.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 회원 적립금 도메인의 예외를 처리하기 위한 컨트롤러 어드바이스
 *
 * @see MemberPointExceptionInterface
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class MemberPointExceptionHandleController {


    /**
     * 회원 적립금 타입이 잘못된 경우 발생하는 예외
     *
     * @param request 요청
     * @param e       예외
     * @return 에러 응답
     */
    @ExceptionHandler(NotEnoughPointException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleNotEnoughPointException(HttpServletRequest request, NotEnoughPointException e) {
        logError(request, e);
        return new ErrorResponse(e);
    }

    /**
     * 회원 적립금 금액이 잘못된 경우 발생하는 예외
     *
     * @param request 요청
     * @param e       예외
     * @return 에러 응답
     */
    @ExceptionHandler(BadMemberPointAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBadMemberPointAmountException(HttpServletRequest request, BadMemberPointAmountException e) {
        logError(request, e);
        return new ErrorResponse(e);
    }

    /**
     * 회원 적립금 만료일이 잘못된 경우 발생하는 예외
     *
     * @param request 요청
     * @param e       예외
     * @return 에러 응답
     */
    @ExceptionHandler(BadMemberPointExpireDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBadMemberPointExpireDateException(HttpServletRequest request, BadMemberPointExpireDateException e) {
        logError(request, e);
        return new ErrorResponse(e);
    }

    /**
     * 회원 적립금 타입이 잘못된 경우 발생하는 예외
     *
     * @param request 요청
     * @param e
     * @return 에러 응답
     */
    @ExceptionHandler(BadMemberPointTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBadMemberPointTypeException(HttpServletRequest request, BadMemberPointTypeException e) {
        logError(request, e);
        return new ErrorResponse(e);
    }

    /**
     * 회원 적립금 상세가 존재하지 않는 경우 발생하는 예외
     *
     * @param request 요청
     * @param e       예외
     * @return 에러 응답
     */
    @ExceptionHandler(MemberPointDetailNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleMemberPointDetailNotFoundException(HttpServletRequest request, MemberPointDetailNotFoundException e) {
        logError(request, e);
        return new ErrorResponse(e);
    }

    /**
     * 회원 적립금 사용시 무한루프가 발생하는 경우 발생하는 예외
     * 매커니즘의 문제이므로 구체적인 에러 메시지는 노출하지 않습니다.
     *
     * @param e 예외
     * @return 에러 응답
     */
    @ExceptionHandler(MemberPointUseInfiniteLoopException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleMemberPointUseInfiniteLoopException(HttpServletRequest request, MemberPointUseInfiniteLoopException e) {
        logError(request, e);
        return new ErrorResponse();
    }

    /**
     * 회원 적립금 적립/사용 내역이 이미 취소되었을 때 발생하는 예외
     *
     * @param request 요청
     * @param e       예외
     * @return 에러 응답
     */
    @ExceptionHandler(MemberPointAlreadyRollbackedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleMemberPointAlreadyRollbackedException(HttpServletRequest request, MemberPointAlreadyRollbackedException e) {
        logError(request, e);
        return new ErrorResponse(e);
    }

    /**
     * 회원 적립금 정산 처리가 비정상적으로 이루저였을 때 발생하는 예외
     * 예외를 외부에 완전히 노출할 경우 보안상의 문제가 발생할 수 있으므로
     * 구체적인 에러 메시지는 노출하지 않습니다.
     *
     * @param request 요청
     * @param e       예외
     * @return 에러 응답
     */
    @ExceptionHandler(MemberPointAmountBrokenException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleMemberPointAmountBrokenException(HttpServletRequest request, MemberPointAmountBrokenException e) {
        logError(request, e);
        return new ErrorResponse();
    }

    /**
     * 그 외의 예외를 처리하기 위한 핸들러
     * 예외를 외부에 완전히 노출할 경우 보안상의 문제가 발생할 수 있으므로
     * 구체적인 에러 메시지는 노출하지 않습니다.
     *
     * @param e 예외
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleException(HttpServletRequest request, Exception e) {
        logError(request, e);
        return new ErrorResponse();
    }


    /**
     * 에러 로그를 남깁니다.
     *
     * @param request 요청
     * @param e       예외
     */
    private void logError(HttpServletRequest request, Exception e) {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String errorName = e.getClass().getSimpleName();
        String errorMessage = e.getMessage();
        log.error("{} {} {} {}", method, requestUri, errorName, errorMessage);
    }

}
