package com.ssaika.ssiren.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode)
        throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(BaseResponse.fail(errorCode)));
    }
}
