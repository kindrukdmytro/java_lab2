package ua.kpi.comsys.collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+) \\S+ \\S+ \\[\\d{2}/[A-Za-z]{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} [+-]\\d{4}\\] \"[^\"]*\" \\d{3} (?:\\d+|-)$"
    );

    public Set<String> getUniqueIps(List<String> logLines) {
        checkLogLines(logLines);
        Set<String> uniqueIps = new HashSet<>();
        for (String line : logLines) {
            String ip = extract_ip(line);
            if (ip != null) {
                uniqueIps.add(ip);
            }
        }
        return uniqueIps;
    }

    public List<String> getTopIps(List<String> logLines, int topN) {
        checkLogLines(logLines);
        if (topN <= 0) {
            throw new IllegalArgumentException(
                    "topN must be greater than 0"
            );
        }

        Map<String, Integer> frequencies = new HashMap<>();
        for (String line : logLines) {
            String ip = extract_ip(line);
            if (ip != null) {
                frequencies.merge(ip, 1, Integer::sum);
            }
        }

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>(frequencies.entrySet());
        entries.sort(
                Comparator.<Map.Entry<String, Integer>>comparingInt(
                        Map.Entry::getValue
                ).reversed().thenComparing(Map.Entry::getKey)
        );
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, entries.size()); i++) {
            result.add(entries.get(i).getKey());
        }
        return result;
    }

    private static void checkLogLines(List<String> logLines) {
        if (logLines == null) {
            throw new IllegalArgumentException(
                    "logLines cannot be null"
            );
        }
    }

    private static String extract_ip(String line) {
        if (line == null) {
            return null;
        }
        Matcher matcher = LOG_PATTERN.matcher(line);
        return matcher.matches() ? matcher.group(1) : null;
    }
}
