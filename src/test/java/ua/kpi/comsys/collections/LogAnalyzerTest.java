package ua.kpi.comsys.collections;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LogAnalyzerTest {

    private final LogAnalyzer analyzer = new LogAnalyzer();
    private String log(String ip) {
        return ip + " - - [12/Oct/2026:07:27:11 +0000] "
                + "\"GET /index.html HTTP/1.1\" 200 0001";
    }

    @Test
    void uniqueIpsReturnsDistinctAddresses() {
        List<String> lines = List.of(
                log("192.168.1.1"),
                log("10.0.0.1"),
                log("192.168.1.1")
        );
        assertEquals(
                Set.of("192.168.1.1", "10.0.0.1"),
                analyzer.getUniqueIps(lines)
        );
    }

    @Test
    void uniqueIpsReturnsEmptySetForEmptyList() {
        assertTrue(analyzer.getUniqueIps(List.of()).isEmpty());
    }

    @Test
    void uniqueIpsIgnoresInvalidLines() {
        List<String> lines = Arrays.asList(
                "Invalid log line",
                log("192.168.1.1"),
                null,
                "10.0.0.1 invalid data"
        );
        assertEquals(
                Set.of("192.168.1.1"),
                analyzer.getUniqueIps(lines)
        );
    }

    @Test
    void uniqueIpsIgnoresOnlyInvalidLines() {
        assertTrue(
                analyzer.getUniqueIps(
                        Arrays.asList("invalid", "", null)
                ).isEmpty()
        );
    }

    @Test
    void uniqueIpsAcceptsMissingResponseSize() {
        String line = log("10.0.0.1")
                .replace("200 0001", "404 -");
        assertEquals(
                Set.of("10.0.0.1"),
                analyzer.getUniqueIps(List.of(line))
        );
    }

    @Test
    void uniqueIpsRejectsNullList() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.getUniqueIps(null)
        );
        assertEquals(
                "logLines cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void topIpsSortsByFrequency() {
        List<String> lines = List.of(
                log("192.168.1.1"),
                log("10.0.0.1"),
                log("10.0.0.1"),
                log("172.16.0.1"),
                log("10.0.0.1"),
                log("192.168.1.1")
        );
        assertEquals(
                List.of("10.0.0.1", "192.168.1.1", "172.16.0.1"),
                analyzer.getTopIps(lines, 3)
        );
    }

    @Test
    void topIpsSortsAlphabeticallyWhenFrequenciesMatch() {
        List<String> lines = List.of(
                log("192.168.1.1"),
                log("10.0.0.2"),
                log("10.0.0.1")
        );
        assertEquals(
                List.of("10.0.0.1", "10.0.0.2", "192.168.1.1"),
                analyzer.getTopIps(lines, 3)
        );
    }

    @Test
    void topIpsLimitsResultSize() {
        List<String> lines = List.of(
                log("10.0.0.1"),
                log("10.0.0.1"),
                log("10.0.0.2")
        );
        assertEquals(
                List.of("10.0.0.1"),
                analyzer.getTopIps(lines, 1)
        );
    }

    @Test
    void topIpsReturnsAllWhenTopNExceedsUniqueCount() {
        List<String> lines = List.of(
                log("10.0.0.2"),
                log("10.0.0.1")
        );
        assertEquals(
                List.of("10.0.0.1", "10.0.0.2"),
                analyzer.getTopIps(lines, 10)
        );
    }

    @Test
    void topIpsReturnsEmptyListForEmptyInput() {
        assertTrue(
                analyzer.getTopIps(List.of(), 5).isEmpty()
        );
    }

    @Test
    void topIpsIgnoresInvalidLines() {
        List<String> lines = Arrays.asList(
                log("10.0.0.1"),
                "Invalid log",
                null,
                log("10.0.0.1"),
                log("10.0.0.2")
        );
        assertEquals(
                List.of("10.0.0.1", "10.0.0.2"),
                analyzer.getTopIps(lines, 5)
        );
    }

    @Test
    void topIpsReturnsEmptyListForInvalidLines() {
        assertTrue(
                analyzer.getTopIps(
                        Arrays.asList("invalid", null),
                        3
                ).isEmpty()
        );
    }

    @Test
    void topIpsRejectsNullList() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.getTopIps(null, 3)
        );
        assertEquals(
                "logLines cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void topIpsRejectsZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.getTopIps(List.of(), 0)
        );
        assertEquals(
                "topN must be greater than 0",
                exception.getMessage()
        );
    }

    @Test
    void topIpsRejectsNegativeValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> analyzer.getTopIps(List.of(), -1)
        );
        assertEquals(
                "topN must be greater than 0",
                exception.getMessage()
        );
    }
}
