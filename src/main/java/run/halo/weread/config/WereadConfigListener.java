package run.halo.weread.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.PluginConfigUpdatedEvent;

/**
 * 监听配置变更
 */
@Component
public class WereadConfigListener {
    @EventListener
    public void onConfigUpdated(PluginConfigUpdatedEvent event) {
        if (event.getNewConfig().containsKey(WereadConfig.GROUP)) {
            // do something
        }
    }
}
