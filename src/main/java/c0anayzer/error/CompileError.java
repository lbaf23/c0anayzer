package c0anayzer.error;

import c0anayzer.util.Pos;

public abstract class CompileError extends Exception {

    private static final long serialVersionUID = 1L;

    public abstract ErrorCode getErr();

    public abstract Pos getPos();
}
