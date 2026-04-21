package es.companeros.controller;

import es.companeros.model.House;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class HouseController {

    private final HouseService houseService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public HouseController(HouseService houseService, UserRepository userRepository,
                            NotificationService notificationService) {
        this.houseService = houseService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/join-house")
    public String showJoinHouseForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null && user.getHouse() != null) {
                return "redirect:/tasks"; // Ya tiene casa
            }
            model.addAttribute("currentUser", user);
        }
        return "join-house";
    }

    @PostMapping("/house/create")
    public String createHouse(@RequestParam String name, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        House house = new House();
        house.setName(name);
        house = houseService.saveHouse(house);

        user.setHouse(house);
        user.setRole(Role.HOUSE_ADMIN);
        userRepository.save(user);

        return "redirect:/tasks";
    }

    @PostMapping("/house/join")
    public String joinHouse(@RequestParam String invitationCode, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Optional<House> houseOpt = houseService.findByInvitationCode(invitationCode);
        if (houseOpt.isPresent()) {
            House house = houseOpt.get();
            user.setHouse(house);
            user.setRole(Role.HOUSE_MEMBER);
            userRepository.save(user);
            notificationService.notifyHouse(house, user.getId(),
                    user.getName() + " se ha unido a la casa.", NotificationType.HOUSE_JOIN);
            return "redirect:/tasks";
        } else {
            model.addAttribute("error", "Código de invitación no válido.");
            model.addAttribute("currentUser", user);
            return "join-house";
        }
    }
}
