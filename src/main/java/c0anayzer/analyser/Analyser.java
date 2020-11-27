package c0anayzer.analyser;

import c0anayzer.error.AnalyzeError;
import c0anayzer.error.CompileError;
import c0anayzer.error.ErrorCode;
import c0anayzer.error.ExpectedTokenError;
import c0anayzer.error.TokenizeError;
import c0anayzer.instruction.Instruction;
import c0anayzer.instruction.Operation;
import c0anayzer.midcode.FnInstruct;
import c0anayzer.midcode.GlobalVar;
import c0anayzer.midcode.MidCode;
import c0anayzer.tokenizer.Token;
import c0anayzer.tokenizer.TokenType;
import c0anayzer.tokenizer.Tokenizer;
import c0anayzer.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    MidCode midCode = MidCode.getMidCode();

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    ArrayList<SymbolEntry> symbolTable = new ArrayList<>();


    /**
     * 符号表中查找当前层级的符号
     * @param name
     * @param rank
     * @return 存在相同变量名：true 不存在：false
     */
    public boolean findSymbol(String name, int rank){
        for(SymbolEntry s: symbolTable){
            if(s.symbolName.equals(name) && s.getSymbolRank()==rank){
                return true;
            }
        }
        return false;
    }

    /**
     * 从后往前查找符号，不是全局符号的
     * @param name
     * @param rank
     * @return
     */
    public SymbolEntry findBSymbol(String name, int rank){
        for(int i=symbolTable.size()-1; i>=0; i--){
            if(symbolTable.get(i).getSymbolName().equals(name) &&
                    symbolTable.get(i).getSymbolRank()<=rank && symbolTable.get(i).getSymbolRank()!=0){
                return symbolTable.get(i);
            }
        }
        return null;
    }

    /**
     * 根据变量名和等级获取变量
     * @param name
     * @param rank
     * @return
     */
    public SymbolEntry getSymbol(String name, int rank){
        for(SymbolEntry s: symbolTable){
            if(s.symbolName.equals(name) && s.getSymbolRank()==rank){
                return s;
            }
        }
        return null;
    }

    /**
     * 调用某个变量，若未定义过则抛出异常，若定义过则返回变量
     * @param name
     * @param rank
     * @return
     */
    public SymbolEntry useSymbol(String name, int rank, Pos curPos) throws AnalyzeError {
        for(int i=symbolTable.size()-1; i>=0; i--){
            if(symbolTable.get(i).getSymbolName().equals(name) && symbolTable.get(i).getSymbolRank() <= rank){
                return symbolTable.get(i);
            }
        }
        throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
    }

    /**
     * 将某个rank的变量全都pop掉
     * @param rank
     */
    public void popRank(int rank){
        symbolTable.removeIf(s -> s.getSymbolRank() == rank);
    }


    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public MidCode analyse() throws CompileError {
        analyseProgram();
        return midCode;
    }

    /**
     * 查看下一个 Token
     *
     * @return 下一个token
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token 并前进
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt token
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }


    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, String type, boolean isInitialized, boolean isConstant, Pos curPos, int rank, int offset) throws AnalyzeError {
        if (findSymbol(name, rank)) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.add(new SymbolEntry(name, type, isConstant, isInitialized, offset, rank));
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos, int rank) throws AnalyzeError {
        var entry = getSymbol(name, rank);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, int rank, Pos curPos) throws AnalyzeError {
        var entry = useSymbol(name, rank, curPos);
        return entry.getStackOffset();
    }

    /**
     * 根据SymbolEntry返回符号的偏移量
     * @param sy
     * @return
     * @throws AnalyzeError
     */
    private int getOffset(SymbolEntry sy) throws AnalyzeError {
        return symbolTable.indexOf(sy);
    }

    /**
     * 获取局部变量的位置
     * @param sy
     * @return
     */
    private int getVarThisRankOffset(SymbolEntry sy){
        for(SymbolEntry s : symbolTable){
            if(s.getSymbolName().equals(sy.getSymbolName()) && s.getSymbolRank() == sy.getSymbolRank()){
                return s.getStackOffset();
            }
        }
        return -1;
    }

    /**
     * 获取当前rank的下一个局部变量的位置
     * @param rank
     * @return
     */
    private int getThisRankOffset(int rank){
<<<<<<< HEAD
        if(rank == 0){
            int num=0;
            for(SymbolEntry s: symbolTable){
                if(s.getSymbolRank() == 0){
                    num ++;
                }
            }
            return num;
        }
        else {
            int num = 0;
            for (SymbolEntry s : symbolTable) {
                if (s.getSymbolRank() <= rank && rank != 0) {
                    num++;
                }
=======
        int num=0;
        for(SymbolEntry s:symbolTable){
            if(s.getSymbolRank()<=rank && s.getSymbolRank() != 0){
                num++;
>>>>>>> parent of 3e90496... global
            }
            return num;
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, int rank, Pos curPos) throws AnalyzeError {
        var entry = getSymbol(name, rank);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConst();
        }
    }

    /**
     */
    private void analyseProgram() throws CompileError {
        FnInstruct startFn = new FnInstruct("_start");
        //MidCode.getMidCode().addFunction(startFn);
        analyseBody(startFn);
        MidCode.getMidCode().addFunction(startFn);
        FnInstruct m = midCode.getFn("main", peek().getStartPos());
        startFn.addInstruction(new Instruction(Operation.stackalloc, m.getReturnSlots(), 4));
        int o = midCode.getFnAddress("main");
        startFn.addInstruction(new Instruction(Operation.call, o, 4));
        midCode.addGlobalSymbol("_start", peek().getStartPos());
        // 'end'
        expect(TokenType.EOF);
    }

    private void analyseBody(FnInstruct f) throws CompileError {
        while(!check(TokenType.EOF)){
            if(check(TokenType.FN)){
                analyseFunction();
            }
            else{
                // 全局变量
                if(check(TokenType.CONST)){ // decl_stmt const
                    analyseConstantDeclaration(f, 0);
                }
                else{ // decl_stmt let
                    analyseVariableDeclaration(f, 0);
                }
            }
        }
    }

    // 'let' IDENT ':' ty ('=' expr)? ';'
    private void analyseVariableDeclaration(FnInstruct f, int rank) throws CompileError {
        expect(TokenType.LET);
        Token ident = expect(TokenType.Ident);
        expect(TokenType.COLON);
        Token ty = expectTyToken();

        if(rank==0) {
            // 添加一个全局变量到全局符号表
            midCode.addGlobalVar(ident.getValueString(), peek().getStartPos());
            // 添加一个全局变量到全局变量表
            midCode.addGlobalVar(new GlobalVar(ident.getValueString(), false));
        }
        else {
            if(rank == 1)
                f.notInFnParams(ident.getValueString(), peek().getStartPos());
            f.addLoc();
        }
        int o = getThisRankOffset(rank);
        // 添加符号表
        addSymbol(ident.getValueString(), ty.getValueString(), false, false, peek().getStartPos(), rank, o);

        if(check(TokenType.ASSIGN)){
            expect(TokenType.ASSIGN);

            if(rank==0){
                o = MidCode.getMidCode().getNextGlobalVarOffset();
                f.addInstruction(new Instruction(Operation.globa, o-1, 4));
            }
            else {
                o = f.getNextLocOffset();
                f.addInstruction(new Instruction(Operation.loca, o, 4));
            }

            OperationList opList = new OperationList();
            String type = analyseExpression(f, rank, opList, true, null, null);
            while(!opList.isEmpty()){
                String op = opList.popList();
                addOperatorInstruction(f, op, type);
            }

            f.addInstruction(new Instruction(Operation.store_64));

            if(!type.equals(ty.getValueString())){
                throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
            }
            declareSymbol(ident.getValueString(), peek().getStartPos(), rank);
        }

        expect(TokenType.SEMICOLON);
    }
    // 'const' IDENT ':' ty '=' expr ';'
    private void analyseConstantDeclaration(FnInstruct f, int rank) throws CompileError {
        expect(TokenType.CONST);
        Token ident = expect(TokenType.Ident);
        expect(TokenType.COLON);
        Token ty = expectTyToken();
        if(rank==0) {
            // 添加一个全局变量到全局符号表
            midCode.addGlobalVar(ident.getValueString(), peek().getStartPos());
            // 添加一个全局变量到全局变量表
            midCode.addGlobalVar(new GlobalVar(ident.getValueString(), true));
        }
        else {
            f.notInFnParams(ident.getValueString(), peek().getStartPos());
            f.addLoc();
        }
        int o = getThisRankOffset(rank);
        addSymbol(ident.getValueString(), ty.getValueString(), true, true, peek().getStartPos(), rank, o);

        expect(TokenType.ASSIGN);
        if(rank==0){
            o = MidCode.getMidCode().getNextGlobalVarOffset();
            f.addInstruction(new Instruction(Operation.globa, o-1, 4));
        }
        else {
            o = f.getNextLocOffset();
            f.addInstruction(new Instruction(Operation.loca, o, 4));
        }

        OperationList opList = new OperationList();
        String type = analyseExpression(f, rank, opList, true, null, null);
        while(!opList.isEmpty()){
            String op = opList.popList();
            addOperatorInstruction(f, op, type);
        }

        f.addInstruction(new Instruction(Operation.store_64));

        if(!type.equals(ty.getValueString())){
            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
        }
        declareSymbol(ident.getValueString(), peek().getStartPos(), rank);

        expect(TokenType.SEMICOLON);

    }

    // if_stmt
    private void analyseIfStatement(FnInstruct f, int rank, ArrayList<Integer> breakList, ArrayList<Integer> continueList) throws CompileError {
        expect(TokenType.IF);
        OperationList opList = new OperationList();
        String ty = analyseExpression(f, rank, opList, false, breakList, continueList);
        while(!opList.isEmpty()){
            String op = opList.popList();
            addOperatorInstruction(f, op, ty);
        }
        f.addInstruction(new Instruction(Operation.br_true, 1, 4));


        int len3 = -1;
        int len1 = f.getInstructionsLength();
        f.addInstruction(new Instruction(Operation.br,0, 4));

        analyseBlockStatement(f, rank+1, breakList, continueList);

        int len2 = f.getInstructionsLength();
        f.addInstruction(new Instruction(Operation.br, 0, 4));
        // TODO
        f.setBrInstructionValue(len1, len2-len1);
//        f.insertInstruction(new Instruction(Operation.br, len2-len1 + 1,4), len1);
//        len2 ++;

        if(check(TokenType.ELSE)){
            next();
            if(check(TokenType.IF)){
                analyseIfStatement(f, rank, breakList, continueList);
            }
            else{
                analyseBlockStatement(f, rank+1, breakList, continueList);
            }
        }

        int lenE = f.getInstructionsLength();
        // TODO
        f.setBrInstructionValue(len2, lenE-len2-1);
        //f.insertInstruction(new Instruction(Operation.br, lenE - len2 ,4), len2);
    }
    // while_stmt
    private void analyseWhileStatement(FnInstruct f, int rank) throws CompileError {
        expect(TokenType.WHILE);
        OperationList opList = new OperationList();

        int len0 = f.getInstructionsLength();
        f.addInstruction(new Instruction(Operation.br, 0, 4) );
        String ty = analyseExpression(f, rank, opList, false, null, null);
        while(!opList.isEmpty()){
            String op = opList.popList();
            addOperatorInstruction(f, op, ty);
        }
        f.addInstruction(new Instruction(Operation.br_true, 1, 4));

        int len1 = f.getInstructionsLength();
        f.addInstruction(new Instruction(Operation.br, 0 ,4));
        // 记录位置便于跳转
        ArrayList<Integer> breakList = new ArrayList<>();
        ArrayList<Integer> continueList = new ArrayList<>();
        analyseBlockStatement(f, rank + 1, breakList, continueList);

        int len2 = f.getInstructionsLength();
        f.setBrInstructionValue(len1, len2-len1);
        //f.insertInstruction(new Instruction(Operation.br, len2-len1+1, 4), len1);
        f.addInstruction(new Instruction(Operation.br, len0-len2, 4) );

        for(Integer b: breakList) {
            f.setBrInstructionValue(b, len2 - b);
            //b.setX(len2 - f.getInstructionOffset(b));
        }
        for(Integer c: continueList) {
            f.setBrInstructionValue(c, len0 - c);
            //f.insertInstruction(new Instruction(Operation.br, c-len2, 4), c);
        }
    }
    // return_stmt
    private void analyseReturnStatement(FnInstruct f, int rank) throws CompileError {
        f.addInstruction(new Instruction(Operation.arga, 0, 4));
        expect(TokenType.RETURN);
        String ty = "void";
        if(check(TokenType.SEMICOLON)){
            analyseEmptyStatement(f, rank);
        }
        else{
            OperationList opList = new OperationList();
            ty = analyseExpression(f, rank, opList, true, null, null);
            while(!opList.isEmpty()){
                String op = opList.popList();
                addOperatorInstruction(f, op, ty);
            }
            f.addInstruction(new Instruction(Operation.store_64));
        }
        f.returnFn(ty, peek().getStartPos());
        f.addInstruction(new Instruction(Operation.ret));
    }
    // block_stmt
    private void analyseBlockStatement(FnInstruct f, int rank, ArrayList<Integer> breakList, ArrayList<Integer> continueList) throws CompileError {
        expect(TokenType.L_BRACE);
        while(!check(TokenType.R_BRACE)){
            analyseStatement(f, rank, breakList, continueList);
        }
        expect(TokenType.R_BRACE);
        popRank(rank);
    }
    // empty_stmt
    private void analyseEmptyStatement(FnInstruct f, int rank) throws CompileError {
        expect(TokenType.SEMICOLON);
    }


    /*  expr->
        operator_expr
        | negate_expr
        | assign_expr
        | as_expr
        | call_expr
        | literal_expr
        | ident_expr
        | group_expr
     */

    private String analyseExpression(FnInstruct f, int rank, OperationList opList, boolean isAssignEpr, ArrayList<Integer> breakList, ArrayList<Integer> continueList) throws CompileError {
        String type = "void";
        if(check(TokenType.MINUS)){ // negate_expr
            type = analyseNegateExpression(f, rank, opList, isAssignEpr, breakList, continueList);
        }
        else if(check(TokenType.Ident)){ // ident = expr || ident(...)
            Token ident = next();

            if(check(TokenType.ASSIGN)) { // assign_expr -> l_expr '=' expr

                type = analyseAssignExpression(f, rank, opList, ident);
                /*
                int o;
                String pType;

                if((o=f.getParamOffset(ident.getValueString()) ) >= 0){
                    pType = f.getOffsetParam(o).getType();
                    if(f.haveRet())
                        o++;
                    f.addInstruction(new Instruction(Operation.arga, o, 4));
                }
                else {
                    SymbolEntry sy = useSymbol(ident.getValueString(), rank, peek().getStartPos());
                    pType = sy.getType();
                    o = getOffset(ident.getValueString(), rank, peek().getStartPos());
                    if(sy.getSymbolRank()==0){
                        f.addInstruction(new Instruction(Operation.globa, o, 4));
                    }
                    else {
                        f.addInstruction(new Instruction(Operation.loca, o, 4));
                    }
                }


                String ty = analyseAssignExpression(f, rank, opList);
                if(!pType.equals(ty)){
                    throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
                }

                f.addInstruction(new Instruction(Operation.store_64));

                type = "void";*/
            }
            else if(check(TokenType.L_PARENT)){ // call_expr
                type = analyseCallExpression(f, rank, opList, isAssignEpr, ident);

                if(!type.equals("void") && !isAssignEpr){
                    f.addInstruction(new Instruction(Operation.popn, 1, 4));
                }

            }
            else{
                type=  analyseIdentExpression(f, rank, ident, true);
                f.addInstruction(new Instruction(Operation.load_64));
<<<<<<< HEAD
<<<<<<< HEAD
=======
                /*
                int o;
                if((o=f.getParamOffset(ident.getValueString()) ) >= 0){
                    type = f.getOffsetParam(o).getType();
                    if(f.haveRet())
                        o++;
                    f.addInstruction(new Instruction(Operation.arga, o, 4));
                }
                else {
                    SymbolEntry sy = useSymbol(ident.getValueString(), rank, peek().getStartPos());
                    type = sy.getType();
                    o = getOffset(ident.getValueString(), rank, peek().getStartPos());
                    if(sy.getSymbolRank()==0){
                        f.addInstruction(new Instruction(Operation.globa, o, 4));
                    }
                    else {
                        f.addInstruction(new Instruction(Operation.loca, o, 4));
                    }
                }
                f.addInstruction(new Instruction(Operation.load_64));

                 */
>>>>>>> parent of 3e90496... global
=======

>>>>>>> parent of 1598dee... Global
            }
        }
        else if(check(TokenType.Uint)){ // UINT_LITERAL
            type = analyseUintLiteralExpression(f, rank, isAssignEpr);
        }
        else if(check(TokenType.DoubleVar)) { // DOUBLE_LITERAL
            type = analyseDoubleLiteralExpression(f, rank, isAssignEpr);
        }
        /*else if(check(TokenType.StringVar)) { // STRING_LITERAL
            analyseStringLiteralExpression();
        }*/
        else if(check(TokenType.CharVar)) { // CHAR_LITERAL
            type = analyseCharLiteralExpression(f, rank, isAssignEpr);
        }
        else if(check(TokenType.Ident)){ // ident_expr
            type = analyseIdentExpression(f, rank, true);
        }
        else if(check(TokenType.L_PARENT)){ // group_expr
            type = analyseGroupExpression(f, rank, isAssignEpr);
        }
        else if(check(TokenType.BREAK)){ // BREAK
            next();
            if(breakList==null){
                throw new AnalyzeError(ErrorCode.InvalidIdentifier, peek().getStartPos());
            }
            breakList.add(f.getInstructionsLength());
            f.addInstruction(new Instruction(Operation.br, 0, 4));

            expect(TokenType.SEMICOLON);
        }
        else if(check(TokenType.CONTINUE)){ // CONTINUE
            next();
            if(continueList==null){
                throw new AnalyzeError(ErrorCode.InvalidIdentifier, peek().getStartPos());
            }
            continueList.add(f.getInstructionsLength());
            f.addInstruction(new Instruction(Operation.br, 0, 4));
            expect(TokenType.SEMICOLON);
        }

        else{
            throw new ExpectedTokenError(TokenType.Ident, next());
        }

        while(check(TokenType.AS) || IsBinaryOperator(peek())){
            if(check(TokenType.AS)){
                type = analyseAsExpression(type, f, rank);
            }
            else{
                type = analyseOperatorExpression(f, rank, type, opList, isAssignEpr);
            }
        }

        while(!opList.isEmpty()){
            String op = opList.popList();
            addOperatorInstruction(f, op, type);
        }
/*
        if(!isAssignEpr){
            f.addInstruction(new Instruction(Operation.popn, 1, 4));
        }
*/
        return type;
    }

    private boolean IsBinaryOperator(Token p){
        String v = p.getValueString();
        return v.equals("+") || v.equals("-") || v.equals("*") || v.equals("/") || v.equals("==") ||
                v.equals("!=") || v.equals("<") || v.equals(">") || v.equals("<=") || v.equals(">=");
    }

    /**
     *
     * @param f
     * @param rank
     * @param ty 表达式类型
     * @param opList
     * @return
     * @throws CompileError
     */
    private String analyseOperatorExpression(FnInstruct f, int rank, String ty, OperationList opList, boolean isAssignEpr) throws CompileError {
        Token o = next();

        //System.out.print(o.getValueString());
        //System.out.println(OperationList.cmpOp(opList.peek(), o.getValueString()));

        if(OperationList.cmpOp(opList.peek(), o.getValueString())){
            addOperatorInstruction(f, opList.popList(), ty);
            opList.addList(o.getValueString());
        }
        else{
            opList.addList(o.getValueString());
        }
        // TODO check
        String type = analyseExpression(f, rank, opList, isAssignEpr, null, null);
        if(!ty.equals(type))
            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());

        return type;
    }
    private void addOperatorInstruction(FnInstruct f, String op, String ty) throws CompileError {
        if(ty.equals("int")){
            switch (op){
                case "+":
                    f.addInstruction(new Instruction(Operation.add_i));
                    break;
                case "-":
                    f.addInstruction(new Instruction(Operation.sub_i));
                    break;
                case "*":
                    f.addInstruction(new Instruction(Operation.mul_i));
                    break;
                case "/":
                    f.addInstruction(new Instruction(Operation.div_i));
                    break;
                case "neg":
                    f.addInstruction(new Instruction(Operation.neg_i));
                    break;
                case "==":
                    f.addInstruction(new Instruction(Operation.cmp_i));
                    f.addInstruction(new Instruction(Operation.not));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case "!=":
                    f.addInstruction(new Instruction(Operation.cmp_i));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case "<":
                    f.addInstruction(new Instruction(Operation.cmp_i));
                    f.addInstruction(new Instruction(Operation.set_lt));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case ">":
                    f.addInstruction(new Instruction(Operation.cmp_i));
                    f.addInstruction(new Instruction(Operation.set_gt));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case "<=":
                    f.addInstruction(new Instruction(Operation.cmp_i));
                    f.addInstruction(new Instruction(Operation.set_gt));
                    f.addInstruction(new Instruction(Operation.not));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case ">=":
                    f.addInstruction(new Instruction(Operation.cmp_i));
                    f.addInstruction(new Instruction(Operation.set_lt));
                    f.addInstruction(new Instruction(Operation.not));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;

            }
        }
        else if(ty.equals("double")){
            switch (op){
                case "+":
                    f.addInstruction(new Instruction(Operation.add_f));
                    break;
                case "-":
                    f.addInstruction(new Instruction(Operation.sub_f));
                    break;
                case "*":
                    f.addInstruction(new Instruction(Operation.mul_f));
                    break;
                case "/":
                    f.addInstruction(new Instruction(Operation.div_f));
                    break;
                case "neg":
                    f.addInstruction(new Instruction(Operation.neg_f));
                    break;
                case "==":
                    f.addInstruction(new Instruction(Operation.cmp_f));
                    f.addInstruction(new Instruction(Operation.not));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case "!=":
                    f.addInstruction(new Instruction(Operation.cmp_f));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case "<":
                    f.addInstruction(new Instruction(Operation.cmp_f));
                    f.addInstruction(new Instruction(Operation.set_lt));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case ">":
                    f.addInstruction(new Instruction(Operation.cmp_f));
                    f.addInstruction(new Instruction(Operation.set_gt));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case "<=":
                    f.addInstruction(new Instruction(Operation.cmp_f));
                    f.addInstruction(new Instruction(Operation.set_gt));
                    f.addInstruction(new Instruction(Operation.not));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
                case ">=":
                    f.addInstruction(new Instruction(Operation.cmp_f));
                    f.addInstruction(new Instruction(Operation.set_lt));
                    f.addInstruction(new Instruction(Operation.not));
                    //f.addInstruction(new Instruction(Operation.br_true, 1, 4));
                    break;
            }
        }
        else{
            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
        }
    }



    // '-' expr
    private String analyseNegateExpression(FnInstruct f, int rank, OperationList opList, boolean isAssignEpr, ArrayList<Integer> breakList, ArrayList<Integer> continueList) throws CompileError {
        expect(TokenType.MINUS);
        opList.addList("neg");
        return analyseExpression(f, rank, opList, isAssignEpr, breakList, continueList);
    }
    // '=' expr
    private String analyseAssignExpression(FnInstruct f, int rank, OperationList opList, Token ident) throws CompileError {
        String type = analyseIdentExpression(f,rank, ident, false);
        expect(TokenType.ASSIGN);
        // TODO CHEKC
        String ty = analyseExpression(f, rank, opList, true, null ,null);
        f.addInstruction(new Instruction(Operation.store_64));
        if(!ty.equals(type))
            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
        return "void";
    }
    // 'as' ty
    private String analyseAsExpression(String baseType, FnInstruct f, int rank) throws CompileError {
        expect(TokenType.AS);
        Token ty = expectTyToken();
        if(baseType.equals("void") || ty.getValueString().equals("void")){
            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
        }

        if(!baseType.equals(ty.getValueString())){
            if(ty.getValueString().equals("int")){
                f.addInstruction(new Instruction(Operation.ftoi));
            }
            else{
                f.addInstruction(new Instruction(Operation.itof));
            }
        }

        return ty.getValueString();
    }
    // '(' (expr p_list? ')'   p_list-> ',' expr)*
    private String analyseCallExpression(FnInstruct f, int rank, OperationList opList, boolean isAssignEpr, Token ident) throws CompileError {
        String ty = "void";
        if((ty = FnInstruct.libFn.get(ident.getValueString()))!=null ){
            expect(TokenType.L_PARENT);
            int o = midCode.insertLibFunctionBefore(f.getFnName(), ident.getValueString());
            switch (ident.getValueString()) {
                case "getdouble":
                    ty = "double";
                    f.addInstruction(new Instruction(Operation.stackalloc, 1, 4));
                    break;
                case "getint":
                case "getchar":
                    ty = "int";
                    f.addInstruction(new Instruction(Operation.stackalloc, 1, 4));
                    break;
                case "putstr":
                    f.addInstruction(new Instruction(Operation.stackalloc, 0, 4));
                    if (check(TokenType.R_PARENT)) {
                        throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
                    }
                    Token t = expect(TokenType.StringVar);
                    midCode.addGlobalSymbolToLastPos(t.getValueString(), peek().getStartPos());
                    o = midCode.getSymbolAddress(t.getValueString());
                    f.addInstruction(new Instruction(Operation.push, o, 8));
                    o = midCode.getSymbolAddress("putstr");
                    break;
                case "putln":
                    f.addInstruction(new Instruction(Operation.stackalloc, 0, 4));
                    break;
                default:
                    if (check(TokenType.R_PARENT)) {
                        throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
                    }
                    f.addInstruction(new Instruction(Operation.stackalloc, 0, 4));
                    if (ident.getValueString().equals("putint") || ident.getValueString().equals("putchar")) {
                        if (!analyseExpression(f, rank, opList, true, null, null).equals("int")) {
                            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
                        }
                    } else if (ident.getValueString().equals("putdouble")) {
                        if (!analyseExpression(f, rank, opList, true, null, null).equals("double")) {
                            throw new AnalyzeError(ErrorCode.TypeMismatch, peek().getStartPos());
                        }
                    }
                    break;
            }
            //int o = midCode.getSymbolAddress(ident.getValueString());

            expect(TokenType.R_PARENT);

            f.addInstruction(new Instruction(Operation.callname, o, 4));
            /*if(!ty.equals("void") && !isAssignEpr){
                f.addInstruction(new Instruction(Operation.popn, 1, 4));
            }*/

            return ty;
        }

        FnInstruct fn = midCode.getFn(ident.getValueString(), peek().getStartPos());
        f.addInstruction(new Instruction(Operation.stackalloc, fn.getReturnSlots(), 4 ));

        expect(TokenType.L_PARENT);
        ArrayList<String> paramsTypeList = new ArrayList<>();
        while (!check(TokenType.R_PARENT)){
            paramsTypeList.add(analyseExpression(f, rank, opList, false, null, null));
            if(check(TokenType.COMMA)){
                next();
            }
            else{
                break;
            }
        }
        expect(TokenType.R_PARENT);


        fn.checkParams(peek().getStartPos(), paramsTypeList);

        f.addInstruction(new Instruction(Operation.call, midCode.getFnAddress(fn.getFnName()), 4 ));
        if(fn.getReturnSlots() > 0){
            //f.addInstruction(new Instruction(Operation.popn, 1, 4 ));
        }
        return fn .getReturnType();
    }
    private String analyseUintLiteralExpression(FnInstruct f, int rank, boolean isAssignEpr) throws CompileError {
        f.addInstruction(new Instruction(Operation.push, (long)(next().getValue()), 8 ));

        //f.addInstruction(new Instruction(Operation.store_64));
        return "int";
    }
    private String analyseDoubleLiteralExpression(FnInstruct f, int rank, boolean isAssignEpr) throws CompileError {
        f.addInstruction(new Instruction(Operation.push, (double)(next().getValue())));

        //f.addInstruction(new Instruction(Operation.store_64));
        return "double";
    }
    /*
    private void analyseStringLiteralExpression() throws CompileError {
    }*/
    private String analyseCharLiteralExpression(FnInstruct f, int rank, boolean isAssignEpr) throws CompileError {
        f.addInstruction(new Instruction(Operation.push, (int)(next().getValue()), 8 ));

        //f.addInstruction(new Instruction(Operation.store_64));
        return "int";
    }
    // ident_expr -> IDENT
    private String analyseIdentExpression(FnInstruct f, int rank, boolean allowConst) throws CompileError {
        Token ident = expect(TokenType.Ident);
        return analyseIdentExpression(f, rank, ident, allowConst);
    }

    private String analyseIdentExpression(FnInstruct f, int rank, Token ident, boolean allowConst) throws CompileError {
        int o;
        String type;

        // 按照rank由高到低查找局部变量表
        SymbolEntry sy;
        if ((sy = findBSymbol(ident.getValueString(), rank))!=null) {

            if(sy.isConst() && !allowConst){
                throw new AnalyzeError(ErrorCode.ChangeConst, peek().getStartPos());
            }

            o = getVarThisRankOffset(sy);
            f.addInstruction(new Instruction(Operation.loca, o, 4));
            type = sy.getType();
        }
        // 查找函数参数表
        else if ((o = f.getParamOffset(ident.getValueString())) >= 0) {
            if(f.getOffsetParam(o).isConst() && !allowConst){
                throw new AnalyzeError(ErrorCode.ChangeConst, peek().getStartPos());
            }

            type = f.getOffsetParam(o).getType();
            if (f.haveRet())
                o++;
            f.addInstruction(new Instruction(Operation.arga, o, 4));
        }
        // 查找变量表
        else {
            sy = useSymbol(ident.getValueString(), 0, peek().getStartPos());
            if(sy.isConst() && !allowConst){
                throw new AnalyzeError(ErrorCode.ChangeConst, peek().getStartPos());
            }
            type = sy.getType();
<<<<<<< HEAD
            o = getOffset(ident.getValueString(), rank, peek().getStartPos());
<<<<<<< HEAD
=======
            o = sy.getStackOffset();
>>>>>>> parent of 1598dee... Global

=======
>>>>>>> parent of 3e90496... global
            f.addInstruction(new Instruction(Operation.globa, o, 4));
        }
        //f.addInstruction(new Instruction(Operation.load_64));
        return type;
    }

    // group_expr -> '(' expr ')'
    private String analyseGroupExpression(FnInstruct f, int rank, boolean isAssignEpr) throws CompileError {
        expect(TokenType.L_PARENT);
        OperationList opList = new OperationList();
        String type = analyseExpression(f, rank, opList, isAssignEpr, null, null);
        while(!opList.isEmpty()){
            String op = opList.popList();
            addOperatorInstruction(f, op, type);
        }
        expect(TokenType.R_PARENT);
        return type;
    }

    /*
    变量类型 不能为void
    如果是ty则返回token不是则抛异常
     */
    private Token expectTyToken() throws CompileError{
        Token t = next();
        if(t.getTokenType().equals(TokenType.INT) ||
                t.getTokenType().equals(TokenType.DOUBLE)){
            return t;
        }
        throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.L_PARENT), next());
    }
    /*
    函数返回值
    如果是ty则返回token不是则抛异常
     */
    private Token expectFnTyToken() throws CompileError{
        Token t = next();
        if(t.getTokenType().equals(TokenType.INT) ||
                t.getTokenType().equals(TokenType.DOUBLE) ||
                        t.getTokenType().equals(TokenType.VOID)){
            return t;
        }
        throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.L_PARENT), next());
    }

    /*  stmt ->
        | expr_stmt
        | decl_stmt  let const
        | if_stmt
        | while_stmt
        | return_stmt
        | block_stmt
        | empty_stmt
    */
    private void analyseStatement(FnInstruct f, int rank, ArrayList<Integer> breakList, ArrayList<Integer> continueList) throws CompileError {
        if(check(TokenType.LET)){ // decl_stmt let
            analyseVariableDeclaration(f, rank);
        }
        else if(check(TokenType.CONST)){ // decl_stmt const
            analyseConstantDeclaration(f, rank);
        }
        else if(check(TokenType.IF)){ // if_stmt
            analyseIfStatement(f, rank, breakList, continueList);
        }
        else if(check(TokenType.WHILE)){ // while_stmt
            analyseWhileStatement(f, rank);
        }
        else if(check(TokenType.RETURN)){ // return_stmt
            analyseReturnStatement(f, rank);
        }
        else if(check(TokenType.L_BRACE)){ // block_stmt
            analyseBlockStatement(f, rank+1, breakList, continueList);
        }
        else if(check(TokenType.SEMICOLON)){ // empty_stmt
            analyseEmptyStatement(f, rank);
        }
        else{
            OperationList opList = new OperationList();
            String ty = analyseExpression(f, rank, opList, false, breakList, continueList);
            while(!opList.isEmpty()){
                String op = opList.popList();
                addOperatorInstruction(f, op, ty);
            }

            if(!ty.equals("void") ){
                f.addInstruction(new Instruction(Operation.popn, 1, 4));
            }
        }

    }

    // function
    private void analyseFunction() throws CompileError{
        expect(TokenType.FN);
        Token ident = expect(TokenType.Ident);
        expect(TokenType.L_PARENT);

        FnInstruct f = new FnInstruct(ident.getValueString());

        // 如果没有重复的，添加一个函数
        midCode.addGlobalSymbol(ident.getValueString(), peek().getStartPos());
        midCode.addFunction(f);

        while (!check(TokenType.R_PARENT)){
            analyseFunctionParam(f);
            if(check(TokenType.COMMA)){
                next();
            }
            else{
                break;
            }
        }
        expect(TokenType.R_PARENT);

        expect(TokenType.ARROW);
        Token ty = expectFnTyToken();
        f.setReturn(ty.getValueString());

        analyseBlockStatement(f, 1, null, null);
        if(!f.isReturned()){
            f.addInstruction(new Instruction(Operation.ret));
            f.returnFn("void", peek().getStartPos());
        }
        else {
            if (!f.checkReturnRoutes()) {
                throw new AnalyzeError(ErrorCode.NotAllRoutesReturn, peek().getStartPos());
            }
        }
    }

    private void analyseFunctionParam(FnInstruct f) throws CompileError{
        boolean isConst = false;
        if(check(TokenType.CONST)){
            isConst = true;
            next();
        }
        Token ident = expect(TokenType.Ident);
        expect(TokenType.COLON);
        Token ty = expectTyToken();

        f.addParam(ident.getValueString(),isConst,ty.getValueString(),peek().getStartPos());
    }

}
