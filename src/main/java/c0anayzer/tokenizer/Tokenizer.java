package c0anayzer.tokenizer;

import c0anayzer.error.TokenizeError;
import c0anayzer.error.ErrorCode;
import c0anayzer.util.Pos;

import java.util.HashMap;

public class Tokenizer {
    private static HashMap<String, TokenType> keyWords = new HashMap<>();
    private static HashMap<String, TokenType> varTypes = new HashMap<>();
    static{
        keyWords.put("fn",TokenType.FN);
        keyWords.put("let",TokenType.LET);
        keyWords.put("const",TokenType.CONST);
        keyWords.put("as",TokenType.AS);
        keyWords.put("while",TokenType.WHILE);
        keyWords.put("if",TokenType.IF);
        keyWords.put("else",TokenType.ELSE);
        keyWords.put("return",TokenType.RETURN);
        keyWords.put("break",TokenType.BREAK);
        keyWords.put("continue",TokenType.CONTINUE);

        varTypes.put("void",TokenType.VOID);
        varTypes.put("int",TokenType.INT);
        varTypes.put("double",TokenType.DOUBLE);
    }

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexVar();
        } else if (Character.isAlphabetic(peek) || peek == '_') {
            return lexIdentOrKeyword();
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexVar() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值

        StringBuilder str = new StringBuilder("");
        Pos begin = it.currentPos();
        boolean isDouble = false;
        boolean hasP = true;
        while (!it.isEOF() &&
                (Character.isDigit(it.peekChar()) || it.peekChar()=='.' || it.peekChar()=='e' || it.peekChar()=='E' ) ) {
            char c = it.nextChar();

            if(c == 'E' || c == 'e'){
                if(it.peekChar() == '-' || it.peekChar() == '+'){
                    str.append(c);
                    c = it.nextChar();
                }
            }
            else if(c == '.'){
                if(isDouble)
                    throw new TokenizeError(ErrorCode.InvalidIdentifier, begin);
                isDouble = true;
                hasP = false;
            }
            else
                hasP = true;

            str.append(c);
        }


        if(isDouble) {
            if(!hasP){
                throw new TokenizeError(ErrorCode.InvalidIdentifier, begin);
            }
            return new Token(TokenType.DoubleVar, Double.parseDouble(str.toString()), begin, it.currentPos());
        }
        else {
            return new Token(TokenType.Uint, Long.parseLong(str.toString()), begin, it.currentPos());
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
        StringBuilder str = new StringBuilder("");
        Pos begin = it.currentPos();
        while (!it.isEOF() &&
                (Character.isLetter(it.peekChar()) || Character.isDigit(it.peekChar()) || it.peekChar() == '_') ) {
            str.append(it.nextChar());
        }

        TokenType tt = keyWords.get(str.toString());
        if(tt!=null)
            return new Token(tt, str.toString(), begin, it.currentPos());
        TokenType tp = varTypes.get(str.toString());
        if(tp!=null)
            return new Token(tp, str.toString(), begin, it.currentPos());
        return new Token(TokenType.Ident, str.toString(), begin, it.currentPos());
    }


    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                if(it.peekChar()=='>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                else {
                    return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
                }

            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                // 填入返回语句
                if(it.peekChar()=='/') {
                    while (!it.isEOF() && it.nextChar()!='\n');
                    return this.nextToken();
                }
                else {
                    return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
                }

            case '=':
                // 填入返回语句
                if(it.peekChar()=='='){
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                else{
                    return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());
                }
            case '(':
                // 填入返回语句
                return new Token(TokenType.L_PARENT, '(', it.previousPos(), it.currentPos());
            case ')':
                // 填入返回语句
                return new Token(TokenType.R_PARENT, ')', it.previousPos(), it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());
            case '>':
                char c = it.peekChar();
                if(c=='='){
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                else {
                    return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());
                }
            case '<':
                c = it.peekChar();
                if(c=='='){
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                else {
                    return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());
                }
            case '!':
                c = it.peekChar();
                if(c=='='){
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            case ':':
                return new Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());
            case '\'':
                char cn = it.nextChar(), cv;
                if(cn == '\\'){
                    cv = it.nextChar();
                    if(it.nextChar() == '\''){
                        switch (cv) {
                            case '\'':
                                return new Token(TokenType.CharVar, (int)('\''), it.previousPos(), it.currentPos());
                            case '"':
                                return new Token(TokenType.CharVar, (int)'"', it.previousPos(), it.currentPos());
                            case '\\':
                                return new Token(TokenType.CharVar, (int)'\\', it.previousPos(), it.currentPos());
                            case 'n':
                                return new Token(TokenType.CharVar, (int)'\n', it.previousPos(), it.currentPos());
                            case 't':
                                return new Token(TokenType.CharVar, (int)'\t', it.previousPos(), it.currentPos());
                            case 'r':
                                return new Token(TokenType.CharVar, (int)'\r', it.previousPos(), it.currentPos());
                            default:
                                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                        }
                    }
                    else{
                        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                    }
                }
                else {
                    if(it.nextChar() == '\''){
                        return new Token(TokenType.CharVar, (int)cn, it.previousPos(), it.currentPos());
                    }
                    else{
                        throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                    }
                }
            case '\"':
                StringBuilder str = new StringBuilder();
                while(!it.isEOF() && it.peekChar() != '\"'){
                    char now;
                    if(it.peekChar()=='\\'){
                        it.nextChar();
                        switch (it.nextChar()){
                            case 'r':
                                now = '\r';
                                break;
                            case 'n':
                                now = '\n';
                                break;
                            case '\\':
                                now = '\\';
                                break;
                            case '"':
                                now = '"';
                                break;
                            case '\'':
                                now = '\'';
                                break;
                            case 't':
                                now = '\t';
                                break;
                            default:
                                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                        }
                    }
                    else{
                        now = it.nextChar();
                    }
                    str.append(now);
                }
                if(it.nextChar()=='\"') {
                    return new Token(TokenType.StringVar, str.toString(), it.previousPos(), it.currentPos());
                }
                else {
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
                }
            default:
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
