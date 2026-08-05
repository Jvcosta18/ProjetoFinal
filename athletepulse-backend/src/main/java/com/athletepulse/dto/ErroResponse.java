package com.athletepulse.dto;

// O front (cadastro.js / login.js) lê "erro?.mensagem" - manter esse nome de campo.
public record ErroResponse(String mensagem) {}
