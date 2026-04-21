package es.companeros.repository;

import es.companeros.model.House;
import es.companeros.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Task.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByHouseAndArchivedFalseOrderByIdDesc(House house);

    List<Task> findByHouseAndArchivedTrueOrderByIdDesc(House house);

    @Transactional
    void deleteByHouse(House house);

    long countByHouseAndAssignedUserIdAndCompletedFalseAndArchivedFalse(House house, Long userId);

    long countByHouseAndCompletedFalseAndArchivedFalse(House house);

    long countByHouseAndCompletedTrue(House house);

    long countByHouseAndDueDateBeforeAndCompletedFalseAndArchivedFalse(House house, LocalDate date);
}
