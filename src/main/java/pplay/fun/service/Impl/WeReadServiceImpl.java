package pplay.fun.service.Impl;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import pplay.fun.service.WeReadService;
import pplay.fun.component.WeReadApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeReadServiceImpl implements WeReadService {
    @Resource
    private WeReadApiClient weReadApiClient;
    @Override
    public void synchronizationWeRead(){
        try {
            // 示例：调用 API 方法
            JsonNode bookshelf = weReadApiClient.getBookshelf();
            if (bookshelf != null && bookshelf.has("bookCount")) {
                int bookCount = bookshelf.get("bookCount").asInt();
                log.info("书架书籍数量: {}", bookCount);
            } else {
                log.warn("无法获取书籍数量，bookshelf 为 null 或缺少 'bookCount' 字段");
            }

            // 后续可添加其他 API 调用逻辑
        } catch (Exception e) {
            log.error("同步微信读书数据失败", e);
        }
    }
    public void addWeRead(String cookie){

    }
}
