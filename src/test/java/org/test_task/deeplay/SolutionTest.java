package org.test_task.deeplay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolutionTest {
    static Map<Character, Integer> costs = new HashMap<>();

    private static void updateCosts(Path pathFile, String creature) throws SolutionException {
        if (pathFile == null) {
            pathFile = Path.of("./src/test/resources/default.json");
        }
        costs.clear();
        try {
            JSONObject data = new JSONObject(Files.readString(pathFile));
            {
                JSONArray types = (JSONArray) data.get("types");
                for (int i = 0; i < types.length(); i++) {
                    costs.put(((String) types.get(i)).charAt(0), null); // TODO проверь, что там 1 символ
                }
            }
            JSONArray creatures = (JSONArray) data.get("creatures");
            for (int i = 0; i < creatures.length(); i++) {
                if (Objects.equals(((JSONObject) creatures.get(i)).get("name"), creature)) {
                    var types = costs.keySet();
                    for (var type : types) {
                        int cost = (int) ((JSONObject) creatures.get(i)).get(type.toString());
                        costs.put(type, cost);
                    }
                }
            }
        } catch (Exception e) {
            throw new SolutionException("TODO", e); // TODO
        }
    }

    // TODO построй граф, как-то муторно всё
    private int completeSearch(String field) {
        var dist = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < 4; i++) {
            dist.add(new ArrayList<>());
            for (int j = 0; j < 4; j++) {
                dist.get(i).add(costs.get(field.charAt(4 * i + j)));
            }
        }
        for (int k = 0; k < 4*4; k++)
            for (int i = 0; i < 4*4; i++)
                for (int j = 0; j < 4*4; j++) {
                    //relax (d[i,j] , d[i,k] + d[k,j])
                }
        return dist.get(0).get(0) - costs.get(field.charAt(0));
    }

    @Test
    public void simpleTest() throws SolutionException {
        updateCosts(null, "Human");
        int ans = completeSearch("STWSWTPPTPTTPWPP");
        assertEquals(10, ans);
        assertEquals(ans, Solution.getSolution(null, "STWSWTPPTPTTPWPP", "Human"));
    }
}
