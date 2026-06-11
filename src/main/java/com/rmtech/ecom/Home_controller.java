package com.rmtech.ecom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home_controller {

    @GetMapping("/")
    public String home() {
        return "Running";
    }


}
