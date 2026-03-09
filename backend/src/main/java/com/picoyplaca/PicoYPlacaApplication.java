package com.picoyplaca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Pico y Placa API.
 * <p>
 * Esta aplicación provee un servicio REST para consultar
 * la restricción vehicular de Pico y Placa basándose en
 * el número de placa, fecha y hora proporcionados.
 */
@SpringBootApplication
public class PicoYPlacaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PicoYPlacaApplication.class, args);
    }
}
