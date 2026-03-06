package org.example.mcp;

import dev.langchain4j.agent.tool.Tool;

public class CalculatorService {

    @Tool("Calculates the sum of two numbers")
    public double add(double a, double b) {
        return a + b;
    }

    @Tool("Calculates the product of two numbers")
    public double multiply(double a, double b) {
        return a * b;
    }
}
