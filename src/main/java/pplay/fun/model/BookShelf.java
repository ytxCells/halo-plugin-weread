package pplay.fun.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import java.time.Instant;
import java.util.List;

@GVK(group = "weread.pplay.fun",
    version = "v1alpha1",
    kind = "bookshelf",
    plural = "bookshelfs",
    singular = "bookshelf"
)
public class BookShelf extends AbstractExtension {
    private BookShelfSpec spec;
    @Data
    public static class BookShelfSpec {
        @Schema(description = "用户ID", required = true)
        private String userId;

        @Schema(description = "书籍项列表")
        private List<ShelfItem> items;
    }

    @Data
    public static class ShelfItem {
        @Schema(description = "关联书籍ID", required = true)
        private String bookName; // 指向 WeReadBook 的 metadata.name

        @Schema(description = "阅读状态: READING/FINISHED/PLAN_TO_READ")
        private String status;

        @Schema(description = "添加到书架的时间")
        private Instant addTime;
    }
}
