package pplay.fun.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class WeReadApiTest {
    private static final String BASE_URL = "https://weread.qq.com";
    private static final String I_BASE_URL = "https://i.weread.qq.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient;
    private final String cookie;

    public WeReadApiTest(String cookie) {
        this.httpClient = HttpClients.createDefault();
        this.cookie = cookie;
    }

    /**
     * 测试所有API接口的可用性
     */
    public void testAllApis(String bookId) {
        System.out.println("=== 微信读书API接口可用性测试 ===");
        System.out.println("使用书籍ID: " + bookId);
        System.out.println();

        // 测试书架API
        testApi("获取用户书架信息",
            BASE_URL + "/web/shelf/sync",
            "GET",
            response -> response.has("bookCount") && response.has("books"));

        // 测试笔记本API
        testApi("获取笔记书籍清单",
            BASE_URL + "/api/user/notebook",
            "GET",
            response -> response.has("books"));

        // 测试书籍详情API
        testApi("获取书籍详情",
            BASE_URL + "/api/book/info?bookId=" + bookId,
            "GET",
            response -> response.has("title") && response.has("author"));

        // 测试章节信息API
        testApi("获取章节信息",
            BASE_URL + "/web/book/chapterInfos",
            "POST",
            response -> {
                if (response.has("data")) {
                    JsonNode data = response.get("data");
                    return data.isArray() && data.size() > 0 && data.get(0).has("updated");
                }
                return false;
            });

        // 测试阅读状态API
        testApi("获取阅读状态",
            BASE_URL + "/web/book/getProgress?bookId=" + bookId,
            "GET",
            response -> response.has("book") && response.get("book").has("progress"));

        // 测试热门评论API
        testApi("获取热门评论",
            BASE_URL + "/web/review/list/best?bookId=" + bookId + "&synckey=0&maxIdx=0&count=3",
            "GET",
            response -> response.has("reviews"));

        // 测试划线记录API
        testApi("获取划线记录",
            BASE_URL + "/web/book/bookmarklist?bookId=" + bookId,
            "GET",
            response -> response.has("updated") || response.has("errcode"));

        // 测试个人笔记API
        testApi("获取个人笔记",
            I_BASE_URL + "/review/list?bookId=" + bookId + "&listType=11&mine=1&synckey=0",
            "GET",
            response -> response.has("reviews") || response.has("errcode"));
        testApi("获取个人笔记",
            BASE_URL + "api/review/list?bookId=" + bookId + "&listType=11&mine=1",
            "GET",
            response -> response.has("reviews") || response.has("errcode"));
        // 测试阅读历史API
        testApi("获取阅读历史",
            I_BASE_URL + "/readdata/summary?synckey=0",
            "GET",
            response -> response.has("totalReadTime") || response.has("errcode"));
    }

    private void testApi(String apiName, String url, String method, ApiResponseValidator validator) {
        System.out.println("[测试] " + apiName + " (" + method + " " + url + ")");

        try {
            JsonNode response;
            if ("POST".equalsIgnoreCase(method)) {
                // 构建POST请求体
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.putArray("bookIds").add("821337"); // 使用固定书籍ID

                response = executePostRequest(url, requestBody.toString());
            } else {
                response = executeGetRequest(url);
            }

            // 检查错误码
            if (response.has("errcode")) {
                int errcode = response.get("errcode").asInt();
                System.out.println("❌ 失败 - 错误码: " + errcode);
                handleErrorCode(errcode);
            } else if (validator.isValid(response)) {
                System.out.println("✅ 成功");
            } else {
                System.out.println("⚠️ 失败 - 响应结构异常");
                System.out.println("响应内容: " + response.toString());
            }
        } catch (Exception e) {
            System.out.println("❌ 失败 - 请求异常: " + e.getMessage());
        }

        System.out.println();
    }

    private void handleErrorCode(int errcode) {
        if (errcode == -2010 || errcode == -2012) {
            System.out.println("错误原因: Cookie已过期");
        } else if (errcode == -4022) {
            System.out.println("错误原因: 书籍不存在或无权限访问");
        } else if (errcode == -4020) {
            System.out.println("错误原因: 请求参数错误");
        } else {
            System.out.println("错误原因: 未知错误");
        }
    }

    private JsonNode executeGetRequest(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        addCommonHeaders(request);

        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        return objectMapper.readTree(responseBody);
    }

    private JsonNode executePostRequest(String url, String jsonBody) throws Exception {
        HttpPost request = new HttpPost(url);
        addCommonHeaders(request);
        request.setHeader("Content-Type", "application/json;charset=UTF-8");
        request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        return objectMapper.readTree(responseBody);
    }

    private void addCommonHeaders(org.apache.http.HttpMessage message) {
        message.setHeader("Cookie", cookie);
        message.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        message.setHeader("Referer", "https://weread.qq.com/");
        message.setHeader("Accept", "application/json, text/plain, */*");
        message.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
    }

    @FunctionalInterface
    private interface ApiResponseValidator {
        boolean isValid(JsonNode response);
    }

    public static void main(String[] args) {

        String cookie = "";
        if (cookie == null || cookie.isEmpty()) {
            System.err.println("请设置环境变量 WEREAD_COOKIE");
            return;
        }

        WeReadApiTest tester = new WeReadApiTest(cookie);

        // 使用测试书籍ID（《读透王阳明：心学教你内心强大的智慧》）
        String testBookId = "821337";

        // 测试所有API
        tester.testAllApis(testBookId);
    }
}