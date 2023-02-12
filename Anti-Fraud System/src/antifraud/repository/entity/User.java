package antifraud.repository.entity;

import antifraud.service.dto.UserRole;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(View.UserView.class)
    private long id;

    @NotEmpty
    @JsonView(View.UserView.class)
    private String name;

    @NotEmpty
    @Column(unique = true)
    @JsonView(View.UserView.class)
    private String username;

    @NotEmpty
    private String password;

    @JsonView(View.UserView.class)
    private UserRole role = UserRole.MERCHANT;

    private Boolean isAccountLocked = true;

    public User(long id, String name, String username, String password, UserRole role, Boolean isAccountLocked) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isAccountLocked = isAccountLocked;
    }

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getAccountLocked() {
        return isAccountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        isAccountLocked = accountLocked;
    }
}