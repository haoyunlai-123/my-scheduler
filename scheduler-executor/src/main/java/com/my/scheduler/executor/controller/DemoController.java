package com.my.scheduler.executor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/slow")
    public String slow(@RequestParam(defaultValue = "5000") long ms) throws InterruptedException {
        Thread.sleep(ms);
        return "SLOW OK " + ms;
    }

    @GetMapping("/fail")
    public String fail() {
        throw new RuntimeException("intentional failure");
    }
}
