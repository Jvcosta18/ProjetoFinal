package com.athletepulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe de inicialização da API do AthletePulse.
 * <p>
 * Sistema de controle de performance e saúde de atletas de futebol, com
 * três perfis de acesso ({@link com.athletepulse.model.TipoUsuario}):
 * atleta, comissão técnica e psicólogo. Fornece autenticação via JWT,
 * check-ins diários dos atletas, notas de acompanhamento psicológico e
 * mensagens entre atletas e a equipe.
 */
@SpringBootApplication
public class AthletePulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AthletePulseApplication.class, args);
    }
}
