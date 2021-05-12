package self.robin.examples.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author mrli
 * @date 2020/8/4
 */
public class MapStringUtil {

    /**
     * @param mapString
     * @param clazz
     * @param <T> target Object
     * @return
     */
    public static <T> T convertTo(String mapString, Class<T> clazz){
        return new Gson().fromJson(convertToJson(mapString), clazz);
    }

    /**
     * @param mapString
     * @return Map Object
     */
    public static Map convertToMap(String mapString){
        String mapStr = convert(mapString);
        Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map>() {
        }.getType(), new GsonTypeAdapter()).create();
        Map map = gson.fromJson(mapStr, new TypeToken<Map>() {
        }.getType());
        return map;
    }

    /**
     * @param mapString
     * @return JsonString
     */
    public static String convertToJson(String mapString) {
        Map map = convertToMap(mapString);
        return new Gson().toJson(map);
    }

    private static String convert(String s) {

        int len = s.length();
        StringBuilder buffer = new StringBuilder(len);

        /**
         * 1.去掉空格 两端空格
         * 2.补空串 value, list
         *
         * { } [] , = "
         */

        //是上述标点符号
        boolean isSign = false;

        //buffer的最后一个元素位置
        Stack stack = new Stack();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            switch (c){
                case '}':
                    isSign = true;
                    if(backToNoneBlankChar(buffer)=='='){
                        buffer.append("\"\"");
                    }
                    fractureWrap(buffer);
                    break;
                case ']':
                    isSign = true;
                    if(backToNoneBlankChar(buffer)==','){
                        buffer.append("\"\"");
                    }
                    fractureWrap(buffer);
                    break;
                case ',':
                    isSign = true;
                    char p = backToNoneBlankChar(buffer);
                    //如果是等号，追加空串
                    if(p=='='){
                        buffer.append("\"\"");
                    }
                    else if(p==',' || p=='['){
                        buffer.append("\"\"");
                    }
                    fractureWrap(buffer);
                    break;
                case '=':
                    isSign = true;
                    backToNoneBlankChar(buffer);
                    fractureWrap(buffer);
                    break;
                case '{': case '[':
                    isSign = true;
                    fractureWrap(buffer);
                    break;
                case ' ': case '\n': case '\t':
                    if(isSign){
                        continue;
                    }
                    break;
                default:
                    isSign = false;
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    /**
     * 回退到最近一个非空字符
     * @param buffer
     */
    private static final char backToNoneBlankChar(StringBuilder buffer){
        int p = buffer.length();
        mark:
        while (--p>0){
            switch (buffer.charAt(p)){
                case ' ': case '\n': case '\t':
                    buffer.deleteCharAt(p);
                    break;
                default:
                    break mark;
            }
        }
        return buffer.charAt(buffer.length()-1);
    }

    /**
     * 包扎断裂的key或value
     */
    private static final void fractureWrap(StringBuilder buffer){
        int p = buffer.length();
        boolean isUgly = false;
        mark:
        while (--p>0){
            switch (buffer.charAt(p)){
                case ',': case '=': case '{': case '}': case '[': case ']':
                    break mark;
                case ' ': case '\n': case '\t': case '/': case '\\':
                    isUgly = true;
                    break;
                default:
            }
        }
        if(isUgly){
            buffer.insert(p+1, "\"");
            buffer.append("\"");
        }
    }


    public static class GsonTypeAdapter extends TypeAdapter<Object> {

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;

                case STRING:
                    return in.nextString();

                case NUMBER:
                    //将其作为一个字符串读取出来
                    String numberStr = in.nextString();
                    //返回的numberStr不会为null
                    if (numberStr.contains(".") || numberStr.contains("e")
                            || numberStr.contains("E")) {
                        return Double.parseDouble(numberStr);
                    }
                    return Long.parseLong(numberStr);
                case BOOLEAN:
                    return in.nextBoolean();

                case NULL:
                    in.nextNull();
                    return null;

                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            // 序列化不处理
        }

    }

    public static void main(String[] args) {

        String s = "{log_timestamp=1597301099480, ip=223.88.4.55, field={app={int_type=null, long_type=null, float_type=null, string_type=qtt}, log_id={int_type=null, long_type=null, float_type=null, string_type=b3805579b4a52f3827b21d300ddaf589|1037756}";

        String s2 = "{int_type=null, long_type=, float_type=\"2\", string_type=\"okhttp,3.10.0\"}";

        String s3 = "{log_timestamp=1597301099216, ip=223.104.35.60, field={app={int_type=null, long_type=null, float_type=null, string_type=qtt}, log_id={int_type=null, long_type=null, float_type=null, string_type=fa70ca7972b4260c6f7b38e6db1611bc|37003}, session_id={int_type=null, long_type=null, float_type=null, string_type=57966ec57fdc4c26be9ddd812848cda5}, orgtk={int_type=null, long_type=null, float_type=null, string_type=ACFXFzyldCuaCtiM4nhCHraUKERB6xYGXxo0NzUxNDk1MDg5NTIyNQ}, tuid={int_type=null, long_type=null, float_type=null, string_type=Vxc8pXQrmgrYjOJ4Qh62lA}, header_LAT={int_type=null, long_type=null, float_type=null, string_type=31.494946}, header_OC={int_type=null, long_type=null, float_type=null, string_type=29}, metric={int_type=null, long_type=null, float_type=null, string_type=301}, extra={int_type=null, long_type=null, float_type=null, string_type=refreshOnBack:true}, header_User-Agent={int_type=null, long_type=null, float_type=null, string_type=qukan_android}, topic={int_type=null, long_type=null, float_type=null, string_type=qukan_client_collect_v2}, action={int_type=null, long_type=null, float_type=null, string_type=5}, header_LON={int_type=null, long_type=null, float_type=null, string_type=118.476618}, cmd={int_type=null, long_type=null, float_type=null, string_type=1001}, header_OAID={int_type=null, long_type=null, float_type=null, string_type=ffaf8d27-13a7-4e4a-9ecd-de94432931d7}, saveTime={int_type=null, long_type=null, float_type=null, string_type=1597301099395}, orgtuid={int_type=null, long_type=null, float_type=null, string_type=}, status={int_type=null, long_type=null, float_type=null, string_type=0}}, device=null, header_sys=1, header_ver=30986000, header_vn=3.9.86.000.0710.0958, header_uuid=19232a4f334d4c399ec62903197eabb8, header_net=3g, header_ov=10, header_mo=VOG-AL00, header_ma=HUAWEI, header_dtu=014, header_mi=124877727, header_tk=ACFXFzyldCuaCtiM4nhCHraUKERB6xYGXxo0NzUxNDk1MDg5NTIyNQ, header_env=null, header_expids=null, day=2020-08-13, hour=14, cmd=1001, time_minute=2020-08-13 14:40}";

        Map map = convertToMap(s3);
        System.out.println();

    }


}
