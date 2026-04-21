package es.companeros.controller;

import es.companeros.model.House;
import es.companeros.model.ShoppingListItem;
import es.companeros.model.Task;
import es.companeros.model.User;
import es.companeros.repository.UserRepository;
import es.companeros.service.NotificationService;
import es.companeros.service.ShoppingListService;
import es.companeros.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final TaskService taskService;
    private final ShoppingListService shoppingListService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public DashboardController(TaskService taskService, ShoppingListService shoppingListService,
                                UserRepository userRepository, NotificationService notificationService) {
        this.taskService = taskService;
        this.shoppingListService = shoppingListService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    public String tasks(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "active") String tab,
                        Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (user.getHouse() == null) {
            return "redirect:/join-house";
        }
        House house = user.getHouse();

        model.addAttribute("tasks", taskService.findActiveTasks(house));
        model.addAttribute("archivedTasks", taskService.findArchivedTasks(house));
        model.addAttribute("tab", tab);
        model.addAttribute("newTask", new Task());
        model.addAttribute("users", userRepository.findByHouse(house));
        model.addAttribute("currentUser", user);
        model.addAttribute("pendingTasksCount", taskService.countAllPendingTasks(house));
        model.addAttribute("completedTasksCount", taskService.countAllCompletedTasks(house));
        model.addAttribute("overdueTasksCount", taskService.countAllOverdueTasks(house));
        model.addAttribute("activePage", "tasks");
        return "tasks";
    }

    @GetMapping("/shopping")
    public String shopping(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (user.getHouse() == null) {
            return "redirect:/join-house";
        }
        House house = user.getHouse();
        if (!house.isShoppingEnabled()) return "redirect:/tasks";

        List<ShoppingListItem> items = shoppingListService.findAllItemsByHouse(house);
        model.addAttribute("shoppingListItems", items);
        model.addAttribute("newItem", new ShoppingListItem());
        model.addAttribute("users", userRepository.findByHouse(house));
        model.addAttribute("currentUser", user);

        long pendingItems = items.stream().filter(i -> !i.isPurchased()).count();
        long purchasedItems = items.stream().filter(i -> i.isPurchased()).count();
        model.addAttribute("pendingItemsCount", pendingItems);
        model.addAttribute("purchasedItemsCount", purchasedItems);
        model.addAttribute("totalItemsCount", items.size());
        model.addAttribute("activePage", "shopping");
        return "shopping";
    }

    @PostMapping("/tasks/add")
    public String addTask(@ModelAttribute Task task, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (task.getAssignedUserId() == null) {
            task.setAssignedUserId(user.getId());
        }
        task.setHouse(user.getHouse());
        taskService.saveTask(task);
        notificationService.notifyTaskAssigned(task.getAssignedUserId(), user.getId(),
                user.getHouse().getId(), task.getDescription());
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/toggle/{id}")
    public String toggleTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.findTaskById(id).ifPresent(task -> {
            if (belongsToUserHouse(task.getHouse(), user)) {
                task.setCompleted(!task.isCompleted());
                taskService.saveTask(task);
            }
        });
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/archive/{id}")
    public String archiveTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.findTaskById(id).ifPresent(task -> {
            if (belongsToUserHouse(task.getHouse(), user)) {
                task.setArchived(true);
                taskService.saveTask(task);
            }
        });
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/unarchive/{id}")
    public String unarchiveTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.findTaskById(id).ifPresent(task -> {
            if (belongsToUserHouse(task.getHouse(), user)) {
                task.setArchived(false);
                taskService.saveTask(task);
            }
        });
        return "redirect:/tasks?tab=archived";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.findTaskById(id).ifPresent(task -> {
            if (belongsToUserHouse(task.getHouse(), user)) {
                taskService.deleteTask(id);
            }
        });
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Task task = taskService.findTaskById(id).orElse(null);
        if (task == null || !belongsToUserHouse(task.getHouse(), user)) return "redirect:/tasks";
        model.addAttribute("task", task);
        model.addAttribute("users", user.getHouse() != null ? userRepository.findByHouse(user.getHouse()) : userRepository.findAll());
        model.addAttribute("currentUser", user);
        return "task_edit";
    }

    @PostMapping("/tasks/update/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        taskService.findTaskById(id).ifPresent(existingTask -> {
            if (belongsToUserHouse(existingTask.getHouse(), user)) {
                existingTask.setDescription(task.getDescription());
                existingTask.setPriority(task.getPriority());
                existingTask.setDueDate(task.getDueDate());
                existingTask.setAssignedUserId(task.getAssignedUserId());
                taskService.saveTask(existingTask);
            }
        });
        return "redirect:/tasks";
    }

    @PostMapping("/shopping/add")
    public String addShoppingItem(@ModelAttribute ShoppingListItem item, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        item.setAddedByUserId(user.getId());
        item.setHouse(user.getHouse());
        shoppingListService.saveItem(item);
        notificationService.notifyShoppingAdded(user.getHouse(), user.getId());
        return "redirect:/shopping";
    }

    @GetMapping("/shopping/toggle/{id}")
    public String toggleShoppingItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        shoppingListService.findItemById(id).ifPresent(item -> {
            if (belongsToUserHouse(item.getHouse(), user)) {
                item.setPurchased(!item.isPurchased());
                shoppingListService.saveItem(item);
            }
        });
        return "redirect:/shopping";
    }

    @GetMapping("/shopping/delete/{id}")
    public String deleteShoppingItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        shoppingListService.findItemById(id).ifPresent(item -> {
            if (belongsToUserHouse(item.getHouse(), user)) {
                shoppingListService.deleteItem(id);
            }
        });
        return "redirect:/shopping";
    }

    @PostMapping("/shopping/clear")
    public String clearShoppingList(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (user.getHouse() != null) {
            shoppingListService.clearByHouse(user.getHouse());
        }
        return "redirect:/shopping";
    }

    private boolean belongsToUserHouse(House entityHouse, User user) {
        return entityHouse != null && user.getHouse() != null
                && entityHouse.getId().equals(user.getHouse().getId());
    }
}
