package com.rica.ricaapi.investigadores;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class InvestigadorFactory {

    private final InvestigadorRepository investigadorRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InvestigadorFactory(InvestigadorRepository investigadorRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.investigadorRepository = investigadorRepository;
        this.eventPublisher = eventPublisher;
    }

    public InvestigadorFactory(InvestigadorRepository investigadorRepository) {
        this(investigadorRepository, null);
    }

    public Investigador crear(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        CorreoInstitucional correo = new CorreoInstitucional(correoInstitucional);

        if (investigadorRepository.existsByCorreoInstitucional_Valor(correo.valor())) {
            throw new CorreoDuplicadoException(
                    "Ya existe un investigador registrado con el correo " + correo.valor());
        }

        Investigador nuevo = new Investigador(null, nombreCompleto, correo, grupoInvestigacion);
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new InvestigadorRegistrado(correo.valor(), Instant.now()));
        }
        return nuevo;
    }

}
