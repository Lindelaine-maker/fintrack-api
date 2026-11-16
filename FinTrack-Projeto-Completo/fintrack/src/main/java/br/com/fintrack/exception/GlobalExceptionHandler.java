package br.com.fintrack.exception;

import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<String,Object> body(HttpStatus status,String message){
        return Map.of("timestamp", Instant.now().toString(),"status",status.value(),
                "erro",status.getReasonPhrase(),"mensagem",message);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> notFound(ResourceNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND,e.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    ResponseEntity<?> conflict(ResourceConflictException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT,e.getMessage()));
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    ResponseEntity<?> insufficientBalance(SaldoInsuficienteException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT,e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> integrity(DataIntegrityViolationException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT,
                "Registro duplicado ou relacionado a outro recurso."));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<?> unauthenticated(AuthenticationException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> forbidden(AccessDeniedException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN,
                "Você não tem permissão para acessar este recurso."));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    ResponseEntity<?> validation(Exception e){
        String message = "Dados inválidos.";
        if(e instanceof MethodArgumentNotValidException m){
            message = m.getBindingResult().getFieldErrors().stream()
                    .map(x -> x.getField()+": "+x.getDefaultMessage()).findFirst().orElse(message);
        } else if(e instanceof IllegalArgumentException || e instanceof ValidationException){
            if(e.getMessage()!=null && !e.getMessage().isBlank()) message=e.getMessage();
        } else if(e instanceof MethodArgumentTypeMismatchException m){
            message="Parâmetro inválido: "+m.getName()+".";
        }
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST,message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> generic(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado."));
    }
}
