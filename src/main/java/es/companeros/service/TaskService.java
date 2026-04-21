package es.companeros.service;

import es.companeros.model.House;
import es.companeros.model.Task;
import es.companeros.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de tareas.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Obtiene todas las tareas activas (no archivadas).
     * @return Lista de tareas activas.
     */
    public List<Task> findActiveTasks(House house) {
        return taskRepository.findByHouseAndArchivedFalseOrderByIdDesc(house);
    }

    public List<Task> findArchivedTasks(House house) {
        return taskRepository.findByHouseAndArchivedTrueOrderByIdDesc(house);
    }

    /**
     * Busca una tarea por su ID.
     * @param id El ID de la tarea.
     * @return Un Optional con la tarea si existe.
     */
    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Guarda o actualiza una tarea.
     * @param task La tarea a guardar.
     * @return La tarea guardada.
     */
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    /**
     * Elimina una tarea por su ID.
     * @param id El ID de la tarea a eliminar.
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public long countAllPendingTasks(House house) {
        return taskRepository.countByHouseAndCompletedFalseAndArchivedFalse(house);
    }
    public long countAllCompletedTasks(House house) {
        return taskRepository.countByHouseAndCompletedTrue(house);
    }

    public long countAllOverdueTasks(House house) {
        return taskRepository.countByHouseAndDueDateBeforeAndCompletedFalseAndArchivedFalse(house, LocalDate.now());
    }
}
