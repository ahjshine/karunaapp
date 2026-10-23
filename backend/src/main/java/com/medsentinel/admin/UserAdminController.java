package com.medsentinel.admin;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.rbac.RoleBaseline;
import com.medsentinel.rbac.RoleService;
import com.medsentinel.security.PublicUserDto;

import java.math.BigDecimal;
import java.util.List;

public class UserAdminController {

    private final UserRepository userRepo;
    private final RoleService roleService;

    public UserAdminController(UserRepository userRepo, RoleService roleService) {
        this.userRepo = userRepo;
        this.roleService = roleService;
    }

    public List<User> listUsers() {
        return userRepo.findAll();
    }

    public List<PublicUserDto> listUserDtos() {
        return userRepo.findAll().stream()
            .map(PublicUserDto::from)
            .toList();
    }

    public User toggleUser(Long id) {
        User user = userRepo.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(user.getStatus() == User.UserStatus.ACTIVE
                ? User.UserStatus.DISABLED : User.UserStatus.ACTIVE);
            userRepo.save(user);
        }
        return user;
    }

    public PublicUserDto toggleUserDto(Long id) {
        return PublicUserDto.from(toggleUser(id));
    }

    public List<RoleBaseline> listBaselines() {
        return roleService.getAllBaselines();
    }

    public RoleBaseline updateBaseline(Long id, Integer maxRecords, BigDecimal multiplier) {
        return roleService.updateBaseline(id, maxRecords, multiplier);
    }
}
