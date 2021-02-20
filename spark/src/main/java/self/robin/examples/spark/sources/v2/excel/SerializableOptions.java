package self.robin.examples.spark.sources.v2.excel;

import org.apache.spark.sql.sources.v2.DataSourceOptions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @Description: ...
 * @Author: Robin-Li
 * @DateTime: 2021-02-08 22:02
 */
public class SerializableOptions implements Serializable {

    private final Map<String, String> keyLowerCasedMap;

    private String toLowerCase(String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    public static SerializableOptions of(DataSourceOptions options){
        return new SerializableOptions(options.asMap());
    }

    public SerializableOptions(Map<String, String> originalMap) {
        keyLowerCasedMap = new HashMap<>(originalMap.size());
        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
            keyLowerCasedMap.put(toLowerCase(entry.getKey()), entry.getValue());
        }
    }

    public Map<String, String> asMap() {
        return new HashMap<>(keyLowerCasedMap);
    }

    /**
     * Returns the option value to which the specified key is mapped, case-insensitively.
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(keyLowerCasedMap.get(toLowerCase(key)));
    }

    /**
     * Returns the boolean value to which the specified key is mapped,
     * or defaultValue if there is no mapping for the key. The key match is case-insensitive
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String lcaseKey = toLowerCase(key);
        return keyLowerCasedMap.containsKey(lcaseKey) ?
                Boolean.parseBoolean(keyLowerCasedMap.get(lcaseKey)) : defaultValue;
    }

    /**
     * Returns the integer value to which the specified key is mapped,
     * or defaultValue if there is no mapping for the key. The key match is case-insensitive
     */
    public int getInt(String key, int defaultValue) {
        String lcaseKey = toLowerCase(key);
        return keyLowerCasedMap.containsKey(lcaseKey) ?
                Integer.parseInt(keyLowerCasedMap.get(lcaseKey)) : defaultValue;
    }

    /**
     * Returns the long value to which the specified key is mapped,
     * or defaultValue if there is no mapping for the key. The key match is case-insensitive
     */
    public long getLong(String key, long defaultValue) {
        String lcaseKey = toLowerCase(key);
        return keyLowerCasedMap.containsKey(lcaseKey) ?
                Long.parseLong(keyLowerCasedMap.get(lcaseKey)) : defaultValue;
    }

    /**
     * Returns the double value to which the specified key is mapped,
     * or defaultValue if there is no mapping for the key. The key match is case-insensitive
     */
    public double getDouble(String key, double defaultValue) {
        String lcaseKey = toLowerCase(key);
        return keyLowerCasedMap.containsKey(lcaseKey) ?
                Double.parseDouble(keyLowerCasedMap.get(lcaseKey)) : defaultValue;
    }
}
