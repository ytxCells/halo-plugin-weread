package run.halo.weread.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.plugin.ApiVersion;

@ApiVersion("v1alpha1")
@RequestMapping("/weread")
@RestController
@Slf4j
public class ApiController {

}
