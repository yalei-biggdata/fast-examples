package self.robin.examples.utils.json;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * @author mrli
 * @date 2020/7/13
 */
public class JSONObject extends JSON implements Map<String, Object>, Serializable, Cloneable {

    private static final long  serialVersionUID         = 1L;
    private static final int   DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<String, Object> map;

    public JSONObject(){
        this(DEFAULT_INITIAL_CAPACITY, false);
    }

    public JSONObject(boolean ordered){
        this(DEFAULT_INITIAL_CAPACITY, ordered);
    }

    public JSONObject(int initialCapacity){
        this(initialCapacity, false);
    }

    public JSONObject(int initialCapacity, boolean ordered){
        if (ordered) {
            map = new LinkedHashMap<String, Object>(initialCapacity);
        } else {
            map = new HashMap<String, Object>(initialCapacity);
        }
    }

    public JSONObject(Map<String, Object> map){
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        Object val = map.get(key);

        if (val == null && key instanceof Number) {
            val = map.get(key.toString());
        }
        return val;
    }

    public JSONObject getJSONObject(String key) {
        Object value = get(key);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        return ((JSONObject) toJSON(value).get());
    }

    public JSONArray getJSONArray(String key) {
        Object value = get(key);
        return JSON.parseArray(value);
    }
//
    public <T> T getObject(String key, Class<T> clazz) {
        Object obj = get(key);
//        if(obj instanceof ){
//
//        }
//
//        return JSON.parseObject(obj, clazz);
        return null;
    }

    public <T> T getObject(String key, Type type) {
        Object obj = get(key);
//        return JSON.parseObject(obj, type);
        return null;
    }

    public Boolean getBoolean(String key) {
        Object value = get(key);
        return castToBoolean(value);
    }

    public byte[] getBytes(String key) {
        Object value = get(key);
        return castToBytes(value);
    }

    public boolean getBooleanValue(String key) {
        Boolean booleanVal = getBoolean(key);
        if (booleanVal == null) {
            return false;
        }
        return booleanVal.booleanValue();
    }

    public Byte getByte(String key) {
        Object value = get(key);
        return castToByte(value);
    }

    public byte getByteValue(String key) {
        Byte byteVal = getByte(key);
        if (byteVal == null) {
            return 0;
        }

        return byteVal.byteValue();
    }

    public Short getShort(String key) {
        Object value = get(key);
        return castToShort(value);
    }

    public short getShortValue(String key) {
        Short shortVal = getShort(key);
        if (shortVal == null) {
            return 0;
        }

        return shortVal.shortValue();
    }

    public Integer getInteger(String key) {
        Object value = get(key);
        return castToInteger(value);
    }

    public int getIntValue(String key) {
        Integer intVal = getInteger(key);
        if (intVal == null) {
            return 0;
        }

        return intVal.intValue();
    }

    public Long getLong(String key) {
        Object value = get(key);
        return castToLong(value);
    }


    public long getLongValue(String key) {
        Long longVal = getLong(key);
        if (longVal == null) {
            return 0L;
        }

        return longVal.longValue();
    }

    public Float getFloat(String key) {
        Object value = get(key);
        return castToFloat(value);
    }

    public float getFloatValue(String key) {
        Float floatValue = getFloat(key);
        if (floatValue == null) {
            return 0F;
        }

        return floatValue.floatValue();
    }

    public Double getDouble(String key) {
        Object value = get(key);
        return castToDouble(value);
    }

    public double getDoubleValue(String key) {
        Double doubleValue = getDouble(key);
        if (doubleValue == null) {
            return 0D;
        }

        return doubleValue.doubleValue();
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);
        return castToBigDecimal(value);
    }

    public BigInteger getBigInteger(String key) {
        Object value = get(key);
        return castToBigInteger(value);
    }

    public String getString(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public Date getDate(String key) {
        Object value = get(key);
        return castToDate(value);
    }

    public java.sql.Date getSqlDate(String key) {
        Object value = get(key);
        return castToSqlDate(value);
    }

    public java.sql.Timestamp getTimestamp(String key) {
        Object value = get(key);
        return castToTimestamp(value);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }
    public JSONObject fluentPut(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public JSONObject fluentPutAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
        return this;
    }

    public JSONObject fluentRemove(Object key) {
        map.remove(key);
        return this;
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    public JSONObject fluentClear() {
        map.clear();
        return this;
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new JSONObject(map instanceof LinkedHashMap
                ? new LinkedHashMap<String, Object>(map)
                : new HashMap<String, Object>(map)
        );
    }

    @Override
    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {
        return toJSONString(this);
    }

    public String toJSONString(){
        return toJSONString(this);
    }
}
