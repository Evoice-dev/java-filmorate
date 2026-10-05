package ru.yandex.practicum.filmorate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDate(DateTimeParseException e) {
        return new ErrorResponse("Неверный формат даты: " + e.getParsedString());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof ValidationException) {
            return new ErrorResponse(cause.getMessage());
        }
        if (cause instanceof DateTimeParseException) {
            return new ErrorResponse("Неверный формат даты");
        }
        return new ErrorResponse("Некорректный JSON");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Throwable e) {
        return new ErrorResponse("Внутренняя ошибка сервера");
    }
}