package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.filmorate.exception.DuplicateDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Валидация данных не пройдена: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedDataException(DuplicateDataException ex) {
        log.warn("Попытка добавления дублирующих данных: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.info("Запрашиваемый ресурс не найден: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Ошибка валидации входящих данных: {}", errorMessages);
        ErrorResponse errorResponse = new ErrorResponse(errorMessages);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        log.error("Ошибка доступа к данным", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "Произошла ошибка при работе с базой данных. Попробуйте позже."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleValidationException(Exception ex) {
        log.error("Непредвиденное исключение", ex);
        ErrorResponse errorResponse = new ErrorResponse("Произошла непредвиденная ошибка");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}