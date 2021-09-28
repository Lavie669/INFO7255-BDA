package edu.neu.coe.info7255bda.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("plan")
public class PlanController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Plan!";
    }
}