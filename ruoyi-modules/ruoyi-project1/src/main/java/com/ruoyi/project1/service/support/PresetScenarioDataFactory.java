package com.ruoyi.project1.service.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.DigestUtils;

/**
 * Builds deterministic preset matching data from the project1 GroundTruth file.
 */
class PresetScenarioDataFactory
{
    private static final String DEFAULT_RESOURCE = "/project1/preset/ground-truth-all-mappings.csv";
    private static final BigDecimal AUTO_APPROVE_THRESHOLD = new BigDecimal("0.850000");

    private final Map<String, List<PresetMapping>> mappingsBySystem;

    private PresetScenarioDataFactory(Map<String, List<PresetMapping>> mappingsBySystem)
    {
        this.mappingsBySystem = mappingsBySystem;
    }

    static PresetScenarioDataFactory loadDefault()
    {
        return load(DEFAULT_RESOURCE);
    }

    static PresetScenarioDataFactory load(String resourcePath)
    {
        InputStream inputStream = PresetScenarioDataFactory.class.getResourceAsStream(resourcePath);
        if (inputStream == null)
        {
            throw new IllegalStateException("Preset GroundTruth resource not found: " + resourcePath);
        }

        Map<String, List<PresetMapping>> grouped = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            String headerLine = reader.readLine();
            if (headerLine == null)
            {
                throw new IllegalStateException("Preset GroundTruth resource is empty: " + resourcePath);
            }
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> headerIndex = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++)
            {
                headerIndex.put(stripBom(headers.get(i)), i);
            }

            String line;
            long rowNo = 1L;
            while ((line = reader.readLine()) != null)
            {
                if (line.trim().isEmpty())
                {
                    continue;
                }
                List<String> columns = parseCsvLine(line);
                PresetMapping mapping = new PresetMapping(
                        rowNo++,
                        value(columns, headerIndex, "source_database").toUpperCase(Locale.ROOT),
                        value(columns, headerIndex, "source_table"),
                        value(columns, headerIndex, "source_column"),
                        value(columns, headerIndex, "target_table"),
                        value(columns, headerIndex, "target_column"),
                        value(columns, headerIndex, "mapping_type"),
                        value(columns, headerIndex, "difficulty"),
                        value(columns, headerIndex, "notes"));
                grouped.computeIfAbsent(mapping.sourceDatabase(), key -> new ArrayList<>()).add(mapping);
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to load preset GroundTruth resource: " + resourcePath, e);
        }
        return new PresetScenarioDataFactory(grouped);
    }

    List<PresetMapping> mappingsFor(String system)
    {
        if (system == null)
        {
            return List.of();
        }
        return mappingsBySystem.getOrDefault(system.toUpperCase(Locale.ROOT), List.of());
    }

    static List<CandidateProfile> candidateProfiles()
    {
        return Arrays.asList(
                new CandidateProfile("Magneto", "all", 0, new BigDecimal("0.675"), new BigDecimal("0.330")),
                new CandidateProfile("Magneto", "one2one", 1, new BigDecimal("0.715"), BigDecimal.ZERO),
                new CandidateProfile("MagnetoBoost", "all", 2, new BigDecimal("0.805"), new BigDecimal("0.175")),
                new CandidateProfile("MagnetoBoost", "one2one", 3, new BigDecimal("0.835"), BigDecimal.ZERO),
                new CandidateProfile("MagnetoGPT", "all", 4, new BigDecimal("0.895"), new BigDecimal("0.085")),
                new CandidateProfile("MagnetoGPT", "one2one", 5, new BigDecimal("0.925"), BigDecimal.ZERO));
    }

    GeneratedResult generateCandidate(String system, CandidateProfile profile)
    {
        List<PresetMapping> mappings = mappingsFor(system);
        if (mappings.isEmpty())
        {
            return new GeneratedResult(List.of(), new Metrics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, 0, 0, 0, 0), List.of(), List.of(), BigDecimal.ZERO);
        }

        int groundTruthCount = mappings.size();
        int truePositiveTarget = targetTruePositiveCount(system, profile, groundTruthCount);
        int extraFalsePositiveCount = targetExtraFalsePositiveCount(profile, groundTruthCount);
        List<Integer> truePositiveIndexes = selectedIndexes(system, profile, groundTruthCount, truePositiveTarget);

        List<GeneratedRow> rows = new ArrayList<>();
        long rowNo = 1L;
        for (int i = 0; i < mappings.size(); i++)
        {
            PresetMapping source = mappings.get(i);
            boolean hit = truePositiveIndexes.contains(i);
            PresetMapping target = hit ? source : wrongTarget(mappings, i, profile);
            BigDecimal score = scoreFor(system, source, profile, rowNo, hit);
            rows.add(new GeneratedRow(rowNo++, source, target.targetTable(), target.targetColumn(), score, hit,
                    reviewStatus(rowNo, score, hit), false));
        }

        for (int i = 0; i < extraFalsePositiveCount; i++)
        {
            int sourceIndex = Math.floorMod((i * 7) + profile.order(), mappings.size());
            PresetMapping source = mappings.get(sourceIndex);
            PresetMapping target = wrongTarget(mappings, sourceIndex + i + 3, profile);
            BigDecimal score = scoreFor(system, source, profile, rowNo, false);
            rows.add(new GeneratedRow(rowNo++, source, target.targetTable(), target.targetColumn(), score, false,
                    reviewStatus(rowNo, score, false), true));
        }

        int truePositiveCount = (int) rows.stream().filter(GeneratedRow::groundTruthHit).count();
        int falsePositiveCount = rows.size() - truePositiveCount;
        int falseNegativeCount = groundTruthCount - truePositiveCount;
        Metrics metrics = metrics(system, profile, truePositiveCount, falsePositiveCount, falseNegativeCount,
                rows.size(), groundTruthCount);
        BigDecimal avgScore = decimal(rows.stream()
                .map(GeneratedRow::score)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(rows.size()), 8, RoundingMode.HALF_UP), profile.order(), system);

        return new GeneratedResult(rows, metrics, scoreDistribution(rows), targetDistribution(rows), avgScore);
    }

    private int targetTruePositiveCount(String system, CandidateProfile profile, int size)
    {
        BigDecimal adjusted = profile.hitRate().add(systemAdjustment(system));
        int count = adjusted.multiply(new BigDecimal(size)).setScale(0, RoundingMode.HALF_UP).intValue();
        return Math.max(1, Math.min(size, count));
    }

    private int targetExtraFalsePositiveCount(CandidateProfile profile, int size)
    {
        return profile.extraFalsePositiveRatio().multiply(new BigDecimal(size)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal systemAdjustment(String system)
    {
        return switch (String.valueOf(system).toUpperCase(Locale.ROOT))
        {
            case "PLM" -> new BigDecimal("-0.012");
            case "ERP" -> new BigDecimal("0.004");
            case "MES" -> new BigDecimal("0.009");
            case "QMS" -> new BigDecimal("-0.006");
            case "MRO" -> new BigDecimal("0.014");
            default -> BigDecimal.ZERO;
        };
    }

    private List<Integer> selectedIndexes(String system, CandidateProfile profile, int size, int count)
    {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            indexes.add(i);
        }
        indexes.sort(Comparator.comparing(index -> stableKey(system, profile, String.valueOf(index))));
        return indexes.subList(0, count);
    }

    private PresetMapping wrongTarget(List<PresetMapping> mappings, int index, CandidateProfile profile)
    {
        int offset = profile.order() * 5 + 3;
        PresetMapping source = mappings.get(Math.floorMod(index, mappings.size()));
        for (int i = 0; i < mappings.size(); i++)
        {
            PresetMapping candidate = mappings.get(Math.floorMod(index + offset + i, mappings.size()));
            if (!Objects.equals(candidate.targetTable(), source.targetTable())
                    || !Objects.equals(candidate.targetColumn(), source.targetColumn()))
            {
                return candidate;
            }
        }
        return source;
    }

    private BigDecimal scoreFor(String system, PresetMapping mapping, CandidateProfile profile, long rowNo, boolean hit)
    {
        BigDecimal base = hit ? new BigDecimal("0.790000") : new BigDecimal("0.535000");
        BigDecimal methodBoost = new BigDecimal(profile.order()).multiply(new BigDecimal("0.026000"));
        BigDecimal jitter = new BigDecimal(stableBucket(system, profile, mapping.sourceTable() + mapping.sourceColumn() + rowNo, 137))
                .movePointLeft(3);
        BigDecimal score = base.add(methodBoost).add(jitter);
        BigDecimal max = hit ? new BigDecimal("0.992731") : new BigDecimal("0.842731");
        return score.min(max).setScale(6, RoundingMode.HALF_UP);
    }

    private String reviewStatus(long rowNo, BigDecimal score, boolean hit)
    {
        if (hit)
        {
            if (rowNo % 17 == 0)
            {
                return "approved";
            }
            return score.compareTo(AUTO_APPROVE_THRESHOLD) >= 0 ? "auto_approved" : "pending";
        }
        return rowNo % 9 == 0 ? "pending" : "rejected";
    }

    private Metrics metrics(String system, CandidateProfile profile, int truePositiveCount, int falsePositiveCount,
            int falseNegativeCount, int matchCount, int groundTruthCount)
    {
        BigDecimal precision = ratio(truePositiveCount, truePositiveCount + falsePositiveCount);
        BigDecimal recall = ratio(truePositiveCount, truePositiveCount + falseNegativeCount);
        BigDecimal f1 = precision.add(recall).compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : precision.multiply(recall).multiply(new BigDecimal("2"))
                        .divide(precision.add(recall), 8, RoundingMode.HALF_UP);
        BigDecimal recallAt20 = recall.add(new BigDecimal(profile.order()).multiply(new BigDecimal("0.0041")))
                .min(new BigDecimal("0.987654"));
        BigDecimal mrr = f1.add(new BigDecimal("0.0613"))
                .add(new BigDecimal(profile.order()).multiply(new BigDecimal("0.0037")))
                .add(systemAdjustment(system).divide(new BigDecimal("3"), 8, RoundingMode.HALF_UP))
                .min(new BigDecimal("0.992319"));
        return new Metrics(
                decimal(f1, profile.order(), system),
                decimal(mrr, profile.order() + 2, system),
                decimal(precision, profile.order() + 4, system),
                decimal(recallAt20, profile.order() + 6, system),
                matchCount,
                groundTruthCount,
                truePositiveCount,
                falsePositiveCount);
    }

    private BigDecimal ratio(int numerator, int denominator)
    {
        if (denominator == 0)
        {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(BigDecimal value, int salt, String system)
    {
        BigDecimal epsilon = new BigDecimal(stableBucket(system, null, String.valueOf(salt), 731) + 1)
                .movePointLeft(8);
        return value.add(epsilon).min(new BigDecimal("0.999999")).setScale(6, RoundingMode.HALF_UP);
    }

    private List<Map<String, Object>> scoreDistribution(List<GeneratedRow> rows)
    {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("score = 1.00", 0);
        counts.put("0.90 - 0.99", 0);
        counts.put("0.80 - 0.89", 0);
        counts.put("0.70 - 0.79", 0);
        counts.put("< 0.70", 0);
        for (GeneratedRow row : rows)
        {
            BigDecimal score = row.score();
            if (score.compareTo(new BigDecimal("0.995")) >= 0)
            {
                counts.compute("score = 1.00", (key, value) -> value + 1);
            }
            else if (score.compareTo(new BigDecimal("0.900")) >= 0)
            {
                counts.compute("0.90 - 0.99", (key, value) -> value + 1);
            }
            else if (score.compareTo(new BigDecimal("0.800")) >= 0)
            {
                counts.compute("0.80 - 0.89", (key, value) -> value + 1);
            }
            else if (score.compareTo(new BigDecimal("0.700")) >= 0)
            {
                counts.compute("0.70 - 0.79", (key, value) -> value + 1);
            }
            else
            {
                counts.compute("< 0.70", (key, value) -> value + 1);
            }
        }
        return counts.entrySet().stream().map(entry -> chartRow(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    private List<Map<String, Object>> targetDistribution(List<GeneratedRow> rows)
    {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (GeneratedRow row : rows)
        {
            counts.put(row.targetTable(), counts.getOrDefault(row.targetTable(), 0) + 1);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> chartRow(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> chartRow(String label, int value)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("targetTable", label);
        row.put("value", value);
        return row;
    }

    private String stableKey(String system, CandidateProfile profile, String value)
    {
        return DigestUtils.md5DigestAsHex((String.valueOf(system) + "|"
                + (profile == null ? "" : profile.method() + "|" + profile.variant()) + "|" + value)
                        .getBytes(StandardCharsets.UTF_8));
    }

    private int stableBucket(String system, CandidateProfile profile, String value, int bucketSize)
    {
        String digest = stableKey(system, profile, value);
        return Math.floorMod(Integer.parseUnsignedInt(digest.substring(0, 7), 16), bucketSize);
    }

    private static String value(List<String> columns, Map<String, Integer> headerIndex, String header)
    {
        Integer index = headerIndex.get(header);
        if (index == null || index >= columns.size())
        {
            return "";
        }
        return columns.get(index).trim();
    }

    private static String stripBom(String value)
    {
        if (value != null && !value.isEmpty() && value.charAt(0) == '\uFEFF')
        {
            return value.substring(1);
        }
        return value;
    }

    private static List<String> parseCsvLine(String line)
    {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++)
        {
            char ch = line.charAt(i);
            if (ch == '"')
            {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"')
                {
                    current.append('"');
                    i++;
                }
                else
                {
                    quoted = !quoted;
                }
            }
            else if (ch == ',' && !quoted)
            {
                columns.add(current.toString());
                current.setLength(0);
            }
            else
            {
                current.append(ch);
            }
        }
        columns.add(current.toString());
        return columns;
    }

    record PresetMapping(long rowNo, String sourceDatabase, String sourceTable, String sourceColumn,
            String targetTable, String targetColumn, String mappingType, String difficulty, String notes)
    {
    }

    record CandidateProfile(String method, String variant, int order, BigDecimal hitRate,
            BigDecimal extraFalsePositiveRatio)
    {
    }

    record GeneratedRow(long rowNo, PresetMapping source, String targetTable, String targetColumn, BigDecimal score,
            boolean groundTruthHit, String reviewStatus, boolean extraCandidate)
    {
    }

    record Metrics(BigDecimal f1Score, BigDecimal mrr, BigDecimal precision, BigDecimal recallAt20,
            int matchCount, int groundTruthCount, int truePositiveCount, int falsePositiveCount)
    {
    }

    record GeneratedResult(List<GeneratedRow> rows, Metrics metrics, List<Map<String, Object>> scoreDistribution,
            List<Map<String, Object>> targetDistribution, BigDecimal avgScore)
    {
    }
}
