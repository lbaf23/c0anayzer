package c0anayzer.error;

import java.util.ArrayList;
import java.util.List;

import c0anayzer.tokenizer.Token;
import c0anayzer.tokenizer.TokenType;
import c0anayzer.util.Pos;

public class ExpectedTokenError extends CompileError {
    private static final long serialVersionUID = 1L;

    List<TokenType> expecTokenType;
    Token token;

    @Override
    public ErrorCode getErr() {
        return ErrorCode.ExpectedToken;
    }

    @Override
    public Pos getPos() {
        return token.getStartPos();
    }

    /**
     * @param expectedTokenType
     * @param token
     */
    public ExpectedTokenError(TokenType expectedTokenType, Token token) {
        this.expecTokenType = new ArrayList<>();
        this.expecTokenType.add(expectedTokenType);
        this.token = token;
    }

    /**
     * @param expectedTokenType
     * @param token
     */
    public ExpectedTokenError(List<TokenType> expectedTokenType, Token token) {
        this.expecTokenType = expectedTokenType;
        this.token = token;
    }

    @Override
    public String toString() {
        return "Analyse error. Expected " + expecTokenType + " at " +
                token.getStartPos() + ", got: " + token.toStringAlt();
    }
}
