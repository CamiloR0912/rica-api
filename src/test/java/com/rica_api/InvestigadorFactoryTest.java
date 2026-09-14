package com.rica_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.rica.ricaapi.investigadores.CorreoDuplicadoException;
import com.rica.ricaapi.investigadores.Investigador;
import com.rica.ricaapi.investigadores.InvestigadorFactory;
import com.rica.ricaapi.investigadores.InvestigadorRegistrado;
import com.rica.ricaapi.investigadores.InvestigadorRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorFactoryTest {

    @Mock
    private InvestigadorRepository investigadorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InvestigadorFactory investigadorFactory;

    @Test
    void crearConstruyeInvestigadorValidoYPublicaEvento() {
        when(investigadorRepository.existsByCorreoInstitucional_Valor("ana.torres@uptc.edu.co")).thenReturn(false);

        Investigador investigador = investigadorFactory.crear("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(investigador.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(investigador.getCorreoInstitucional().valor()).isEqualTo("ana.torres@uptc.edu.co");
        assertThat(investigador.getGrupoInvestigacion()).isEqualTo("GIT-UPTC");
        verify(eventPublisher).publishEvent(any(InvestigadorRegistrado.class));
    }

    @Test
    void crearLanzaCorreoDuplicadoExceptionCuandoYaExiste() {
        when(investigadorRepository.existsByCorreoInstitucional_Valor("ana.torres@uptc.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> investigadorFactory.crear("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessageContaining("ana.torres@uptc.edu.co");
    }

    @Test
    void crearLanzaIllegalArgumentExceptionCuandoCorreoEsInvalido() {
        assertThatThrownBy(() -> investigadorFactory.crear("Ana Torres", "ana.torres@gmail.com", "GIT-UPTC"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
