package org.parnasSolutions;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {
    public static boolean isValid(String line) {
        return line.matches("^\"[^\"]*\"(;\"[^\"]*\")*$");
    }

    private static String find(Map<String, String> parents, String x) {
        String parent = parents.get(x);
        if(!parent.equals(x)) {
            return find(parents, parent);
        }
        return parent;
    }

    private static void union(Map<String, String> parents, String x, String y) {
        String root1 = find(parents, x);
        String root2 = find(parents, y);
        if(!root1.equals(root2)) {
            parents.put(root1, root2);
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        Path inputPath = Paths.get(args[0]);
        if (!Files.exists(inputPath)) {
            System.err.println("Файл не найден: " + inputPath);
            System.exit(1);
        }

        Set<String> uniqueLines = new LinkedHashSet<>();
        List<String[]> splitedLines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(isValid(line)) {
                    if(uniqueLines.add(line)) {
                        splitedLines.add(line.split(";", -1));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> lines = new ArrayList<>(uniqueLines);

        Map<String, String> parents = new HashMap<>();
        for (String line : lines) {
            parents.put(line, line);
        }

        Map<Integer, Map<String, String>> columnValueLineMap = new HashMap<>();
        for(int i = 0; i < splitedLines.size(); i++) {
            String[] splitedLine = splitedLines.get(i);
            String line = lines.get(i);

            for(int col = 0; col < splitedLine.length; col++) {
                String value = splitedLine[col];

                if(!value.isEmpty() && !value.equals("\"\"")) {
                    Map<String, String> valueLineMap =  columnValueLineMap.computeIfAbsent(col, _ -> new HashMap<>());

                    if(!valueLineMap.containsKey(value)) {
                        valueLineMap.put(value, line);
                    } else {
                        union(parents, line, valueLineMap.get(value));
                    }
                }
            }
        }

        Map<String, Set<String>> groups = new HashMap<>();
        for (String line : lines) {
            String root = find(parents, line);
            groups.computeIfAbsent(root, _ -> new HashSet<>()).add(line);
        }

        //Отбираем и сортируем группы
        List<Set<String>> resultGroups = groups.values().stream()
                .filter(it -> it.size() > 1)
                .sorted((a, b) -> Integer.compare(b.size(), a.size()))
                .toList();

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("output_file.txt"))) {
            writer.write("" + resultGroups.size());
            writer.newLine();

            int groupCounter = 1;
            for(Set<String> component : resultGroups) {
                writer.write("Группа " + groupCounter);
                writer.newLine();
                for (String line : component) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.newLine();
                groupCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Количество групп с более чем одним элементом: " + resultGroups.size());
        System.out.println("Время выполнения: " + duration + " мс");
    }
}