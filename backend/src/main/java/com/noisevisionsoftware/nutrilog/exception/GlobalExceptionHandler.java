package com.noisevisionsoftware.nutrilog.exception;

import com.noisevisionsoftware.nutrilog.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Wystąpił błąd serwera"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setDetail(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
