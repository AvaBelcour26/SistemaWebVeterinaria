package com.sistemaVeterinario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

@Controller
@Tag(name = "Language Controller", description = "Controlador para el manejo de cambio de idioma en la aplicación")
public class LanguageController {

    @GetMapping("/changeLanguage")
    @Operation(
            summary = "Cambiar idioma de la aplicación",
            description = "Cambia el idioma de la interfaz de usuario y redirige a la página anterior o a la página principal si no hay página de referencia"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Idioma cambiado exitosamente, redirige a la página anterior o a la página principal",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetro de idioma inválido o faltante",
                    content = @Content(mediaType = "text/html")
            )
    })
    public String changeLanguage(
            @Parameter(
                    description = "Código del idioma a establecer (ej: 'es' para español, 'en' para inglés)",
                    required = true,
                    example = "es"
            )
            @RequestParam("lang") String lang,

            @Parameter(description = "Objeto HttpServletRequest para obtener información de la petición", hidden = true)
            HttpServletRequest request,

            @Parameter(description = "Objeto HttpServletResponse para configurar la respuesta", hidden = true)
            HttpServletResponse response) {

        // Obtener el LocaleResolver y cambiar el idioma
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, new Locale(lang));
        }

        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null) ? referer : "/";

        return "redirect:" + redirectUrl;
    }
}