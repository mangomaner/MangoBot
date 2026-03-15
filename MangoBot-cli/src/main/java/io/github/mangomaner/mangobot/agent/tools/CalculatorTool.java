package io.github.mangomaner.mangobot.agent.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.annotation.MangoTool;

@MangoTool(name = "CalculatorTool", description = "计算器工具，用于执行基本数学运算", category = "SYSTEM")
public class CalculatorTool {

    @Tool(description = "执行加法运算")
    public double add(
            @ToolParam(name = "a", description = "第一个数") double a,
            @ToolParam(name = "b", description = "第二个数") double b) {
        return a + b;
    }

    @Tool(description = "执行减法运算")
    public double subtract(
            @ToolParam(name = "a", description = "被减数") double a,
            @ToolParam(name = "b", description = "减数") double b) {
        return a - b;
    }

    @Tool(description = "执行乘法运算")
    public double multiply(
            @ToolParam(name = "a", description = "第一个数") double a,
            @ToolParam(name = "b", description = "第二个数") double b) {
        return a * b;
    }

    @Tool(description = "执行除法运算")
    public double divide(
            @ToolParam(name = "a", description = "被除数") double a,
            @ToolParam(name = "b", description = "除数") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return a / b;
    }

    @Tool(description = "计算幂运算")
    public double power(
            @ToolParam(name = "base", description = "底数") double base,
            @ToolParam(name = "exponent", description = "指数") double exponent) {
        return Math.pow(base, exponent);
    }

    @Tool(description = "计算平方根")
    public double sqrt(@ToolParam(name = "number", description = "要计算平方根的数") double number) {
        if (number < 0) {
            throw new IllegalArgumentException("不能对负数计算平方根");
        }
        return Math.sqrt(number);
    }

    @Tool(description = "计算百分比")
    public double percentage(
            @ToolParam(name = "value", description = "数值") double value,
            @ToolParam(name = "percent", description = "百分比") double percent) {
        return value * percent / 100;
    }
}
