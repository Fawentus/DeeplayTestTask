package org.test_task.deeplay;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SolutionTest {
    final static Path DEFAULT = Path.of("./src/test/resources/default.json");
    final static Path WITHOUT_NEGATIVE = Path.of("./src/test/resources/withoutNegative.json");
    final static Path WITH_NEGATIVE = Path.of("./src/test/resources/withNegative.json");

    final static int n = 4;
    final static int NO_EDGE = Integer.MAX_VALUE / 2;

    static Map<Character, Integer> costs = new HashMap<>();

    private static String updateCosts(Path pathFile, String creature) throws SolutionException {
        if (pathFile == null) {
            pathFile = DEFAULT;
        }
        costs.clear();
        try {
            JSONObject data = new JSONObject(Files.readString(pathFile));
            {
                JSONArray types = (JSONArray) data.get("types");
                for (int i = 0; i < types.length(); i++) {
                    if (((String) types.get(i)).length() != 1) {
                        throw new SolutionException("Invalid cell type format. Expected: one character, actual: " + types.get(i));
                    }
                    costs.put(((String) types.get(i)).charAt(0), null);
                }
            }

            JSONArray creatures = (JSONArray) data.get("creatures");
            if (creature == null) {
                creature = (String) ((JSONObject) creatures.get((new Random()).nextInt(creatures.length()))).get("name");
            }
            int countSuitableCreatures = 0;
            var types = costs.keySet();
            for (int i = 0; i < creatures.length(); i++) {
                if (Objects.equals(((JSONObject) creatures.get(i)).get("name"), creature)) {
                    countSuitableCreatures++;
                    for (var type : types) {
                        int cost = (int) ((JSONObject) creatures.get(i)).get(type.toString());
                        costs.put(type, cost);
                    }
                }
            }
            if (countSuitableCreatures != 1) {
                throw new SolutionException("Invalid count of creatures of the type: " + creature + ". Expected: 1, actual: " + countSuitableCreatures);
            }
            return creature;
        } catch (Exception e) {
            throw new SolutionException("Error in the " + pathFile, e);
        }
    }

    private int floyd(@NotNull String field) throws SolutionException {
        if (field.length() != n * n) {
            throw new SolutionException("Invalid field size. Expected: " + (n * n) + ", actual: " + field.length());
        }

        var dist = new ArrayList<ArrayList<Integer>>();

        for (int i1 = 0; i1 < n; i1++)
            for (int j1 = 0; j1 < n; j1++) {
                dist.add(new ArrayList<>());
                for (int i2 = 0; i2 < n; i2++)
                    for (int j2 = 0; j2 < n; j2++) {
                        if (i1 * n + j1 != i2 * n + j2 && ((i1 - i2 <= 1 && i2 - i1 <= 1 && j1 == j2) || (i1 == i2 && j1 - j2 <= 1 && j2 - j1 <= 1))) {
                            if (!costs.containsKey(field.charAt(n * i2 + j2))) {
                                throw new SolutionException("Invalid cell type. Possible types: " + costs.keySet() + ", actual: " + field.charAt(n * i2 + j2));
                            }
                            dist.get(i1 * n + j1).add(costs.get(field.charAt(n * i2 + j2)));
                        } else {
                            dist.get(i1 * n + j1).add(NO_EDGE);
                        }
                    }
            }


        for (int i1 = 0; i1 < n; i1++)
            for (int j1 = 0; j1 < n; j1++)
                for (int i2 = 0; i2 < n; i2++)
                    for (int j2 = 0; j2 < n; j2++)
                        for (int i3 = 0; i3 < n; i3++)
                            for (int j3 = 0; j3 < n; j3++) {
                                if (dist.get(i2 * n + j2).get(i3 * n + j3) > dist.get(i2 * n + j2).get(i1 * n + j1) + dist.get(i1 * n + j1).get(i3 * n + j3)) {
                                    dist.get(i2 * n + j2).set(i3 * n + j3, dist.get(i2 * n + j2).get(i1 * n + j1) + dist.get(i1 * n + j1).get(i3 * n + j3));
                                }
                            }

        for (int i1 = 0; i1 < n; i1++)
            for (int j1 = 0; j1 < n; j1++) {
                if (dist.get(i1 * n + j1).get(i1 * n + j1) < 0) {
                    throw new SolutionException("The field contains a negative cycle");
                }
            }
        return dist.get(0).get(n * n - 1);
    }

    @Test
    public void simpleTest() throws SolutionException {
        updateCosts(null, "Human");
        int ans = floyd("STWSWTPPTPTTPWPP");
        assertEquals(10, ans);
        assertEquals(ans, Solution.getSolution(null, "STWSWTPPTPTTPWPP", "Human"));
    }

    @Test
    public void testInvalidFieldSize() {
        assertThrows(SolutionException.class, () -> Solution.getSolution(null, "STWSWTPPT", "Human"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(DEFAULT, "STWSWTPPTPPPPPPSSSSSSSSSSPPPPPP", "Swamper"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(WITHOUT_NEGATIVE, "", "Vampire"));
    }

    @Test
    public void testInvalidCellType() {
        assertThrows(SolutionException.class, () -> Solution.getSolution(null, "NNNNNNNNNNNNNNNN", "Woodman"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(WITHOUT_NEGATIVE, "STWSWTPPTPTTPWPP", "Human"));
    }

    @Test
    public void testInvalidCreature() {
        assertThrows(SolutionException.class, () -> Solution.getSolution(null, "STSSWTPPWPTTPSPP", "Vampire"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(null, "STWWWTPPTPTTPWPP", "Dog"));

        assertThrows(SolutionException.class, () -> Solution.getSolution(null, "STSSWTPPWPTTPSPP", "Vampire"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(WITHOUT_NEGATIVE, "STSSVTSSVSTTSSSS", "Woodman"));
    }

    @Test
    public void testNegativeCycle() {
        assertDoesNotThrow(() -> updateCosts(WITH_NEGATIVE, "Mermaid"));
        assertThrows(SolutionException.class, () -> floyd("TPTTPSWTTPPPWTTP"));
        assertDoesNotThrow(() -> updateCosts(WITH_NEGATIVE, "Rabbit"));
        assertThrows(SolutionException.class, () -> floyd("SSWSSWWSSPTSWWWS"));

        assertThrows(SolutionException.class, () -> Solution.getSolution(WITH_NEGATIVE, "TPTTPSWTTPPPWTTP", "Mermaid"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(WITH_NEGATIVE, "SSWSSWWSSPTSWWWS", "Rabbit"));
    }

    @Test
    public void testWrongJSON() {
        assertThrows(SolutionException.class, () -> Solution.getSolution(Path.of("./src/test/resources/wrongPath.json"), "TPTTPSWTTPPPWTTP", "Someone"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(Path.of("./src/test/resources/wrong1.json"), "TPTTPSWTTPPPWTTP", "Human"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(Path.of("./src/test/resources/wrong2.json"), "SSWSSWWSSPTSWWWS", "Human"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(Path.of("./src/test/resources/wrong3.json"), "TPTTPSWTTPPPWTTP", "Swamper"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(Path.of("./src/test/resources/wrong4.json"), "SSWSSWWSSPTSWWWS", "Woodman"));
        assertThrows(SolutionException.class, () -> Solution.getSolution(Path.of("./src/test/resources/wrong5.json"), "TPTTPSWTTPPPWTTP", "Human"));
    }

    private void testRandomField(Path path) {
        AtomicReference<String> creature = new AtomicReference<>();
        assertDoesNotThrow(() -> creature.set(updateCosts(path, null)));
        StringBuilder builderField = new StringBuilder();
        for (int i = 0; i < n * n; i++) {
            builderField.append(costs.keySet().stream().findAny().get());
        }
        String field = builderField.toString();

        int countThrow = 0;
        int expected = 0, actual = 0;
        try {
            expected = floyd(field);
        }
        catch (SolutionException e) {
            countThrow++;
        }
        try {
            actual = Solution.getSolution(path, field, creature.get());
        }
        catch (SolutionException e) {
            countThrow++;
        }
        assertEquals(0, countThrow % 2);
        if (countThrow == 0) {
            assertEquals(expected, actual);
        }
    }

    @RepeatedTest(500)
    public void testRandomField() {
        testRandomField(DEFAULT);
        testRandomField(WITH_NEGATIVE);
        testRandomField(WITHOUT_NEGATIVE);
    }
}
