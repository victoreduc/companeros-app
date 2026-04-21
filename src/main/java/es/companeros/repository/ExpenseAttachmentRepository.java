package es.companeros.repository;

import es.companeros.model.ExpenseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseAttachmentRepository extends JpaRepository<ExpenseAttachment, Long> {
    List<ExpenseAttachment> findByExpenseId(Long expenseId);
}
