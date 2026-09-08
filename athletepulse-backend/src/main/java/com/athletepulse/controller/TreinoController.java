package com.athletepulse.controller;

import com.athletepulse.dto.AtribuirTreinoRequest;
import com.athletepulse.dto.TreinoAtribuidoResponse;
import com.athletepulse.dto.TreinoRequest;
import com.athletepulse.dto.TreinoResponse;
import com.athletepulse.service.TreinoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de treinos: catálogo e atribuição diária aos atletas.
 * <p>
 * Rotas sob {@code /catalogo} e {@code /atribuir} e {@code /hoje} são de uso
 * exclusivo da comissão técnica; {@code /meu} e {@code /meu/historico} são
 * de uso exclusivo do atleta.
 */
@RestController
@RequestMapping("/api/treinos")
public class TreinoController {

    private final TreinoService treinoService;

    public TreinoController(TreinoService treinoService) {
        this.treinoService = treinoService;
    }

    /**
     * Cadastra um novo treino no catálogo.
     *
     * @return 201 Created com o treino criado
     */
    @PostMapping("/catalogo")
    public ResponseEntity<TreinoResponse> criarTreino(@Valid @RequestBody TreinoRequest req, Authentication auth) {
        TreinoResponse resposta = treinoService.criarTreino(auth.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    /** Lista todo o catálogo de treinos. */
    @GetMapping("/catalogo")
    public ResponseEntity<List<TreinoResponse>> listarCatalogo(Authentication auth) {
        return ResponseEntity.ok(treinoService.listarCatalogo(auth.getName()));
    }

    /**
     * Edita um treino existente do catálogo.
     *
     * @return 200 OK com o treino atualizado
     */
    @PutMapping("/catalogo/{treinoId}")
    public ResponseEntity<TreinoResponse> editarTreino(
            @PathVariable Long treinoId, @Valid @RequestBody TreinoRequest req, Authentication auth
    ) {
        return ResponseEntity.ok(treinoService.editarTreino(auth.getName(), treinoId, req));
    }

    /**
     * Remove um treino do catálogo.
     *
     * @return 204 No Content em caso de sucesso; 409 se o treino já tiver sido atribuído a algum atleta
     */
    @DeleteMapping("/catalogo/{treinoId}")
    public ResponseEntity<Void> excluirTreino(@PathVariable Long treinoId, Authentication auth) {
        treinoService.excluirTreino(auth.getName(), treinoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Atribui um treino do catálogo a um atleta, para hoje. Se já existir
     * uma atribuição para esse atleta hoje, ela é substituída.
     *
     * @return 200 OK com a atribuição criada/atualizada
     */
    @PostMapping("/atribuir")
    public ResponseEntity<TreinoAtribuidoResponse> atribuir(@Valid @RequestBody AtribuirTreinoRequest req, Authentication auth) {
        return ResponseEntity.ok(treinoService.atribuir(auth.getName(), req));
    }

    /** Lista, para cada atleta, o treino atribuído hoje (visão geral da comissão). */
    @GetMapping("/hoje")
    public ResponseEntity<List<TreinoAtribuidoResponse>> listarAtribuicoesDeHoje(Authentication auth) {
        return ResponseEntity.ok(treinoService.listarAtribuicoesDeHoje(auth.getName()));
    }

    /** Retorna o treino atribuído hoje ao atleta autenticado, ou 204 se ainda não houver nenhum. */
    @GetMapping("/meu")
    public ResponseEntity<TreinoAtribuidoResponse> meuTreinoDeHoje(Authentication auth) {
        return treinoService.meuTreinoDeHoje(auth.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** Lista o histórico de treinos atribuídos ao atleta autenticado. */
    @GetMapping("/meu/historico")
    public ResponseEntity<List<TreinoAtribuidoResponse>> meuHistorico(Authentication auth) {
        return ResponseEntity.ok(treinoService.meuHistorico(auth.getName()));
    }
}
