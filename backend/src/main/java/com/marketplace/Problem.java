package com.marketplace;
import java.util.*;import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import org.springframework.dao.*;import org.springframework.web.bind.MethodArgumentNotValidException;
class Failure extends RuntimeException {final int status;final String code;Failure(int s,String c,String m){super(m);status=s;code=c;}}
@RestControllerAdvice public class Problem {
@ExceptionHandler(Failure.class) ResponseEntity<?> failure(Failure f){return ResponseEntity.status(f.status).body(Map.of("code",f.code,"message",f.getMessage()));}
@ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().body(Map.of("code","INVALID_INPUT","message","Check required fields, values, and limits"));}
@ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(Exception e){return ResponseEntity.status(409).body(Map.of("code","CONFLICT","message","This operation conflicts with current data"));}
@ExceptionHandler(Exception.class) ResponseEntity<?> other(Exception e){org.slf4j.LoggerFactory.getLogger(Problem.class).error("Request failed",e);return ResponseEntity.status(500).body(Map.of("code","INTERNAL_ERROR","message","Unable to complete request"));}
}
