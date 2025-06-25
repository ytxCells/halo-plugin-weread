package pplay.fun.service;

import pplay.fun.model.WeReadConfig;
import reactor.core.publisher.Mono;

public interface WeReadConfigService {
    Mono<WeReadConfig> getWeReadConfig(String name);

    Mono<WeReadConfig> PutWeReadConfig(String name, WeReadConfig config);
}
