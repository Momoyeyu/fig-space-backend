package ai.momoyeyu.figspace.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class HiController {

    @GetMapping("/hi")
    public String sayHi() {
        return "This is a test message for cas-med-prediction";
    }
}
