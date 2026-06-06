package com.ssaika.ssiren.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(Include.NON_NULL)
public class BaseResponse<T> {

    private Integer status;
    private String code; // 실패시에만 나가는 ErrorCode의 name() 값
    private String message;
    private T data;

    /**
     * 성공 시 응답 status 포함
     * @param status HTTPStatus
     * @param message FE <- BE 전달용 메세지
     * @param data 실제 데이터
     * @return BaseResponse: 공통 Response 타입
     * @param <T> Generic
     */
    public static <T> BaseResponse<T> success(HttpStatus status, String message, T data){
        return new BaseResponse<>(status.value(), null, message, data);
    }

    /**
     * 200 OK 경우 사용
     * 성공 시 응답 status 미포함 시 200 OK default
     * @param message FE <- BE 전달용 메세지
     * @param data 실제 데이터
     * @return BaseResponse: 공통 Response 타입
     * @param <T> Generic
     */
    public static <T> BaseResponse<T> success(String message, T data){
        return new BaseResponse<>(HttpStatus.OK.value(), null, message, data);
    }

    // 204 NO Content 경우 사용
    // ResponseEntity.status(HttpStatus.NoContent)을 사용할 경우,
    // 프론트에 아무 내용이 나가지 않기 때문에 해당 메서드 사용
    public static <T> BaseResponse<T> success(String message) {
        return new BaseResponse<>(HttpStatus.NO_CONTENT.value(), null, message, null);
    }

    /**
     * 실패 시 응답
     * @param errorCode ErrorCode Enum
     * @return data는 실패 시 null
     */
    public static BaseResponse<Object> fail(ErrorCode errorCode) {
        return new BaseResponse<>(
            errorCode.getHttpStatus().value(), // Integer 상태코드 (ex: 400)
            errorCode.name(),                  // ErrorCode Enum의 name() 값
            errorCode.getMessage(),            // 에러 메시지
            null                               // 에러 데이터는 없으므로 null
        );
    }

    /**
     * 실패 시 응답, 메세지 커스텀
     * @param errorCode ErrorCode Enum
     * @param message 커스텀 메시지
     * @return data는 실패 시 null
     */
    public static BaseResponse<Object> fail(ErrorCode errorCode, String message) {
        return new BaseResponse<>(
            errorCode.getHttpStatus().value(), // Integer 상태코드 (ex: 400)
            errorCode.name(),                  // ErrorCode Enum의 name() 값
            message,                           // 에러 메시지
            null                               // 에러 데이터는 없으므로 null
        );
    }

    /**
     * 실패 시 응답, Status, 메세지 커스텀
     * Status는 ErrorCode에 없는 경우, 또는 상황에 따라 다르게 주고 싶은 경우 사용
     * @param status HTTPStatus
     * @param message 커스텀 메시지
     * @return data는 실패 시 null
     */
    public static BaseResponse<Object> fail(HttpStatus status, String message) {
        return new BaseResponse<>(
            status.value(),                     // Integer 상태코드 (ex: 400)
            null,
            message,                           // 에러 메시지
            null                               // 에러 데이터는 없으므로 null
        );
    }
}
