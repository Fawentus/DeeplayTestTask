package org.test_task.deeplay;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolutionTest {
    @Test
    public void simpleTest() throws SolutionException {
        assertEquals(10, Solution.getSolution(null, "STWSWTPPTPTTPWPP", "Human"));
    }
}
