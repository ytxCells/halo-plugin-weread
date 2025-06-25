package pplay.fun.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pplay.fun.model.WeReadConfig;
import pplay.fun.service.WeReadConfigService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WeReadApiClient {
    private static final String BASE_URL = "https://weread.qq.com";
    private static final String I_BASE_URL = "https://i.weread.qq.com";
    private static final int MAX_RETRY = 2; // 最大重试次数

    private final HttpClient httpClient;
    private volatile String cookie; // 使用 volatile 确保线程可见性
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false); // 防止并发刷新
    private final ObjectMapper objectMapper;
    private WeReadConfigService weReadConfigService; // 改为字段声明

    // 添加 setter 方法 todo 不推荐用此方法解决循环依赖的问题，我临时用一下
    @Autowired
    public void setWeReadConfigService(WeReadConfigService weReadConfigService) {
        this.weReadConfigService = weReadConfigService;
    }
    @Autowired
    public WeReadApiClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = objectMapper;
        this.cookie = null; // 保持延迟初始化
    }

    // 新增的测试用构造函数
    public WeReadApiClient(String cookie) {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.cookie = cookie;
    }

    public void updateCookie(String newCookie) {
        this.cookie = newCookie;
        log.info("Cookie updated");
    }

    private void checkCookie() throws Exception {
        if (cookie == null || cookie.isEmpty()) {
            throw new IllegalStateException("WeRead cookie not initialized");
        }
    }

    // 新增: Cookie 刷新方法
    public synchronized void refreshCookie() throws Exception {
        if (isRefreshing.get()) {
            log.info("Cookie refresh already in progress");
            return;
        }

        try {
            isRefreshing.set(true);
            log.info("Refreshing WeRead cookie...");

            HttpHead request = new HttpHead(BASE_URL);
            addCommonHeaders(request);

            HttpResponse response = httpClient.execute(request);
            String setCookieHeader = response.getFirstHeader("Set-Cookie").getValue();

            if (setCookieHeader == null || setCookieHeader.isEmpty()) {
                log.error("Failed to refresh cookie: No Set-Cookie header found");
                throw new Exception("Cookie refresh failed");
            }

            // 解析并合并新 Cookie
            String newCookie = parseAndMergeCookies(setCookieHeader);
            WeReadConfig weReadConfig = new WeReadConfig();
            weReadConfig.getSpec().setCookie(newCookie);
            weReadConfigService.PutWeReadConfig("weread-config",weReadConfig);
            log.info("Cookie refreshed successfully");
        } finally {
            isRefreshing.set(false);
        }
    }

    // 解析并合并 Cookie
    private String parseAndMergeCookies(String setCookieHeader) {
        // 解析 Set-Cookie 头
        List<String> newCookies = Arrays.stream(setCookieHeader.split(";\\s*"))
            .filter(c -> c.contains("="))
            .map(c -> c.split("=")[0] + "=" + c.split("=")[1])
            .collect(Collectors.toList());

        // 合并新旧 Cookie
        Map<String, String> cookieMap = Arrays.stream(this.cookie.split("; "))
            .map(c -> c.split("=", 2))
            .filter(parts -> parts.length == 2)
            .collect(Collectors.toMap(
                parts -> parts[0],
                parts -> parts[1]
            ));

        newCookies.forEach(c -> {
            String[] parts = c.split("=", 2);
            if (parts.length == 2) {
                cookieMap.put(parts[0], parts[1]);
            }
        });

        // 重建 Cookie 字符串
        return cookieMap.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("; "));
    }

    // 处理错误响应
    private boolean handleErrorResponse(JsonNode responseNode, String operation) throws Exception {
        if (responseNode.has("errcode")) {
            int errcode = responseNode.get("errcode").asInt();

            if (errcode == -2012) { // 微信读书特定的登录超时错误码
                log.warn("WeRead cookie expired (errcode: -2012). Refreshing...");
                refreshCookie();
                return true; // 需要重试
            }

            log.error("{} failed with error code: {}", operation, errcode);
            throw new Exception(operation + " failed with error code: " + errcode);
        }
        return false;
    }

    /**
     * 1. 获取用户书架信息 (带重试机制)
     */
    public JsonNode getBookshelf() throws Exception {
        return executeWithRetry(() -> executeGetRequest(BASE_URL + "/web/shelf/sync"), "getBookshelf");
    }

    /**
     * 2. 获取有笔记的书籍清单 (带重试机制)
     */
    public JsonNode getNotebooks() throws Exception {
        return executeWithRetry(() -> executeGetRequest(BASE_URL + "/api/user/notebook"), "getNotebooks");
    }

    /**
     * 3. 获取书籍详情 (带重试机制)
     */
    public JsonNode getBookInfo(String bookId) throws Exception {
        return executeWithRetry(() -> executeGetRequest(BASE_URL + "/api/book/info?bookId=" + bookId), "getBookInfo");
    }

    /**
     * 4. 获取书籍章节信息 (带重试机制)
     */
    public JsonNode getChapterInfos(String bookId) throws Exception {
        return executeWithRetry(() -> {
            String url = BASE_URL + "/web/book/chapterInfos";
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode bookIds = requestBody.putArray("bookIds");
            bookIds.add(bookId);
            return executePostRequest(url, requestBody.toString());
        }, "getChapterInfos");
    }

    /**
     * 5. 获取阅读状态信息 (带重试机制)
     */
    public JsonNode getReadingInfo(String bookId) throws Exception {
        return executeWithRetry(() -> executeGetRequest(BASE_URL + "/web/book/getProgress?bookId=" + bookId), "getReadingInfo");
    }

    /**
     * 6. 获取热门评论 (带重试机制)
     */
    public JsonNode getBestReviews(String bookId, int count) throws Exception {
        return executeWithRetry(() -> {
            String url = BASE_URL + "/web/review/list/best?bookId=" + bookId +
                "&synckey=0&maxIdx=0&count=" + count;
            return executeGetRequest(url);
        }, "getBestReviews");
    }

    /**
     * 7. 获取划线记录 (带重试机制)
     */
    public JsonNode getBookmarks(String bookId) throws Exception {
        return executeWithRetry(() -> executeGetRequest(BASE_URL + "/web/book/bookmarklist?bookId=" + bookId), "getBookmarks");
    }

    /**
     * 8. 获取个人笔记 (带重试机制)
     */
    public JsonNode getPersonalReviews(String bookId) throws Exception {
        return executeWithRetry(() -> {
            String url = I_BASE_URL + "/review/list?bookId=" + bookId +
                "&listType=11&mine=1&synckey=0";
            return executeGetRequest(url);
        }, "getPersonalReviews");
    }

    /**
     * 9. 获取阅读历史 (带重试机制)
     */
    public JsonNode getReadingHistory() throws Exception {
        return executeWithRetry(() -> executeGetRequest(I_BASE_URL + "/readdata/summary?synckey=0"), "getReadingHistory");
    }

    // 带重试的执行逻辑
    private JsonNode executeWithRetry(RequestExecutor executor, String operation) throws Exception {
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            try {
                JsonNode result = executor.execute();

                // 检查错误响应
                if (handleErrorResponse(result, operation)) {
                    attempt++;
                    log.info("Retrying {} after cookie refresh (attempt {})", operation, attempt);
                    continue;
                }

                return result;
            } catch (Exception e) {
                if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                    log.warn("Authentication error detected, refreshing cookie...");
                    refreshCookie();
                    attempt++;
                    log.info("Retrying {} after cookie refresh (attempt {})", operation, attempt);
                } else {
                    throw e;
                }
            }
        }
        throw new Exception(operation + " failed after " + MAX_RETRY + " attempts");
    }

    // 函数式接口用于重试逻辑
    @FunctionalInterface
    private interface RequestExecutor {
        JsonNode execute() throws Exception;
    }

    private JsonNode executeGetRequest(String url) throws Exception {
        checkCookie();
        HttpGet request = new HttpGet(url);
        addCommonHeaders(request);

        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonNode result = objectMapper.readTree(responseBody);

        // 检查 HTTP 状态码
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
            throw new Exception("Unauthorized access (HTTP 401)");
        }

        return result;
    }

    private JsonNode executePostRequest(String url, String jsonBody) throws Exception {
        checkCookie();
        HttpPost request = new HttpPost(url);
        addCommonHeaders(request);
        request.setHeader("Content-Type", "application/json;charset=UTF-8");
        request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonNode result = objectMapper.readTree(responseBody);

        // 检查 HTTP 状态码
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
            throw new Exception("Unauthorized access (HTTP 401)");
        }

        return result;
    }

    private void addCommonHeaders(org.apache.http.HttpMessage message) {
        message.setHeader("Cookie", cookie);
        message.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        message.setHeader("Referer", "https://weread.qq.com/");
    }

    public static void main(String[] args) {
        // 从环境变量获取Cookie
        String cookie = "";
        if (cookie == null || cookie.isEmpty()) {
            System.err.println("请设置环境变量 WEREAD_COOKIE");
            return;
        }

        WeReadApiClient client = new WeReadApiClient(cookie);

        try {
            // 1. 获取用户书架
            JsonNode bookshelf = client.getBookshelf();
            int bookCount = bookshelf.get("bookCount").asInt();
            System.out.println("书架书籍数量: " + bookCount);

            // 2. 获取有笔记的书籍
            JsonNode notebooks = client.getNotebooks();
            int notebookCount = notebooks.get("books").size();
            System.out.println("有笔记的书籍数量: " + notebookCount);

            // 3. 测试书籍ID（《读透王阳明：心学教你内心强大的智慧》）
            String testBookId = "821337";

            // 4. 获取书籍详情
            JsonNode bookInfo = client.getBookInfo(testBookId);
            System.out.println("书籍标题: " + bookInfo.get("title").asText());

            // 5. 获取章节信息
            JsonNode chapters = client.getChapterInfos(testBookId);
            JsonNode chapterList = chapters.get("data").get(0).get("updated");
            System.out.println("章节数量: " + chapterList.size());

            // 6. 获取阅读状态
            JsonNode readingInfo = client.getReadingInfo(testBookId);
            int progress = readingInfo.get("book").get("progress").asInt();
            System.out.println("阅读进度: " + progress + "%");

            // 7. 获取热门评论
            JsonNode bestReviews = client.getBestReviews(testBookId, 3);
            System.out.println("热门评论数量: " + bestReviews.get("reviews").size());

            // 8. 尝试获取划线记录
            try {
                JsonNode bookmarks = client.getBookmarks(testBookId);
                if (bookmarks.has("updated")) {
                    System.out.println("划线数量: " + bookmarks.get("updated").size());
                } else if (bookmarks.has("errcode")) {
                    System.out.println("获取划线失败，错误码: " + bookmarks.get("errcode").asInt());
                }
            } catch (Exception e) {
                System.out.println("获取划线记录异常: " + e.getMessage());
            }

            // 9. 尝试获取个人笔记
            try {
                JsonNode personalReviews = client.getPersonalReviews(testBookId);
                if (personalReviews.has("reviews")) {
                    System.out.println("个人笔记数量: " + personalReviews.get("reviews").size());
                } else if (personalReviews.has("errcode")) {
                    System.out.println("获取个人笔记失败，错误码: " + personalReviews.get("errcode").asInt());
                }
            } catch (Exception e) {
                System.out.println("获取个人笔记异常: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("API调用失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}