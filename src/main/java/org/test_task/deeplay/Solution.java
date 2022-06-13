package org.test_task.deeplay;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Solution {
    static Map<Character, Integer> costs = new HashMap<>();

    public static int getSolution(Path pathFile, String field, String creature) throws SolutionException {
        if (pathFile == null) {
            pathFile = Path.of("./src/test/resources/default.json");
        }
        if (updateCosts(pathFile, creature)) {
            return fordBellman(field);
        }
        else {
            return dynamics(field);
        }
    }

    private static int dynamics(String field) { // TODO подкинь память в 2 строки, можешь ксорить
        var dist = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < 4; i++){
            dist.add(new ArrayList<>());
            for (int j = 0; j < 4; j++){
                dist.get(i).add(null);
            }
        }
        int i = 3, j = 3;
        dist.get(3).set(3, costs.get(field.charAt(4 * i + j)));

        for (j = 2; j >= 0; j--) {
            dist.get(i).set(j, costs.get(field.charAt(4 * i + j)) + dist.get(i).get(j + 1));
        }

        for (i = 2; i >= 0; i--) {
            j = 3;
            dist.get(i).set(j, costs.get(field.charAt(4 * i + j)) + dist.get(i + 1).get(j));
            for (j--; j >= 0; j--) {
                dist.get(i).set(j, costs.get(field.charAt(4 * i + j)) + Math.min(dist.get(i).get(j + 1), dist.get(i + 1).get(j)));
            }
        }
        System.out.println(dist);
        return dist.get(0).get(0) - costs.get(field.charAt(0));
    }

    private static int fordBellman(String field) {
        return 0;
    }

    private static boolean updateCosts(Path pathFile, String creature) throws SolutionException {
        costs.clear();

        boolean containNegative = false;

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
                        if (cost < 0) {
                            containNegative = true;
                        }
                        costs.put(type, cost);
                    }
                }
            }
        } catch (Exception e) {
            throw new SolutionException("TODO", e); // TODO
        }

        System.out.println(costs);
        return containNegative;
    }
}