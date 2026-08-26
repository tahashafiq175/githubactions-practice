package com.CICDDEMO;


import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {


    @GetMapping("/get/{name}")
    public String getEmployees(@PathVariable String name){
        return "The name of the Employees is "+name;
    }
}


