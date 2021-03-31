package com.orm.demo.controller;

import com.orm.demo.dynamic.BaseController;
import com.orm.demo.repository.Demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class SysUserController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(SysUserController.class);


    @Autowired
    public Demo demo;


    @RequestMapping("/insertData")
    public String insertData() throws IOException {
        demo.insertData("{\n" +
                "  \"pageNo\": 1,\n" +
                "  \"pageSize\": 10\n" +
                "}");
        return "success ";
    }





}
