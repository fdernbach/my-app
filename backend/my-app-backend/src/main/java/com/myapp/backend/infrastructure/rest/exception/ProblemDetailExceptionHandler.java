package com.myapp.backend.infrastructure.rest.exception;

import com.myapp.backend.domain.exception.UserNameAlreadyExistsException;
import com.myapp.backend.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class ProblemDetailExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailExceptionHandler.class);

    @ExceptionHandler(UserNameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUserNameAlreadyExists(UserNameAlreadyExistsException ex,
                                                                     HttpServletRequest request) {
        log.warn("Username conflict: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
        pd.setTitle("Username Already Taken");
        pd.setType(URI.create("urn:problem:duplicate-username"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(CONFLICT).body(pd);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex,
                                                            HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
        pd.setTitle("User Not Found");
        pd.setType(URI.create("urn:problem:user-not-found"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(NOT_FOUND).body(pd);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                              HttpServletRequest request) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(CONFLICT,
                "The resource was modified by another request. Please retry with the latest version.");
        pd.setTitle("Concurrent Modification");
        pd.setType(URI.create("urn:problem:concurrent-modification"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(CONFLICT).body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail pd = ex.getBody();
        pd.setTitle("Validation Failed");
        pd.setType(URI.create("urn:problem:validation-error"));
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of(
                        "field", e.getField(),
                        "message", e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalid"))
                .toList();
        pd.setProperty("errors", errors);
        return handleExceptionInternal(ex, pd, headers, status, request);
    }
}
