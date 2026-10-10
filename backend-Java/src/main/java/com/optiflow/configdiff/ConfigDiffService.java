package com.optiflow.configdiff;

import com.optiflow.shared.JsonDb;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigDiffService {

    private final ConfigDiffDao dao;

    ConfigDiffService(ConfigDiffDao dao) {
        this.dao = dao;
    }

    public static int computeDrift(String source, String target) {
        // TODO: implement by interviewee
        throw new UnsupportedOperationException("computeDrift not implemented");
    }

    public static List<DiffDetail> buildDiffDetails(String source, String target) {
        List<String> sourceLines = source.lines().toList();
        List<String> targetLines = target.lines().toList();
        List<int[]> matchingBlocks = matchingBlocks(sourceLines, targetLines);
        matchingBlocks.add(new int[]{sourceLines.size(), targetLines.size(), 0});

        List<DiffDetail> details = new ArrayList<>();
        int sourceIndex = 0;
        int targetIndex = 0;
        for (int[] block : matchingBlocks) {
            int sourceEnd = block[0];
            int targetEnd = block[1];
            String op = null;
            if (sourceIndex < sourceEnd && targetIndex < targetEnd) {
                op = "replace";
            } else if (sourceIndex < sourceEnd) {
                op = "delete";
            } else if (targetIndex < targetEnd) {
                op = "insert";
            }
            if (op != null) {
                List<String> changedLines = targetIndex < targetEnd
                        ? targetLines.subList(targetIndex, targetEnd)
                        : sourceLines.subList(sourceIndex, sourceEnd);
                details.add(new DiffDetail(op, label(changedLines.get(0))));
            }
            sourceIndex = sourceEnd + block[2];
            targetIndex = targetEnd + block[2];
        }
        return details;
    }

    private static List<int[]> matchingBlocks(List<String> sourceLines, List<String> targetLines) {
        int m = sourceLines.size();
        int n = targetLines.size();
        int[][] lcs = new int[m + 1][n + 1];

        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                lcs[i][j] = sourceLines.get(i).equals(targetLines.get(j))
                        ? lcs[i + 1][j + 1] + 1
                        : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }

        List<int[]> blocks = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < m && j < n) {
            if (sourceLines.get(i).equals(targetLines.get(j))) {
                int sourceStart = i;
                int targetStart = j;
                while (i < m && j < n && sourceLines.get(i).equals(targetLines.get(j))) {
                    i++;
                    j++;
                }
                blocks.add(new int[]{sourceStart, targetStart, i - sourceStart});
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return blocks;
    }

    private static String label(String line) {
        int separator = line.indexOf('=');
        return separator < 0 ? line : line.substring(0, separator);
    }

    public ConfigDiffResponse diff(ConfigDiffRequest request) {
        List<DiffDetail> details = buildDiffDetails(request.sourceConfig(), request.targetConfig());
        int driftScore = computeDrift(request.sourceConfig(), request.targetConfig());

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", JsonDb.newId("diff"));
        record.put("config_id", request.configId());
        record.put("source_env", request.sourceEnv());
        record.put("target_env", request.targetEnv());
        record.put("drift_score", driftScore);
        record.put("diff_details", details);
        dao.insert(record);

        return new ConfigDiffResponse(request.configId(), request.sourceEnv(), request.targetEnv(),
                driftScore, details);
    }

    public List<ConfigDiffResponse> history(String configId) {
        return dao.findAll(Map.of("config_id", configId)).reversed().stream()
                .map(ConfigDiffService::toResponse)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private static ConfigDiffResponse toResponse(Map<String, Object> record) {
        // TODO: implement by interviewee
        throw new UnsupportedOperationException("toResponse not implemented");
    }
}
