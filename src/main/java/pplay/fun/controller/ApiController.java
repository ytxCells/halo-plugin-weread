package pplay.fun.controller;

import pplay.fun.service.WeReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.plugin.ApiVersion;

@ApiVersion("v1alpha1")
@RequestMapping("/weRead")
@RestController
@Slf4j
public class ApiController {
    private final WeReadService weReadService;

    public ApiController(WeReadService weReadService) {
        this.weReadService = weReadService;
    }
    @PostMapping("/synchronizationWeRead")
    public void synchronizationWeRead(){
        weReadService.synchronizationWeRead();
    }
}
