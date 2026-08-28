package learn.data;

import learn.models.Role;
import learn.models.User;

public class TestDataHelper {
    public static User existingClient() {
        return new User(1, "Alice Client", "alice", "password", Role.CLIENT);
    }

    public static User existingAgent() {
        return new User(2, "Bob Agent", "bob", "password", Role.AGENT);
    }

    public static User existingAdmin() {
        return new User(3, "Carol Admin", "carol", "password", Role.ADMIN);
    }

    public static User userToCreate() {
        return new User("Dana Client", "dana", "rawpassword1", Role.CLIENT);
    }

    public static User userAfterCreate() {
        User user = userToCreate();
        user.setId(4);
        return user;
    }
}