package pplay.fun.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pplay.fun.model.WeReadConfig;
import pplay.fun.service.WeReadConfigService;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ApiVersion;

@ApiVersion("weread.pplay.fun/v1alpha1")
@RequestMapping("/config")
@RestController
@RequiredArgsConstructor
public class WeReadConfigController {
    private final WeReadConfigService weReadConfigService;
    @GetMapping("/{name}")
    public Mono<WeReadConfig> getWeReadConfig(@PathVariable("name") String name){
        return weReadConfigService.getWeReadConfig(name);
    }

    @PutMapping("/{name}")
    public Mono<WeReadConfig> PutWeReadConfig( @PathVariable("name") String name,
        @RequestBody WeReadConfig config){
        return weReadConfigService.PutWeReadConfig(name,config);
    }
}
