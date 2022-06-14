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
    final static int n = 4;
    final static int NO_EDGE = Integer.MAX_VALUE / 2;

    static Map<Character, Integer> costs = new HashMap<>();

    public static int getSolution(Path pathFile, String field, String creature) throws SolutionException {
        if (field == null) {
            throw new SolutionException("Field is null");
        }
        if (field.length() != n * n) {
            throw new SolutionException("Invalid field size. Expected: " + (n * n) + ", actual: " + field.length());
        }
        if (pathFile == null) {
            pathFile = Path.of("./src/test/resources/default.json");
        }
        if (updateCosts(pathFile, creature)) {
            return fordBellman(field);
        } else {
            return dynamics(field);
        }
    }

    private static int checkGetCost(Character key) throws SolutionException {
        if (!costs.containsKey(key)) {
            throw new SolutionException("Invalid cell type. Possible types: " + costs.keySet() + ", actual: " + key);
        }
        return costs.get(key);
    }

    private static int dynamics(String field) throws SolutionException {
        var dist = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < 2; i++) {
            dist.add(new ArrayList<>());
            for (int j = 0; j < n; j++) {
                dist.get(i).add(null);
            }
        }
        int i = n - 1, j = n - 1;
        dist.get(i % 2).set(j, checkGetCost(field.charAt(n * i + j)));

        for (j--; j >= 0; j--) {
            dist.get(i % 2).set(j, checkGetCost(field.charAt(n * i + j)) + dist.get(i % 2).get(j + 1));
        }

        for (i--; i >= 0; i--) {
            j = n - 1;
            dist.get(i % 2).set(j, checkGetCost(field.charAt(n * i + j)) + dist.get((i + 1) % 2).get(j));
            for (j--; j >= 0; j--) {
                dist.get(i % 2).set(j, checkGetCost(field.charAt(n * i + j)) + Math.min(dist.get(i % 2).get(j + 1), dist.get((i + 1) % 2).get(j)));
            }
        }
        return dist.get(0).get(0) - checkGetCost(field.charAt(0));
    }

    private static int fordBellman(String field) throws SolutionException {
        var dist = new ArrayList<Integer>();
        int countVertex = n * n;
        for (int i = 0; i < countVertex; i++) {
            dist.add(NO_EDGE);
        }
        dist.set(0, 0);
        boolean relax = true;
        for (int k = 0; relax && k < countVertex - 1; k++) {
            relax = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int l = 1; l >= -1; l -= 2) {
                        if (l + i < n && l + i >= 0) {
                            int cost = checkGetCost(field.charAt(n * (i + l) + j));
                            if (dist.get((i + l) * n + j) > dist.get(i * n + j) + cost) {
                                relax = true;
                                dist.set((i + l) * n + j, dist.get(i * n + j) + cost);
                            }
                        }
                        if (l + j < n && l + j >= 0) {
                            int cost = checkGetCost(field.charAt(n * i + j + l));
                            if (dist.get(i * n + j + l) > dist.get(i * n + j) + cost) {
                                relax = true;
                                dist.set(i * n + j + l, dist.get(i * n + j) + cost);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int l = 1; l >= -1; l -= 2) {
                    if (l + i < n && l + i >= 0) {
                        int cost = checkGetCost(field.charAt(n * (i + l) + j));
                        if (dist.get((i + l) * n + j) > dist.get(i * n + j) + cost) {
                            throw new SolutionException("The field contains a negative cycle");
                        }
                    }
                    if (l + j < n && l + j >= 0) {
                        int cost = checkGetCost(field.charAt(n * i + j + l));
                        if (dist.get(i * n + j + l) > dist.get(i * n + j) + cost) {
                            throw new SolutionException("The field contains a negative cycle");
                        }
                    }
                }
            }
        }

        return dist.get(countVertex - 1);
    }

    private static boolean updateCosts(Path pathFile, String creature) throws SolutionException {
        costs.clear();

        boolean containNegative = false;

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
            int countSuitableCreatures = 0;
            for (int i = 0; i < creatures.length(); i++) {
                if (Objects.equals(((JSONObject) creatures.get(i)).get("name"), creature)) {
                    countSuitableCreatures++;
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
            if (countSuitableCreatures != 1) {
                throw new SolutionException("Invalid count of creatures of the type: " + creature + ". Expected: 1, actual: " + countSuitableCreatures);
            }
        } catch (Exception e) {
            throw new SolutionException("Error in the " + pathFile, e);
        }

        return containNegative;
    }
}