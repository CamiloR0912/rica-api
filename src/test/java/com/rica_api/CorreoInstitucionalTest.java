package com.rica_api;

import org.junit.jupiter.api.Test;

import com.rica.ricaapi.investigadores.CorreoInstitucional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorreoInstitucionalTest {

    @Test
    void creaCorreoInstitucionalValido() {
        CorreoInstitucional correo = new CorreoInstitucional("carlos.ruiz@uptc.edu.co");
        assertThat(correo.valor()).isEqualTo("carlos.ruiz@uptc.edu.co");
    }

    @Test
    void rechazaCorreoConDominioDiferente() {
        assertThatThrownBy(() -> new CorreoInstitucional("carlos@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo institucional debe pertenecer al dominio @uptc.edu.co");
    }

    @Test
    void rechazaCorreoNulo() {
        assertThatThrownBy(() -> new CorreoInstitucional(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo institucional debe pertenecer al dominio @uptc.edu.co");
    }

}
