package learn.data;

import learn.models.*;

import java.time.LocalDateTime;

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

    public static Problem existingProblem1() {
        return new Problem(1, ProblemCategory.HARDWARE, "LAPTOP", "Laptop will not turn on.");
    }

    public static Problem existingProblem2() {
        return new Problem(2, ProblemCategory.SOFTWARE, "EMAIL", "Cannot log in to email.");
    }

    public static Problem problemToCreate() {
        return new Problem(ProblemCategory.OTHER, null, "Something else is wrong.");
    }

    public static Problem problemAfterCreate() {
        Problem problem = problemToCreate();
        problem.setId(3);
        return problem;
    }

    public static TimeRecord existingTimeRecord1() {
        return new TimeRecord(1, LocalDateTime.of(2026, 1, 1, 9, 0), null);
    }

    public static TimeRecord existingTimeRecord2() {
        return new TimeRecord(2, LocalDateTime.of(2026, 1, 2, 10, 0), null);
    }

    public static TimeRecord timeRecordToCreate() {
        return new TimeRecord(LocalDateTime.of(2026, 1, 3, 8, 0), null);
    }

    public static TimeRecord timeRecordAfterCreate() {
        TimeRecord timeRecord = timeRecordToCreate();
        timeRecord.setId(3);
        return timeRecord;
    }

    public static Chat existingActiveChat() {
        return new Chat(1, existingClient(), existingAgent(), ChatStatus.ACTIVE,
                existingProblem1(), existingTimeRecord1());
    }

    public static Chat existingWaitingChat() {
        return new Chat(2, existingClient(), null, ChatStatus.WAITING,
                existingProblem2(), existingTimeRecord2());
    }
}
