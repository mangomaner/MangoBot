package org.mango.mangobot.manager.websocketReverseProxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Slf4j
public class BotStartupRunner implements CommandLineRunner {

    private final BotMessageHandler botMessageHandler;

    @Value("${server.port}")
    private String port;

    public BotStartupRunner(BotMessageHandler botMessageHandler) {
        this.botMessageHandler = botMessageHandler;
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);

//        System.out.print("输入群号: ");
//        String groupId = scanner.nextLine();
//        botMessageHandler.setGroupId(groupId);
//
//        botMessageHandler.startInputLoop();

        log.info("WebSocket 服务器已启动，监听 ws://localhost:" + port);
    }
}