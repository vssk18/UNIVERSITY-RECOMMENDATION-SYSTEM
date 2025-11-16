package data;

import model.University;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class UniversityRepository {

    private final List<University> universities;

    // Instance-based API (still works for future use)
    public UniversityRepository(Path csvPath) throws IOException {
        this.universities = loadInternal(csvPath);
    }

    public List<University> getAll() {
        return Collections.unmodifiableList(universities);
    }

    // ✅ New public static loader for the CLI
    public static List<University> load(Path csvPath) throws IOException {
        return new UniversityRepository(csvPath).getAll();
    }

    // 🔒 Internal CSV loader (renamed from `load` → `loadInternal`)
    private static List<University> loadInternal(Path csv) throws IOException {
        List<University> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csv)) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IOException("Empty CSV: " + csv);
            }
            List<String> headers = parseCsvLine(headerLine);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                List<String> cells = parseCsvLine(line);
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < headers.size() && i < cells.size(); i++) {
                    rowMap.put(headers.get(i), cells.get(i));
                }
                list.add(new University(rowMap));
            }
        }
        return list;
    }

    // Small CSV parser that handles quotes
    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out;
    }
}
