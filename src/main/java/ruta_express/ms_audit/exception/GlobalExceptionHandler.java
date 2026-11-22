package ruta_express.ms_audit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// errores HTTP de audit con ProblemDetail (contrato 5)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: un dato de la URL con el tipo equivocado (por ejemplo /shipments/abc, cuando el id es un número)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El valor '" + ex.getValue() + "' no es válido para " + ex.getName());
        problem.setTitle("Solicitud inválida");
        return problem;
    }

    // 500: cualquier otro error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        // Spring Framework 7: forStatusAndDetail pide HttpStatusCode, ya no un int
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error interno en audit: " + ex.getMessage());
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
