import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import lombok.Data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/4/30 16:37
 */
public class JSonTest {

    public static void main(String[] args) throws Exception{
        FileInputStream in = new FileInputStream("C:\\Users\\liyalei\\Desktop\\data.json");
        JSONReader reader = new JSONReader(new InputStreamReader(in));
        String str = reader.readString();
        String users = JSONObject.parseObject(str).getString("data");
        List<User> userArr = JSONArray.parseArray(users, User.class);

        List<User> supers = userArr.stream().filter(user -> user.getLeader() == null && user.getDid()!=null && user.getNumber()!=null)
                .collect(Collectors.toList());
        Map<String, List<User>> userMap = userArr.stream().map(user -> {
            if(user.getLeader()==null){
                user.setLeader("");
            }
            return user;
        }).collect(Collectors.groupingBy(User::getLeader));

        ConcurrentHashMap<String, List<User>> tmpMap = new ConcurrentHashMap<>(userMap);

        List<User> finalUser = new ArrayList<>();

        System.out.println();
    }

    public static void tree(List<User> userList, Integer puid){

    }

    @Data
    static class User{

        private String email;

        private String leader;

        private String emailQiyi;

        private Boolean admin;

        private Integer type;

        private String uid;

        private String number;

        private String puid;

        private String name;

        private String department;

        private Integer status;

        private Integer did;

        List<User> subordinates = new ArrayList<>();

    }
}
