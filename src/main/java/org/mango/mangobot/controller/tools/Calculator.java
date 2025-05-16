package org.mango.mangobot.controller.tools;

import dev.langchain4j.agent.tool.Tool;

class Calculator {
    
    @Tool
    double add(int a, int b) {
        return a + b;
    }

    @Tool
    double squareRoot(double x) {
        return Math.sqrt(x);
    }
}