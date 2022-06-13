package org.test_task.deeplay;

public class SolutionException extends Exception {
    public SolutionException(String message) {
        super(message);
    }
    public SolutionException(String message, Exception e) {
        super(message, e);
    }
}
