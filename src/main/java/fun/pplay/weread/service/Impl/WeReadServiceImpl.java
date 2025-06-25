package fun.pplay.weread.service.Impl;
import com.fasterxml.jackson.databind.JsonNode;
import fun.pplay.weread.service.WeReadService;
import fun.pplay.weread.util.WeReadApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class WeReadServiceImpl implements WeReadService {
    private final WeReadApiClient weReadApiClient;

    // 构造函数注入 WeReadApiClient
    public WeReadServiceImpl(WeReadApiClient weReadApiClient) {
        this.weReadApiClient = weReadApiClient;
    }
    @Override
    public void synchronizationWeRead(){
        try {
            // 示例：调用 API 方法
            JsonNode bookshelf = weReadApiClient.getBookshelf();
            int bookCount = bookshelf.get("bookCount").asInt();
            log.info("书架书籍数量: {}", bookCount);

            // 后续可添加其他 API 调用逻辑
        } catch (Exception e) {
            log.error("同步微信读书数据失败", e);
        }
    }
    public void addWeRead(String cookie){

    }
}
