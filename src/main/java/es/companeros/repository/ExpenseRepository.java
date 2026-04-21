package es.companeros.repository;

import es.companeros.model.Expense;
import es.companeros.model.ExpenseStatus;
import es.companeros.model.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByHouseAndStatusOrderByDateDescIdDesc(House house, ExpenseStatus status);

    @Transactional
    void deleteByHouse(House house);
}
