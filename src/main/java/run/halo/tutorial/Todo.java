package run.halo.tutorial;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "Todo", group = "todo.plugin.halo.run",
    version = "v1alpha1", singular = "todo", plural = "todos")
public class Todo extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private TodoSpec spec;

    @Data
    public static class TodoSpec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String title;

        @Schema(defaultValue = "false")
        private Boolean done;
    }
}
