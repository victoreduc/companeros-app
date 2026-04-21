package es.companeros.service;

import es.companeros.model.*;
import es.companeros.repository.ExpenseAttachmentRepository;
import es.companeros.repository.ExpenseRepository;
import es.companeros.repository.ExpenseShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseAttachmentRepository attachmentRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseShareRepository expenseShareRepository,
                          ExpenseAttachmentRepository attachmentRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public List<Expense> findByHouseAndStatus(House house, ExpenseStatus status) {
        return expenseRepository.findByHouseAndStatusOrderByDateDescIdDesc(house, status);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense create(String title, String description, BigDecimal totalAmount,
                          String observations, Long createdByUserId, LocalDate date,
                          House house, List<Long> userIds, String splitMode,
                          Map<Long, BigDecimal> customAmounts,
                          List<MultipartFile> files) throws IOException {
        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setDescription(description);
        expense.setTotalAmount(totalAmount);
        expense.setObservations(observations);
        expense.setCreatedByUserId(createdByUserId);
        expense.setDate(date != null ? date : LocalDate.now());
        expense.setHouse(house);
        expense.setStatus(ExpenseStatus.PENDING);

        if (userIds != null && !userIds.isEmpty()) {
            BigDecimal proportional = totalAmount.divide(
                    BigDecimal.valueOf(userIds.size()), 2, RoundingMode.HALF_UP);
            for (Long uid : userIds) {
                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUserId(uid);
                if ("CUSTOM".equals(splitMode) && customAmounts.containsKey(uid)) {
                    share.setAmountOwed(customAmounts.get(uid));
                } else {
                    share.setAmountOwed(proportional);
                }
                share.setPaid(false);
                expense.getShares().add(share);
            }
        }

        Expense saved = expenseRepository.save(expense);

        if (files != null) {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            int count = 0;
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty() || count >= 3) continue;
                String ct = file.getContentType();
                if (ct == null || !ct.startsWith("image/")) continue;

                String orig = file.getOriginalFilename();
                String stored = UUID.randomUUID().toString() + ".jpg";
                Thumbnails.of(file.getInputStream())
                        .size(1600, 1600)
                        .keepAspectRatio(true)
                        .outputFormat("jpg")
                        .outputQuality(0.80)
                        .toFile(dir.resolve(stored).toFile());

                ExpenseAttachment att = new ExpenseAttachment();
                att.setExpense(saved);
                att.setStoredFileName(stored);
                att.setOriginalFileName(orig);
                att.setContentType("image/jpeg");
                attachmentRepository.save(att);
                count++;
            }
        }

        return saved;
    }

    public Optional<ExpenseAttachment> findAttachmentById(Long id) {
        return attachmentRepository.findById(id);
    }

    public Path getAttachmentPath(String storedFileName) {
        return Paths.get(uploadDir).resolve(storedFileName);
    }

    public void toggleSharePaid(Long shareId) {
        expenseShareRepository.findById(shareId).ifPresent(share -> {
            share.setPaid(!share.isPaid());
            expenseShareRepository.save(share);

            Expense expense = share.getExpense();
            boolean allPaid = expense.getShares().stream().allMatch(ExpenseShare::isPaid);
            expense.setStatus(allPaid ? ExpenseStatus.PAID : ExpenseStatus.PENDING);
            expenseRepository.save(expense);
        });
    }

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }
}
