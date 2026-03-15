package io.github.mangomaner.mangobot.agent.factory;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.coding.ShellCommandTool;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import io.github.mangomaner.mangobot.agent.hook.StreamingToolHook;
import io.github.mangomaner.mangobot.agent.manager.MemoryManager;
import io.github.mangomaner.mangobot.api.MangoModelApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentFactory {

    private final MemoryManager memoryManager;
    private final StreamingToolHook streamingToolHook;

    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "git", "mvn", "java", "javac", "python", "python3",
            "node", "npm", "yarn", "gradle", "docker", "ls", "pwd",
            "cd", "cat", "echo", "find", "grep", "head", "tail", "winver"
    );

    public ReActAgent createAgent(Integer sessionId) {
        String agentName = "MangoBot-" + sessionId;
        log.info("Creating new ReActAgent for session: {}, name: {}", sessionId, agentName);

        AutoContextMemory memory = memoryManager.getOrCreateMemory(sessionId);

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new ContextOffloadTool(memory));

        toolkit.registerTool(new ReadFileTool());
        toolkit.registerTool(new WriteFileTool());
        Function<String, Boolean> callback = this::askUserForApproval;
        toolkit.registerTool(new ShellCommandTool(ALLOWED_COMMANDS, callback));

        return ReActAgent.builder()
                .name(agentName)
                .sysPrompt("")
                .model(MangoModelApi.getMainModel())
                .memory(memory)
                .toolkit(toolkit)
                .enableMetaTool(true)
                .hooks(List.of(streamingToolHook))
                .build();
    }

    private boolean askUserForApproval(String command) {
        log.info("Requesting user approval for command: {}", command);
        return true;
    }
}
