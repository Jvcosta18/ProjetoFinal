package com.athletepulse.exception;

import com.athletepulse.dto.ErroResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manipulador global de exceções: converte qualquer erro lançado pelos
 * controllers em uma resposta JSON padronizada ({@link ErroResponse}),
 * com o status HTTP apropriado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Trata violações de regra de negócio, usando o status definido na própria exceção. */
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErroResponse> tratarNegocio(NegocioException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErroResponse(ex.getMessage()));
    }

    /** Trata falhas de validação de campos (anotações {@code @Valid} nos DTOs), retornando a primeira mensagem encontrada. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse("Dados inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroResponse(mensagem));
    }

    /**
     * Captura qualquer exceção não tratada explicitamente, registra o erro
     * completo no log do servidor (essencial para debug) e retorna uma
     * mensagem genérica ao usuário, sem expor detalhes internos do sistema.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarGenerico(Exception ex) {
        // Loga o erro real no console - sem isso, todo erro inesperado vira
        // uma mensagem genérica pro usuário e some do console.
        log.error("Erro nao tratado: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResponse("Erro interno no servidor. Tente novamente."));
    }
}
