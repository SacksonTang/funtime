package com.rzyou.funtime.controller;

import com.rzyou.funtime.service.DailyTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("task")
public class DailyTaskController {

    @Autowired
    DailyTaskService dailyTaskService;



}
