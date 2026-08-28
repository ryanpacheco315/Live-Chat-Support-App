package learn.dtos;

import learn.models.Role;
import learn.models.User;

public class UserResponse {
    private int id;
    private String fullName;
    private String username;
    private Role role;

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.fullName = user.getFullName();
        response.username = user.getUsername();
        response.role = user.getRole();
        return response;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }
}
