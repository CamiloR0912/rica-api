package com.rica_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rica.ricaapi.compartido.RecursoNoEncontradoException;
import com.rica.ricaapi.investigadores.CorreoInstitucional;
import com.rica.ricaapi.investigadores.Investigador;
import com.rica.ricaapi.investigadores.InvestigadorRepository;
import com.rica.ricaapi.publicaciones.LimiteAnualExcedidoException;
import com.rica.ricaapi.publicaciones.LimitePublicacionesAnualesService;
import com.rica.ricaapi.publicaciones.Publicacion;
import com.rica.ricaapi.publicaciones.PublicacionRepository;
import com.rica.ricaapi.publicaciones.PublicacionService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private InvestigadorRepository investigadorRepository;

    @Mock
    private LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    @InjectMocks
    private PublicacionService publicacionService;

    @Test
    void registrarLanzaExcepcionSiInvestigadorNoExiste() {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("noexiste@uptc.edu.co");

        when(investigadorRepository.findByCorreoInstitucional_Valor("noexiste@uptc.edu.co"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicacionService.registrar(publicacion))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("noexiste@uptc.edu.co");
    }

    @Test
    void registrarLanzaLimiteAnualExcedidoExceptionSiSuperaLimite() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setAnio(2026);

        when(investigadorRepository.findByCorreoInstitucional_Valor("ana.torres@uptc.edu.co"))
                .thenReturn(Optional.of(investigador));
        when(limitePublicacionesAnualesService.puedeRegistrar(investigador, publicacion))
                .thenReturn(false);

        assertThatThrownBy(() -> publicacionService.registrar(publicacion))
                .isInstanceOf(LimiteAnualExcedidoException.class);
    }

    @Test
    void registrarGuardaPublicacionCuandoDatosSonValidos() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setAnio(2026);
        publicacion.setTitulo("Artículo DDD");

        when(investigadorRepository.findByCorreoInstitucional_Valor("ana.torres@uptc.edu.co"))
                .thenReturn(Optional.of(investigador));
        when(limitePublicacionesAnualesService.puedeRegistrar(investigador, publicacion))
                .thenReturn(true);
        when(publicacionRepository.save(publicacion)).thenReturn(publicacion);

        Publicacion guardada = publicacionService.registrar(publicacion);

        assertThat(guardada.getTitulo()).isEqualTo("Artículo DDD");
        verify(publicacionRepository).save(publicacion);
    }

}
