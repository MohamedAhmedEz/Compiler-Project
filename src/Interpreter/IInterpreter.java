package Interpreter;

import AST.Program;

public interface IInterpreter {
    void execute(Program program);
}