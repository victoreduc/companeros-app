package es.companeros.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la página de login.
 */
@Controller
public class LoginController {

    /**
     * Muestra la página de login.
     *
     * @return El nombre de la vista de login.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
