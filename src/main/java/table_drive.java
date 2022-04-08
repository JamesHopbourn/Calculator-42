import com.alibaba.fastjson.util.Function;

import java.util.HashMap;

public class table_drive {
    public static void main(String[] args) {
        HashMap<String, Function<String, String>> map = new HashMap<>();

        map.put("two", String::toLowerCase);
        map.put("three", new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.toUpperCase();
            }
        });

        String apply = map.get("three").apply("abc");
        System.out.println(apply);
    }

    private static Function<String, String> hello(String j) {
        System.out.println(j);
        return null;
    }
}







/*
    private static final Map<String, Consumer> action = new HashMap<>();
    static {
        action.put("红色", o -> myFunction());
        action.put("绿色", o -> System.out.println("现在是绿灯，请通行"));
        action.put("黄色", o -> System.out.println("现在是黄灯，请注意"));
    }
    public static void myFunction(){
        System.out.println("OK");
    }
    public static void actionMap(String trafficLight) {
        // 不考虑非法值，只说明方向
        action.get(trafficLight).accept(null);
    }
 */