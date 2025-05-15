package org.mango.mangobot.controller;

import jakarta.annotation.Resource;
import org.mango.mangobot.work.WorkFlow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Resource
    private WorkFlow workFlow;
    @GetMapping("/workFlowTest")
    public String chat(@RequestParam String question, int level) {
        return workFlow.start(question, level);
    }
}
