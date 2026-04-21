package es.companeros.repository;

import es.companeros.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
}
