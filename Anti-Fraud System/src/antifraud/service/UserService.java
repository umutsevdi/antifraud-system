package antifraud.service;


import antifraud.config.PasswordEncoderProvider;
import antifraud.controller.request.ChangeRoleRequest;
import antifraud.controller.request.ChangeStatusRequest;
import antifraud.controller.response.StatusChangeResponse;
import antifraud.controller.response.UserDeleteResponse;
import antifraud.repository.UserRepository;
import antifraud.repository.entity.User;
import antifraud.service.dto.UserDto;
import antifraud.service.dto.UserRole;
import antifraud.service.dto.UserStatus;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoderProvider passwordEncoderProvider;

    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        if (userRepository.count() == 0) {
            user.setRole(UserRole.ADMINISTRATOR);
            user.setAccountLocked(false);
        }

        user.setPassword(passwordEncoderProvider.getEncoder().encode(user.getPassword()));
        user.setUsername(user.getUsername());

        return userRepository.save(user);
    }

    public UserDeleteResponse delete(String username) {
        User user = findUser(username);
        userRepository.delete(user);
        return new UserDeleteResponse(username);
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    private User findUser(String username) {
        return Optional
                .ofNullable(userRepository.findUserByUsernameIgnoreCase(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public User changeRole(ChangeRoleRequest changeUserRoleRequest) {
        User user = findUser(changeUserRoleRequest.getUsername());

        try {
            UserRole userRole = UserRole.valueOf(changeUserRoleRequest.getRole().toUpperCase());
            if ((userRole != UserRole.SUPPORT) && (userRole != UserRole.MERCHANT)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            } else if (userRole.equals(user.getRole())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
            user.setRole(userRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return userRepository.save(user);
    }

    public StatusChangeResponse changeStatus(ChangeStatusRequest changeUserStatusRequest) {
        User user = findUser(changeUserStatusRequest.getUsername());

        if (user.getRole() == UserRole.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            UserStatus userStatus = UserStatus.valueOf(changeUserStatusRequest.getOperation().toUpperCase());
            user.setAccountLocked(userStatus == UserStatus.LOCK);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        userRepository.save(user);
        return new StatusChangeResponse(user.getUsername(), user.getAccountLocked());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = Optional
                .ofNullable(userRepository.findUserByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));

        return new UserDto(user);
    }
}
