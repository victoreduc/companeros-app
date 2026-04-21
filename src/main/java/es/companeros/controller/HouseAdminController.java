package es.companeros.controller;

import es.companeros.model.NotificationType;
import es.companeros.model.Role;
import es.companeros.model.User;
import es.companeros.repository.UserRepository;
import es.companeros.service.HouseService;
import es.companeros.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HouseAdminController {

    private final HouseService houseService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public HouseAdminController(HouseService houseService, UserRepository userRepository,
                                 NotificationService notificationService) {
        this.houseService = houseService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getAdminOrNull(UserDetails userDetails) {
        if (userDetails == null) return null;
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null || user.getHouse() == null || user.getRole() != Role.HOUSE_ADMIN) return null;
        return user;
    }

    @GetMapping("/house")
    public String housePage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null || user.getHouse() == null) return "redirect:/join-house";
        if (user.getRole() == Role.HOUSE_ADMIN) return "redirect:/house/admin";

        model.addAttribute("currentUser", user);
        model.addAttribute("house", user.getHouse());
        model.addAttribute("members", userRepository.findByHouse(user.getHouse()));
        return "house-member";
    }

    @PostMapping("/house/leave")
    public String leaveHouse(@AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null || user.getHouse() == null) return "redirect:/tasks";

        if (user.getRole() == Role.HOUSE_ADMIN) {
            long adminCount = userRepository.findByHouse(user.getHouse()).stream()
                    .filter(m -> m.getRole() == Role.HOUSE_ADMIN)
                    .count();
            if (adminCount <= 1) {
                redirectAttributes.addFlashAttribute("error", "Eres el único administrador. Nombra otro administrador antes de abandonar la casa.");
                return "redirect:/house/admin";
            }
        }

        es.companeros.model.House house = user.getHouse();
        String userName = user.getName();
        user.setHouse(null);
        user.setRole(null);
        userRepository.save(user);
        notificationService.notifyHouse(house, user.getId(),
                userName + " ha abandonado la casa.", NotificationType.HOUSE_LEAVE);
        return "redirect:/join-house";
    }

    @GetMapping("/house/admin")
    public String adminPanel(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        model.addAttribute("currentUser", admin);
        model.addAttribute("house", admin.getHouse());
        model.addAttribute("members", userRepository.findByHouse(admin.getHouse()));
        model.addAttribute("activePage", "admin");
        return "house-admin";
    }

    @PostMapping("/house/admin/remove/{userId}")
    public String removeMember(@PathVariable Long userId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        if (userId.equals(admin.getId())) {
            redirectAttributes.addFlashAttribute("error", "No puedes expulsarte a ti mismo.");
            return "redirect:/house/admin";
        }

        userRepository.findById(userId).ifPresent(member -> {
            if (admin.getHouse().getId().equals(member.getHouse() != null ? member.getHouse().getId() : null)) {
                es.companeros.model.House house = member.getHouse();
                String memberName = member.getName();
                member.setHouse(null);
                member.setRole(null);
                userRepository.save(member);
                notificationService.notifyHouse(house, member.getId(),
                        memberName + " ha sido expulsado de la casa.", NotificationType.HOUSE_LEAVE);
            }
        });

        redirectAttributes.addFlashAttribute("success", "Miembro eliminado de la casa.");
        return "redirect:/house/admin";
    }

    @PostMapping("/house/admin/update")
    public String updateHouse(@RequestParam String houseName,
                              @RequestParam(required = false) String houseDescription,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        if (houseName == null || houseName.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de la casa no puede estar vacío.");
            return "redirect:/house/admin";
        }

        admin.getHouse().setName(houseName.trim());
        admin.getHouse().setDescription(houseDescription != null ? houseDescription.trim() : null);
        houseService.save(admin.getHouse());
        redirectAttributes.addFlashAttribute("success", "Información de la casa actualizada.");
        return "redirect:/house/admin";
    }

    @PostMapping("/house/admin/modules")
    public String updateModules(@RequestParam(defaultValue = "false") boolean shoppingEnabled,
                                @RequestParam(defaultValue = "false") boolean expensesEnabled,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        admin.getHouse().setShoppingEnabled(shoppingEnabled);
        admin.getHouse().setExpensesEnabled(expensesEnabled);
        houseService.save(admin.getHouse());
        redirectAttributes.addFlashAttribute("success", "Módulos actualizados correctamente.");
        return "redirect:/house/admin";
    }

    @PostMapping("/house/admin/promote/{userId}")
    public String promoteToAdmin(@PathVariable Long userId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        if (userId.equals(admin.getId())) {
            redirectAttributes.addFlashAttribute("error", "Ya eres administrador.");
            return "redirect:/house/admin";
        }

        userRepository.findById(userId).ifPresent(member -> {
            if (admin.getHouse().getId().equals(member.getHouse() != null ? member.getHouse().getId() : null)) {
                member.setRole(es.companeros.model.Role.HOUSE_ADMIN);
                userRepository.save(member);
            }
        });

        redirectAttributes.addFlashAttribute("success", "Miembro promocionado a administrador.");
        return "redirect:/house/admin";
    }

    @PostMapping("/house/admin/regenerate-code")
    public String regenerateCode(@AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        houseService.regenerateInvitationCode(admin.getHouse());
        redirectAttributes.addFlashAttribute("success", "Código de invitación regenerado correctamente.");
        return "redirect:/house/admin";
    }

    @PostMapping("/house/admin/delete")
    public String deleteHouse(@AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User admin = getAdminOrNull(userDetails);
        if (admin == null) return "redirect:/tasks";

        houseService.deleteHouse(admin.getHouse());
        return "redirect:/join-house";
    }
}
