package es.companeros.controller;

import es.companeros.model.*;
import es.companeros.repository.UserRepository;
import es.companeros.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@Controller
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    @Autowired
    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/expenses")
    public String expenses(@RequestParam(defaultValue = "pending") String tab,
                           Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/login";
        if (user.getHouse() == null) return "redirect:/join-house";

        House house = user.getHouse();
        if (!house.isExpensesEnabled()) return "redirect:/tasks";
        ExpenseStatus status = "paid".equals(tab) ? ExpenseStatus.PAID : ExpenseStatus.PENDING;
        List<Expense> expenses = expenseService.findByHouseAndStatus(house, status);
        List<User> members = userRepository.findByHouse(house);

        model.addAttribute("currentUser", user);
        model.addAttribute("expenses", expenses);
        model.addAttribute("members", members);
        model.addAttribute("tab", tab);
        return "expenses";
    }

    @PostMapping("/expenses/add")
    public String addExpense(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam BigDecimal totalAmount,
                             @RequestParam(required = false) String observations,
                             @RequestParam(required = false) List<Long> userIds,
                             @RequestParam(defaultValue = "PROPORTIONAL") String splitMode,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                             @RequestParam(value = "attachments", required = false) List<MultipartFile> files,
                             HttpServletRequest request,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null || user.getHouse() == null) return "redirect:/login";

        Map<Long, BigDecimal> customAmounts = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getKey().startsWith("customAmount_")) {
                try {
                    Long uid = Long.parseLong(entry.getKey().substring("customAmount_".length()));
                    BigDecimal amt = new BigDecimal(entry.getValue()[0]);
                    customAmounts.put(uid, amt);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (userIds == null || userIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar al menos una persona.");
            return "redirect:/expenses";
        }

        try {
            expenseService.create(title, description, totalAmount, observations,
                    user.getId(), date, user.getHouse(), userIds, splitMode, customAmounts, files);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir los archivos. El gasto no se ha guardado.");
            return "redirect:/expenses";
        }

        return "redirect:/expenses";
    }

    @GetMapping("/expenses/attachments/{filename:.+}")
    public ResponseEntity<Resource> serveAttachment(@PathVariable String filename,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        // Prevent path traversal: only allow simple filenames
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")
                || filename.startsWith(".")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = expenseService.getAttachmentPath(filename);

        Resource resource = new PathResource(filePath);
        if (!resource.exists()) return ResponseEntity.notFound().build();

        String contentType = "image/jpeg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) contentType = "image/png";
        else if (lower.endsWith(".gif")) contentType = "image/gif";
        else if (lower.endsWith(".webp")) contentType = "image/webp";
        else if (lower.endsWith(".heic") || lower.endsWith(".heif")) contentType = "image/heic";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(resource);
    }

    @PostMapping("/expenses/{expenseId}/shares/{shareId}/toggle")
    public String toggleShare(@PathVariable Long expenseId,
                              @PathVariable Long shareId,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/login";

        expenseService.findById(expenseId).ifPresent(expense -> {
            boolean isCreator = expense.getCreatedByUserId().equals(user.getId());
            boolean shareBelongsToExpense = expense.getShares().stream()
                    .anyMatch(s -> s.getId().equals(shareId));
            boolean sameHouse = user.getHouse() != null
                    && expense.getHouse().getId().equals(user.getHouse().getId());
            if (isCreator && shareBelongsToExpense && sameHouse) {
                expenseService.toggleSharePaid(shareId);
            }
        });

        String tab = expenseService.findById(expenseId)
                .map(e -> e.getStatus() == ExpenseStatus.PAID ? "paid" : "pending")
                .orElse("pending");
        return "redirect:/expenses?tab=" + tab;
    }

    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user == null || user.getHouse() == null) return "redirect:/login";

        expenseService.findById(id).ifPresent(expense -> {
            boolean isCreator = expense.getCreatedByUserId().equals(user.getId());
            boolean isAdmin = user.getRole() == Role.HOUSE_ADMIN;
            if ((isCreator || isAdmin) &&
                    expense.getHouse().getId().equals(user.getHouse().getId())) {
                expenseService.delete(id);
            }
        });

        return "redirect:/expenses";
    }
}
