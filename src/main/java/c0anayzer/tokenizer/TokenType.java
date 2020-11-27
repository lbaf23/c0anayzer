package c0anayzer.tokenizer;

public enum TokenType {
    /** 空 */
    None,
    /** 无符号整数 */
    Uint,
    DoubleVar,
    /** 标识符 */
    Ident,

    /* void */
    VOID,
    INT,
    DOUBLE,

    // 关键字

    /** fn */
    FN,
    /** let */
    LET,
    /** const */
    CONST,
    /** as */
    AS,
    /** while */
    WHILE,
    /** if */
    IF,
    /** else */
    ELSE,
    /* return */
    /* break */
    RETURN,
    BREAK,
    /** continue */
    CONTINUE,

    // 符号

    /** 加号+ */
    PLUS,
    /** 减号- */
    MINUS,
    /** 乘号* */
    MUL,
    /** 除号/ */
    DIV,
    /** 等号= */
    ASSIGN,
    /** == */
    EQ,
    /** != */
    NEQ,
    /** < */
    LT,
    /** > */
    GT,
    /** <= */
    LE,
    /** >= */
    GE,
    /** 左括号 */
    L_PARENT,
    /** 右括号 */
    R_PARENT,
    /* { */
    L_BRACE,
    /* } */
    R_BRACE,
    /* -> */
    ARROW,
    /* , */
    COMMA,
    /* : */
    COLON,
    /** 分号 */
    SEMICOLON,
    /* 字符串 */
    StringVar,
    /* 字符 */
    CharVar,



    /** 文件尾 */
    EOF;

    @Override
    public String toString() {
        switch (this) {
            case INT:
                return "INT";
            case DOUBLE:
                return "DOUBLE";
            case VOID:
                return "VOID";
            case None:
                return "NullToken";
            case FN:
                return "FNToken";
            case LET:
                return "LETToken";
            case CONST:
                return "CONSTToken";
            case AS:
                return "ASToken";
            case WHILE:
                return "WHILEToken";
            case IF:
                return "IFToken";
            case ELSE:
                return "ELSEToken";
            case RETURN:
                return "RETURNToken";
            case BREAK:
                return "BREAKToken";
            case CONTINUE:
                return "CONTINUEToken";
            case PLUS:
                return "PLUSToken";
            case MINUS:
                return "MINUSToken";
            case MUL:
                return "MULToken";
            case DIV:
                return "DIVToken";
            case ASSIGN:
                return "ASSIGNToken";
            case EQ:
                return "EQToken";
            case NEQ:
                return "NEQToken";
            case LT:
                return "LTToken";
            case GT:
                return "GTToken";
            case LE:
                return "LEToken";
            case GE:
                return "GEToken";
            case L_PARENT:
                return "L_PARENTToken";
            case R_PARENT:
                return "R_PARENTToken";
            case L_BRACE:
                return "L_BRACEToken";
            case R_BRACE:
                return "R_BRACEToken";
            case ARROW:
                return "ARROWToken";
            case COMMA:
                return "COMMAToken";
            case COLON:
                return "COLONToken";
            case SEMICOLON:
                return "SEMICOLONToken";
            case EOF:
                return "EOF";
            case Ident:
                return "Identifier";
            case Uint:
                return "UnsignedInteger";
            case DoubleVar:
                return "DoubleVar";
            case CharVar:
                return "CharVar";
            case StringVar:
                return "StringVar";
            default:
                return "InvalidToken";
        }
    }
}
