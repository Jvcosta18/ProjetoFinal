package com.athletepulse.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção usada para sinalizar violações de regra de negócio (email duplicado,
 * credenciais inválidas, acesso não permitido para o perfil, etc.), já
 * carregando o status HTTP apropriado para a resposta.
 * <p>
 * Tratada centralmente por {@link GlobalExceptionHandler}, que converte a
 * mensagem em um {@link com.athletepulse.dto.ErroResponse}.
 */
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    /**
     * @param mensagem mensagem de erro, em português, para exibição ao usuário
     * @param status   status HTTP a ser retornado na resposta (ex: 403, 404, 409)
     */
    public NegocioException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }

    /** @return o status HTTP associado a esta exceção */
    public HttpStatus getStatus() {
        return status;
    }
}
