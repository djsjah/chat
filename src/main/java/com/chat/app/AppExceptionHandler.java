package com.chat.app;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import com.chat.app.dto.ApiErrorDTO;
import com.chat.app.dto.FieldErrorDTO;

@RestControllerAdvice
@Slf4j
public class AppExceptionHandler {
    private static ResponseEntity<ApiErrorDTO> build(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiErrorDTO(status.value(), status.getReasonPhrase(), message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorDTO> handle(MethodArgumentTypeMismatchException ex) {
        return build(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "'"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handle(MethodArgumentNotValidException ex) {
        List<FieldErrorDTO> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldErrorDTO(err.getField(), err.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDTO(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDTO> handle(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatEx) {
            String fieldName = invalidFormatEx.getPath()
                    .stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .reduce((first, second) -> second)
                    .orElse("unknown");

            String targetType = invalidFormatEx.getTargetType().getSimpleName();
            String errorMessage = "Invalid value for field '" + fieldName + "', expected type " + targetType;

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiErrorDTO(
                            HttpStatus.BAD_REQUEST.value(),
                            HttpStatus.BAD_REQUEST.getReasonPhrase(),
                            errorMessage));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Malformed JSON request"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> handle(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handle(Exception ex) {
        log.error("Unhandled exception", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorDTO(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Internal server error"
                ));
    }
}
