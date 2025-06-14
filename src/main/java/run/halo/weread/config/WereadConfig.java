package run.halo.weread.config;

import lombok.Data;

@Data
public class WereadConfig {
    public static final String GROUP = "weread";
    private String cookie;

    public boolean blockSpiders() {
        return false;
    }
}
