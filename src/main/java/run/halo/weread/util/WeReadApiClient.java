package run.halo.weread.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class WeReadApiClient {
    private static final String BASE_URL = "https://weread.qq.com";
    private static final String I_BASE_URL = "https://i.weread.qq.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient;
    private final String cookie;

    public WeReadApiClient(String cookie) {
        this.httpClient = HttpClients.createDefault();
        this.cookie = cookie;
    }

    /**
     * 1. 获取用户书架信息
     */
    public JsonNode getBookshelf() throws Exception {
        return executeGetRequest(BASE_URL + "/web/shelf/sync");
    }

    /**
     * 2. 获取有笔记的书籍清单
     */
    public JsonNode getNotebooks() throws Exception {
        return executeGetRequest(BASE_URL + "/api/user/notebook");
    }

    /**
     * 3. 获取书籍详情
     */
    public JsonNode getBookInfo(String bookId) throws Exception {
        return executeGetRequest(BASE_URL + "/api/book/info?bookId=" + bookId);
    }

    /**
     * 4. 获取书籍章节信息
     */
    public JsonNode getChapterInfos(String bookId) throws Exception {
        String url = BASE_URL + "/web/book/chapterInfos";

        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode bookIds = requestBody.putArray("bookIds");
        bookIds.add(bookId);

        return executePostRequest(url, requestBody.toString());
    }

    /**
     * 5. 获取阅读状态信息
     */
    public JsonNode getReadingInfo(String bookId) throws Exception {
        return executeGetRequest(BASE_URL + "/web/book/getProgress?bookId=" + bookId);
    }

    /**
     * 6. 获取热门评论
     */
    public JsonNode getBestReviews(String bookId, int count) throws Exception {
        String url = BASE_URL + "/web/review/list/best?bookId=" + bookId +
                "&synckey=0&maxIdx=0&count=" + count;
        return executeGetRequest(url);
    }

    /**
     * 7. 获取划线记录（可能不可用）
     */
    public JsonNode getBookmarks(String bookId) throws Exception {
        return executeGetRequest(BASE_URL + "/web/book/bookmarklist?bookId=" + bookId);
    }

    /**
     * 8. 获取个人笔记（可能不可用）
     */
    public JsonNode getPersonalReviews(String bookId) throws Exception {
        String url = I_BASE_URL + "/review/list?bookId=" + bookId +
                "&listType=11&mine=1&synckey=0";
        return executeGetRequest(url);
    }

    /**
     * 9. 获取阅读历史（可能不可用）
     */
    public JsonNode getReadingHistory() throws Exception {
        return executeGetRequest(I_BASE_URL + "/readdata/summary?synckey=0");
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