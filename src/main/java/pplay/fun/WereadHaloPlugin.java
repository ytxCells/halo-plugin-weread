package pplay.fun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pplay.fun.model.WeReadConfig;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author ytxCells
 * @since 1.0.0
 */
@Component
public class WereadHaloPlugin extends BasePlugin {

    @Autowired
    private SchemeManager schemeManager;

    public WereadHaloPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
        schemeManager.register(WeReadConfig.class);

        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
    }
}
