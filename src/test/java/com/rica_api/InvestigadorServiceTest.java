package com.rica_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rica.ricaapi.compartido.RecursoNoEncontradoException;
import com.rica.ricaapi.investigadores.CorreoDuplicadoException;
import com.rica.ricaapi.investigadores.CorreoInstitucional;
import com.rica.ricaapi.investigadores.Investigador;
import com.rica.ricaapi.investigadores.InvestigadorFactory;
import com.rica.ricaapi.investigadores.InvestigadorRepository;
import com.rica.ricaapi.investigadores.InvestigadorService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorServiceTest {

    @Mock
    private InvestigadorRepository investigadorRepository;

    @Mock
    private InvestigadorFactory investigadorFactory;

    @InjectMocks
    private InvestigadorService investigadorService;

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        when(investigadorRepository.findById(1L)).thenReturn(Optional.of(investigador));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(investigadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigadorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        when(investigadorFactory.crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC"))
                .thenThrow(new CorreoDuplicadoException("Ya existe un investigador registrado con el correo carlos.ruiz@uptc.edu.co"));

        assertThatThrownBy(() -> investigadorService.registrar("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(investigadorFactory).crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC");
    }

    @Test
    void registrarGuardaInvestigadorExitosamente() {
        Investigador nuevo = new Investigador(null, "Carlos Ruiz", new CorreoInstitucional("carlos.ruiz@uptc.edu.co"), "GIT-UPTC");
        Investigador guardado = new Investigador(1L, "Carlos Ruiz", new CorreoInstitucional("carlos.ruiz@uptc.edu.co"), "GIT-UPTC");

        when(investigadorFactory.crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC")).thenReturn(nuevo);
        when(investigadorRepository.save(nuevo)).thenReturn(guardado);

        Investigador resultado = investigadorService.registrar("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC");

        assertThat(resultado.getId()).isEqualTo(1L);
        verify(investigadorFactory).crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC");
        verify(investigadorRepository).save(nuevo);
    }

}
