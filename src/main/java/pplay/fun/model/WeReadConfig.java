package pplay.fun.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "weread.pplay.fun",
    version = "v1alpha1",
    kind = "wereadconfig",
    plural = "wereadconfigs",
    singular = "wereadconfig")

public class WeReadConfig extends AbstractExtension {
    @Schema(description = "微信读书配置规格")
    private WeReadConfigSpec spec;
    @Data
    public static class WeReadConfigSpec {
        @Schema(description = "微信读书cookie")
        private String cookie;
    }
}
