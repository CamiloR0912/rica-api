package com.rica.ricaapi.investigadores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InvestigadorEventListener {

    private static final Logger log = LoggerFactory.getLogger(InvestigadorEventListener.class);

    @EventListener
    public void manejarInvestigadorRegistrado(InvestigadorRegistrado evento) {
        log.info("Evento de dominio recibido: Investigador registrado con correo {} en {}",
                evento.correoInstitucional(), evento.ocurridoEn());
    }

}
