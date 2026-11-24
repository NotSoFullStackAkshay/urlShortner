package com.example.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Returns RFC 7807 problem+json. Bean-validation errors are handled by the parent class (400). */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    ProblemDetail notFound(UrlNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UrlExpiredException.class)
    ProblemDetail expired(UrlExpiredException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, e.getMessage());
    }

    @ExceptionHandler(AliasAlreadyExistsException.class)
    ProblemDetail conflict(AliasAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    ProblemDetail invalid(InvalidUrlException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
