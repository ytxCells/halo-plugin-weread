package run.halo.tutorial;

import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class TodoListPlugin extends BasePlugin {
    private final SchemeManager schemeManager;

    public TodoListPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        // 插件启动时注册自定义模型
        schemeManager.register(Todo.class);
        System.out.println("Hello world 插件启动了!");
    }

    @Override
    public void stop() {
        // 插件停用时取消注册自定义模型
        Scheme todoScheme = schemeManager.get(Todo.class);
        schemeManager.unregister(todoScheme);
        System.out.println("Hello world 被停止!");
    }
}
