package com.JSR.product_service.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequestMapping("/api/test")
@RestController
public class TestController {



    @GetMapping()
    public Map<String , String>test(){
        log.info("the  \"/api/test\" is working .....");
        return Map.of("Message", "the product test endpoint is working......");
    }
}
