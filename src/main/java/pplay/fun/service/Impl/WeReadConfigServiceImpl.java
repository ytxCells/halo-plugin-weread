package pplay.fun.service.Impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pplay.fun.component.WeReadApiClient;
import pplay.fun.model.WeReadConfig;
import pplay.fun.service.WeReadConfigService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeReadConfigServiceImpl implements WeReadConfigService {
    private final ReactiveExtensionClient client;
    @Resource
    private WeReadApiClient weReadApiClient;
    @Override
    public Mono<WeReadConfig> getWeReadConfig(String name) {
        return client.fetch(WeReadConfig.class, name)
            .switchIfEmpty(Mono.defer(() -> createDefaultConfig(name)));
    }

    @Override
    public Mono<WeReadConfig> PutWeReadConfig(String name, WeReadConfig config) {
        // 确保配置名称一致
        config.getMetadata().setName(name);
        if (name.equals("weread-config")){
            weReadApiClient.updateCookie(config.getSpec().getCookie());
        }
        return client.update(config);
    }

    private Mono<WeReadConfig> createDefaultConfig(String name) {
        WeReadConfig config = new WeReadConfig();
        // 设置元数据
        Metadata metadata = new Metadata();
        metadata.setName(name);
        config.setMetadata(metadata);
        // 设置默认规格
        WeReadConfig.WeReadConfigSpec spec = new WeReadConfig.WeReadConfigSpec();
        spec.setCookie(""); // 空cookie
        config.setSpec(spec);
        log.info("创建默认微信读书配置: {}", name);
        return client.create(config);
    }
}