package utils;

import play.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrossTableUtil {
    public static final String KEY_COLUMN = "RowKey";

    public static <T, V> List<Map<String, Object>> generateCrossTable(List<T> list, CrossTableConverter<T, V> converter) {
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        LinkedHashMap<String, Map<String, Object>> mappedCrossTable = generateMappedCrossTable(
                list, converter);
        for (String key : mappedCrossTable.keySet()) {
            resultMapList.add(mappedCrossTable.get(key));
        }
        return resultMapList;
    }

    public static <T, V> LinkedHashMap<String, Map<String, Object>> generateMappedCrossTable(
            List<T> list, CrossTableConverter<T, V> converter) {
        LinkedHashMap<String, Map<String, Object>> mappedCrossTable = new LinkedHashMap<>();
        for (T target : list) {
            String key = converter.getRowKey(target);
            Logger.debug("Row key: %s", key);
            Map<String, Object> item = mappedCrossTable.get(key);
            if (item == null) {
                item = new HashMap<>();
                item.put(KEY_COLUMN, key);
                mappedCrossTable.put(key, item);
            }
            V oldValue = (V) item.get(converter.getColumnKey(target));
            item.put(converter.getColumnKey(target), converter.addValue(target, oldValue));
        }
        return mappedCrossTable;
    }
}
