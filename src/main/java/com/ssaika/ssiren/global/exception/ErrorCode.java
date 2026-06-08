package com.ssaika.ssiren.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorCode {

    // ============================================================
    // * COMMON
    // ============================================================
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "파라미터 값이 잘못되었습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "타입이 불일치합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리소스입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "중복된 리소스입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "입력 형식이 올바르지 않습니다."),

    // ============================================================
    // * AUTH
    // ============================================================
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "액세스 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "로그아웃 된 사용자입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    WRONG_TYPE_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 서명 또는 형식의 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인에 실패했습니다."),
    SOCIAL_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 연동 해제에 실패했습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),
    OAUTH_TOKEN_FAILED(HttpStatus.UNAUTHORIZED, "카카오 요청 중 에러가 발생했습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.UNAUTHORIZED, "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH_PROFILE_FAILED(HttpStatus.UNAUTHORIZED, "카카오 프로필 조회 중 에러가 발생했습니다."),
    OAUTH_SESSION_EXPIRED(HttpStatus.BAD_REQUEST, "회원가입 세션이 만료되었습니다."),
    OAUTH_TEMP_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 정보를 불러올 수 없습니다."),

    // ============================================================
    // * USER (사용자)
    // ============================================================
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    USER_WITHDRAW(HttpStatus.FORBIDDEN, "탈퇴한 유저입니다."),
    OAUTH_TEMP_SAVE_FAILED(HttpStatus.BAD_REQUEST, "임시 저장 실패"),
    USER_ROLE_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 역할입니다."),

    // ============================================================
    // * AI
    // ============================================================
    AI_SERVER_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "AI 서버에 연결할 수 없습니다."),
    AI_SERVER_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI 서버 응답 시간이 초과되었습니다."),
    AI_SERVER_RESPONSE_ERROR(HttpStatus.BAD_GATEWAY, "AI 서버 응답이 올바르지 않습니다.");



    // ============================================================
    // *
    // ============================================================



    private final HttpStatus httpStatus;
    private final String message;
}
