package es.companeros.service;

import es.companeros.model.House;
import es.companeros.model.User;
import es.companeros.repository.ExpenseRepository;
import es.companeros.repository.HouseRepository;
import es.companeros.repository.NotificationRepository;
import es.companeros.repository.ShoppingListItemRepository;
import es.companeros.repository.TaskRepository;
import es.companeros.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HouseService {

    private final HouseRepository houseRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ShoppingListItemRepository shoppingRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public HouseService(HouseRepository houseRepository,
                        UserRepository userRepository,
                        TaskRepository taskRepository,
                        ShoppingListItemRepository shoppingRepository,
                        ExpenseRepository expenseRepository,
                        NotificationRepository notificationRepository) {
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.shoppingRepository = shoppingRepository;
        this.expenseRepository = expenseRepository;
        this.notificationRepository = notificationRepository;
    }

    public House saveHouse(House house) {
        return houseRepository.save(house);
    }

    public Optional<House> findByInvitationCode(String invitationCode) {
        return houseRepository.findByInvitationCode(invitationCode);
    }

    public House save(House house) {
        return houseRepository.save(house);
    }

    public House regenerateInvitationCode(House house) {
        house.setInvitationCode(java.util.UUID.randomUUID().toString().substring(0, 8));
        return houseRepository.save(house);
    }

    @Transactional
    public void deleteHouse(House house) {
        List<User> members = userRepository.findByHouse(house);
        for (User m : members) {
            m.setHouse(null);
            m.setRole(null);
        }
        userRepository.saveAll(members);

        taskRepository.deleteByHouse(house);
        shoppingRepository.deleteByHouse(house);
        expenseRepository.deleteByHouse(house);
        notificationRepository.deleteByHouseId(house.getId());

        houseRepository.delete(house);
    }
}
