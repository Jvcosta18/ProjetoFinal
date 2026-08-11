package com.athletepulse.service;

import com.athletepulse.dto.AtletaEmocionalResponse;
import com.athletepulse.dto.NotaRequest;
import com.athletepulse.dto.NotaResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.CheckIn;
import com.athletepulse.model.NotaPsicologica;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.CheckInRepository;
import com.athletepulse.repository.NotaPsicologicaRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PsicologoService {

    private final UsuarioRepository usuarioRepository;
    private final CheckInRepository checkInRepository;
    private final NotaPsicologicaRepository notaRepository;

    public PsicologoService(
            UsuarioRepository usuarioRepository,
            CheckInRepository checkInRepository,
            NotaPsicologicaRepository notaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.checkInRepository = checkInRepository;
        this.notaRepository = notaRepository;
    }

    public List<AtletaEmocionalResponse> listarAtletas(String emailPsicologo) {
        exigirPsicologo(emailPsicologo);

        List<Usuario> atletas = usuarioRepository.findByTipoOrderByNomeAsc(TipoUsuario.JOGADOR);

        return atletas.stream()
                .map(atleta -> {
                    CheckIn ultimo = checkInRepository
                            .findFirstByAtleta_IdOrderByDataCheckinDesc(atleta.getId())
                            .orElse(null);

                    return new AtletaEmocionalResponse(
                            atleta.getId(),
                            atleta.getNome(),
                            atleta.getEmail(),
                            calcularStatusEmocional(ultimo),
                            ultimo != null ? ultimo.getEstadoEmocional() : null,
                            ultimo != null ? ultimo.getDataCheckin().toString() : null
                    );
                })
                .toList();
    }

    public List<NotaResponse> listarNotas(String emailPsicologo, Long atletaId) {
        exigirPsicologo(emailPsicologo);
        buscarAtletaPorId(atletaId);

        return notaRepository.findByAtleta_IdOrderByCriadoEmDesc(atletaId)
                .stream()
                .map(n -> new NotaResponse(n.getId(), n.getTexto(), n.getPsicologo().getNome(), n.getCriadoEm()))
                .toList();
    }

    @Transactional
    public NotaResponse criarNota(String emailPsicologo, Long atletaId, NotaRequest req) {
        Usuario psicologo = exigirPsicologo(emailPsicologo);
        Usuario atleta = buscarAtletaPorId(atletaId);

        NotaPsicologica nota = new NotaPsicologica();
        nota.setAtleta(atleta);
        nota.setPsicologo(psicologo);
        nota.setTexto(req.texto().trim());

        notaRepository.save(nota);
        return new NotaResponse(nota.getId(), nota.getTexto(), psicologo.getNome(), nota.getCriadoEm());
    }

    private String calcularStatusEmocional(CheckIn ultimo) {
        if (ultimo == null || !ultimo.getDataCheckin().isEqual(LocalDate.now())) {
            return "sem_dado";
        }

        int estado = ultimo.getEstadoEmocional();
        if (estado <= 2) return "alerta";
        if (estado == 3) return "atencao";
        return "ok";
    }

    private Usuario exigirPsicologo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (usuario.getTipo() != TipoUsuario.PSICOLOGO) {
            throw new NegocioException("Apenas a psicologia pode acessar essa área.", HttpStatus.FORBIDDEN);
        }

        return usuario;
    }

    private Usuario buscarAtletaPorId(Long id) {
        Usuario atleta = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Atleta não encontrado.", HttpStatus.NOT_FOUND));

        if (atleta.getTipo() != TipoUsuario.JOGADOR) {
            throw new NegocioException("Usuário informado não é um atleta.", HttpStatus.BAD_REQUEST);
        }

        return atleta;
    }
}
