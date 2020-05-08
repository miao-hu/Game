import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
    统计每个词的数量（词云图）
 */
public class WordsServlet extends HttpServlet {

    // GET 请求则调用该方法
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=utf-8");

        Map<String, Integer> map = new HashMap();   //存放内容为    词:词的数量
        JSONArray jsonArray = new JSONArray();

        try {
            //得到数据库连接
            Connection connection = DBConfig.getConnection();

            //创建 PreparedStatement 对象
            String sql = "SELECT words  FROM tangshi";             //查询所有的词
            PreparedStatement statement = connection.prepareStatement(sql);

            //返回查询结果集
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {    //当结果集中还有数据时
                String word = resultSet.getString("words");    //每一行的数据就是一首诗的所有词
                String[] arr = word.split(",");    //一首诗的所有词都放在了 arr 数组里面

                for (int i = 0; i < arr.length; i++) {
                    String s = arr[i];   //词
                    int count = 0;
                    if (map.get(s) == null) {
                        count = count + 1;
                    } else {
                        count = map.get(s) + 1;
                    }
                    map.put(s, count);
                }
            }
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                String s = e.getKey();       //词
                int count = e.getValue();    //词的数量

                JSONArray item = new JSONArray();
                item.add(s);
                item.add(count);

                jsonArray.add(item);
            }

            resp.getWriter().println(jsonArray.toJSONString());   //返回所有 词和词的数量 的 json 格式的字符串

            resultSet.close();   //关闭结果集
            statement.close();   //关闭 statement
            connection.close();  //关闭数据库连接
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject object = new JSONObject();
            object.put("error", e.getMessage());
            resp.getWriter().println(object.toJSONString());    // 返回 json 格式的错误响应
        }
    }
}
