package self.robin.examples.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author mrli
 * @date 2020/7/13
 */
public class JSON {

    public static <T> T parseObject(String value, Type type) {
        return parseObject(value, ()->type);
    }

    public static <T> T parseObject(String value, Class<T> clazz) {
        if(value==null){
            return null;
        }
        return new Gson().fromJson(value, clazz);
    }

    private static <T> T parseObject(String value, Supplier<? extends Type> supplier) {
        if(value==null){
            return null;
        }
        return new Gson().fromJson(value, supplier.get());
    }

    private static <T> T parseObject(Reader reader, Supplier<? extends Type> supplier) {
        if(reader==null){
            return null;
        }
        return new Gson().fromJson(reader, supplier.get());
    }

    public static <T> T parseObject(Reader reader, Class<T> clazz) {
        return parseObject(reader, ()->clazz);
    }

    public static <T> T parseObject(InputStream input, Class<T> clazz) {
        return parseObject(new InputStreamReader(input), ()->clazz);
    }

    public static <T> T parseObject(InputStream input, Type type) {
        return parseObject(new InputStreamReader(input), ()->type);
    }

    public static JSONObject parseObject(String value){
        Map<String, Object> tmp = parseObject(value, Map.class);
        return new JSONObject(tmp);
    }


    public static JSONArray parseArray(Object value){
        if(value==null){
            return null;
        }

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        Gson gson = new Gson();
        if (value instanceof String) {
            List list =  parseArray((String)value, Object.class);
            return new JSONArray(list);
        }

        if(value instanceof List){
            List list = ((List) value);
            return new JSONArray(list);
        }

        if(value instanceof Object[]){
            ArrayList list = new ArrayList<>(Arrays.asList(((Object[]) value)));
            return new JSONArray(list);
        }

        throw new RuntimeException("解析失败");
        /*com.google.gson.JsonArray jsonArr = gson.toJsonTree(value).getAsJsonArray();
        return new JSONArray(jsonArr.iterator());*/
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text == null) {
            return null;
        }
        Gson gson = new Gson();
        List<T> list = gson.fromJson((String)text, new TypeToken<List<T>>(){}.getType());
        return list;
    }

    public static List<Object> parseArray(String text, Type[] types) {
        if (text == null) {
            return null;
        }
        Gson gson = new Gson();
        Type[] list = gson.fromJson((String) text, types.getClass());
        return Arrays.asList(list);
    }

    public static String toJSONString(Object value){
        return new GsonBuilder().disableHtmlEscaping().create().toJson(value);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public <T> T toJavaObject(Class<T> tClass){
        if(tClass == Map.class || tClass==List.class){
            return ((T) this);
        }
        return parseObject(toJSONString(this), tClass);
    }

    public static <T> T toJavaObject(JSON json, Class<T> clazz) {
        if(json==null){
            return null;
        }
        return json.toJavaObject(clazz);
    }

    public static Optional<Object> toJSON(Object javaObject){
        if(javaObject==null){
            return Optional.empty();
        }
        if(javaObject instanceof List){
            return Optional.of(new JSONArray((List) javaObject));
        }

        if(javaObject instanceof Map){
            return Optional.of(new JSONObject(((Map) javaObject)));
        }

        if(javaObject instanceof Object[]){
            return Optional.of(new JSONArray(Arrays.asList(((Object[]) javaObject))));
        }

        if(javaObject instanceof Iterator){
            return Optional.of(new JSONArray(((Iterator) javaObject)));
        }

        if(javaObject instanceof Iterable){
            return Optional.of(new JSONArray(((Iterable) javaObject).iterator()));
        }

        Gson gson = new Gson();
        //普通java 类
        String jsonStr = gson.toJson(javaObject);
        Map map = gson.fromJson(jsonStr, Map.class);

        return Optional.of(new JSONObject(map));
    }


    //********************************************************
    //              《类型转换方法》
    //
    //********************************************************

    protected Boolean castToBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if(value instanceof Boolean){
            return (Boolean) value;
        }
        if(value instanceof Number){
            return ((Number) value).intValue() == 1;
        }
        if(value instanceof String){
            return Boolean.valueOf((String)value);
        }
        throw new RuntimeException("can not cast to boolean, value : " + value);
    }

    protected byte[] castToBytes(Object value) {
        if (value == null) {
            return null;
        }
        if(value instanceof byte[]){
            return (byte[]) value;
        }
        if(value instanceof String){
            return Base64.getDecoder().decode((String) value);
        }

        throw new RuntimeException("can not cast to byte[], value : " + value);
    }

    protected Byte castToByte(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).byteValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            return Byte.parseByte(strVal);
        }

        throw new RuntimeException("can not cast to byte, value : " + value);
    }

    protected Short castToShort(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).shortValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            return Short.parseShort(strVal);
        }

        throw new RuntimeException("can not cast to short, value : " + value);
    }

    protected Integer castToInteger(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Integer){
            return (Integer) value;
        }
        if(value instanceof Number){
            return ((Number) value).intValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            return Integer.parseInt(strVal);
        }
        if(value instanceof Boolean){
            return ((Boolean) value).booleanValue() ? 1 : 0;
        }
        throw new RuntimeException("can not cast to Integer, value : " + value);
    }

    protected Long castToLong(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).longValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            try{
                return Long.parseLong(strVal);
            } catch(NumberFormatException ex){
                //
            }
        }
        throw new RuntimeException("can not cast to long, value : " + value);
    }

    protected Float castToFloat(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).floatValue();
        }
        if(value instanceof String){
            String strVal = value.toString();
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            return Float.parseFloat(strVal);
        }
        throw new RuntimeException("can not cast to float, value : " + value);
    }

    protected Double castToDouble(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).doubleValue();
        }
        if(value instanceof String){
            String strVal = value.toString();
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != 0){
                strVal = strVal.replaceAll(",", "");
            }
            return Double.parseDouble(strVal);
        }
        throw new RuntimeException("can not cast to double, value : " + value);
    }

    protected BigDecimal castToBigDecimal(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof BigDecimal){
            return (BigDecimal) value;
        }
        if(value instanceof BigInteger){
            return new BigDecimal((BigInteger) value);
        }
        String strVal = value.toString();
        if(strVal.length() == 0){
            return null;
        }
        if(value instanceof Map && ((Map) value).size() == 0){
            return null;
        }
        return new BigDecimal(strVal);
    }

    protected BigInteger castToBigInteger(Object value) {
        if(value == null){
            return null;
        }
        if(value instanceof BigInteger){
            return (BigInteger) value;
        }
        if(value instanceof Float || value instanceof Double){
            return BigInteger.valueOf(((Number) value).longValue());
        }
        String strVal = value.toString();
        if(strVal.length() == 0 //
                || "null".equals(strVal) //
                || "NULL".equals(strVal)){
            return null;
        }
        return new BigInteger(strVal);
    }

    protected String castToString(Object value){
        if(value==null){
            return null;
        }
        return value.toString();
    }

    protected Date castToDate(Object value){
        throw new UnsupportedOperationException("未实现");
    }

    protected java.sql.Date castToSqlDate(Object value){
        throw new UnsupportedOperationException("未实现");
    }

    protected Timestamp castToTimestamp(Object value){
        throw new UnsupportedOperationException("未实现");
    }


}

