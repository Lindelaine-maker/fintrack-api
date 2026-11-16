package br.com.fintrack.exception;

import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<String,Object> body(HttpStatus status,String message){
        return Map.of("timestamp", Instant.now().toString(),"status",status.value(),"erro",status.getReasonPhrase(),"mensagem",message);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> notFound(ResourceNotFoundException e){return ResponseEntity.status(404).body(body(HttpStatus.NOT_FOUND,e.getMessage()));}
    @ExceptionHandler(SaldoInsuficienteException.class)
    ResponseEntity<?> conflict(SaldoInsuficienteException e){return ResponseEntity.status(409).body(body(HttpStatus.CONFLICT,e.getMessage()));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> integrity(DataIntegrityViolationException e){return ResponseEntity.status(409).body(body(HttpStatus.CONFLICT,"Registro duplicado ou relacionado a outro recurso."));}
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    ResponseEntity<?> validation(Exception e){
        String msg=e instanceof MethodArgumentNotValidException m
            ? m.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).findFirst().orElse("Dados inválidos")
            : e.getMessage();
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST,msg));
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> forbidden(AccessDeniedException e){return ResponseEntity.status(403).body(body(HttpStatus.FORBIDDEN,"Você não tem permissão para acessar este recurso."));}
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> generic(Exception e){return ResponseEntity.status(500).body(body(HttpStatus.INTERNAL_SERVER_ERROR,"Erro interno inesperado."));}
}
