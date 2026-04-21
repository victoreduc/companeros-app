package es.companeros.controller;

import es.companeros.model.User;
import es.companeros.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class SettingsController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SettingsController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/settings")
    public String settings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", user);
        }
        model.addAttribute("activePage", "settings");
        return "settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String name,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("successMessage", "El nombre no puede estar vacío.");
            return "redirect:/settings";
        }
        if (userDetails != null) {
            userRepository.findByUsername(userDetails.getUsername()).ifPresent(user -> {
                user.setName(name.trim());
                user.setGender(gender != null && gender.isBlank() ? null : gender);
                user.setBirthDate(birthDate);
                userRepository.save(user);
            });
            redirectAttributes.addFlashAttribute("successMessage", "Tus cambios han sido guardados correctamente.");
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "La contraseña actual no es correcta.");
            return "redirect:/settings";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "La nueva contraseña y la confirmación no coinciden.");
            return "redirect:/settings";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "La nueva contraseña debe tener al menos 6 caracteres.");
            return "redirect:/settings";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("passwordSuccess", "Contraseña cambiada correctamente.");
        return "redirect:/settings";
    }
}
