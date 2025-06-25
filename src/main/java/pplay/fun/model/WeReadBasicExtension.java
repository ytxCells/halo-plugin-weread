package pplay.fun.model;

import lombok.Data;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@GVK(group = "weread.pplay.fun",
    version = "v1alpha1",
    kind = "WeReadBasic",
    plural = "wereadbasics",
    singular = "wereadbasics")
public class WeReadBasicExtension extends AbstractExtension {
}
