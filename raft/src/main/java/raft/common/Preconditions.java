package raft.common;

public class Preconditions {
    public  static void assertTrue(boolean value, Object message) {
        if (!value) {
            throw new IllegalStateException(String.valueOf(message));
        }
    }
}
