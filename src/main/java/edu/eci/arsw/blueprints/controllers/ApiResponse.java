package edu.eci.arsw.blueprints.controllers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de la API")
public record ApiResponse<T>(
        @Schema(description = "Código HTTP", example = "200") int code,
        @Schema(description = "Mensaje descriptivo", example = "execute ok") String message,
        @Schema(description = "Datos de la respuesta") T data
) {
    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
