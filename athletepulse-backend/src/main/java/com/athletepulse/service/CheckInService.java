package com.athletepulse.service;

import com.athletepulse.dto.CheckInRequest;
import com.athletepulse.dto.CheckInResponse;
import com.athletepulse.exception.NegocioException;
import com.athletepulse.model.CheckIn;
import com.athletepulse.model.TipoUsuario;
import com.athletepulse.model.Usuario;
import com.athletepulse.repository.CheckInRepository;
import com.athletepulse.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UsuarioRepository usuarioRepository;

    public CheckInService(CheckInRepository checkInRepository, UsuarioRepository usuarioRepository) {
        this.checkInRepository = checkInRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public CheckInResponse registrar(String emailAutenticado, CheckInRequest req) {
        Usuario atleta = buscarAtleta(emailAutenticado);

        if (checkInRepository.existsByAtleta_IdAndDataCheckin(atleta.getId(), LocalDate.now())) {
            throw new NegocioException("Você já enviou o check-in de hoje.", HttpStatus.CONFLICT);
        }

        if (Boolean.TRUE.equals(req.temDor()) && (req.intensidadeDor() == null)) {
            throw new NegocioException("Informe a intensidade da dor.", HttpStatus.BAD_REQUEST);
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setAtleta(atleta);
        checkIn.setHorasSono(req.horasSono());
        checkIn.setFadiga(req.fadiga());
        checkIn.setEstadoEmocional(req.estadoEmocional());
        checkIn.setTemDor(Boolean.TRUE.equals(req.temDor()));
        checkIn.setLocalDor(checkIn.isTemDor() ? req.localDor() : null);
        checkIn.setIntensidadeDor(checkIn.isTemDor() ? req.intensidadeDor() : null);
        checkIn.setObservacoes(req.observacoes());

        checkInRepository.save(checkIn);
        return paraResponse(checkIn);
    }

    public List<CheckInResponse> listarMeus(String emailAutenticado) {
        Usuario atleta = buscarAtleta(emailAutenticado);
        return checkInRepository.findByAtleta_IdOrderByDataCheckinDesc(atleta.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    private Usuario buscarAtleta(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado.", HttpStatus.UNAUTHORIZED));

        if (usuario.getTipo() != TipoUsuario.JOGADOR) {
            throw new NegocioException("Apenas atletas podem enviar check-in.", HttpStatus.FORBIDDEN);
        }

        return usuario;
    }

    private CheckInResponse paraResponse(CheckIn c) {
        return new CheckInResponse(
                c.getId(),
                c.getDataCheckin(),
                c.getHorasSono(),
                c.getFadiga(),
                c.getEstadoEmocional(),
                c.isTemDor(),
                c.getLocalDor(),
                c.getIntensidadeDor(),
                c.getObservacoes(),
                c.getCriadoEm()
        );
    }
}
