package com.rica_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rica.ricaapi.investigadores.CorreoInstitucional;
import com.rica.ricaapi.investigadores.Investigador;
import com.rica.ricaapi.publicaciones.LimitePublicacionesAnualesService;
import com.rica.ricaapi.publicaciones.Publicacion;
import com.rica.ricaapi.publicaciones.PublicacionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimitePublicacionesAnualesServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @InjectMocks
    private LimitePublicacionesAnualesService limiteService;

    @Test
    void permiteRegistrarCuandoPublicacionesDelAnioSonMenoresA5() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setAnio(2026);

        when(publicacionRepository.countByInvestigadorCorreoAndAnio("ana.torres@uptc.edu.co", 2026))
                .thenReturn(4L);

        boolean puede = limiteService.puedeRegistrar(investigador, publicacion);

        assertThat(puede).isTrue();
    }

    @Test
    void rechazaRegistroCuandoPublicacionesDelAnioAlcanzanElLimite() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setAnio(2026);

        when(publicacionRepository.countByInvestigadorCorreoAndAnio("ana.torres@uptc.edu.co", 2026))
                .thenReturn(5L);

        boolean puede = limiteService.puedeRegistrar(investigador, publicacion);

        assertThat(puede).isFalse();
    }

}
