package c0anayzer.midcode;
import c0anayzer.error.AnalyzeError;
import c0anayzer.error.ErrorCode;
import c0anayzer.instruction.Instruction;
import c0anayzer.instruction.Operation;
import c0anayzer.util.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FnInstruct {
    public static HashMap<String, String> libFn = new HashMap<>();
    static {
        libFn.put("getint", "int"); libFn.put("getdouble","double"); libFn.put("putdouble", "void");
        libFn.put("getchar", "int"); libFn.put("putint", "void"); libFn.put("putchar", "void");
        libFn.put("putstr", "void"); libFn.put("putln", "void");
    }

    public String fnName;
    public int returnSlots = 0;
    public String returnType;
    public boolean returned;
    public int paramSlots = 0;
    public ArrayList<FnParam> paramTable = new ArrayList<>();
    public int locSlots = 0;

    public ArrayList<Instruction> fnBody = new ArrayList<>();

    public FnInstruct(String fnName){
        this.fnName = fnName;
    }


    /**
     * 函数体中加入一条指令
     * @param i 指令
     */
    public void addInstruction(Instruction i){
        fnBody.add(i);
    }

    /**
     * 返回当前指令长度 用于跳转
     * @return 长度
     */
    public int getInstructionsLength() {
        return fnBody.size();
    }

    /**
     * 向指定位置插入指令
     * @param i 位置
     * @param index 指令

    public void insertInstruction(Instruction i, int index){
        fnBody.add(index, i);
    }*/
    /**
     * 设置某条指令的返回值，用于设置br指令的跳转距离
     * @param index
     * @param x
     */
    public void setBrInstructionValue(int index, int x){
        Instruction i = this.fnBody.get(index);
        i.setX(x);
    }

    /**
     * 添加函数参数
     * @param paramName
     * @param isConst
     * @param paramType
     * @param curPos
     * @throws AnalyzeError
     */
    public void addParam(String paramName, boolean isConst, String paramType, Pos curPos) throws AnalyzeError {
        for(FnParam f: paramTable){
            if(f.getParamName().equals(paramName))
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        }
        paramTable.add(new FnParam(paramName, isConst, paramType));
        paramSlots ++;
    }


    /**
     * 添加一个局部变量
     */
    public void addLoc(){
        this.locSlots ++;
    }

    /**
     * 获取下一个局部变量的栈偏移
     * @return
     */
    public int getNextLocOffset(){
        return this.locSlots - 1;
    }

    /**
     * 设置返回值类型
     */
    public void setReturn(String ty){
        if(!ty.equals("void"))
            this.returnSlots = 1;
        this.returnType = ty;
    }

    /**
     * 获取函数参数的栈偏移
     * @param name
     * @return
     */
    public int getParamOffset(String name){
        int i=0;
        for(FnParam f: paramTable){
            if(f.getParamName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 根据变量序列获取变量类型，用于检测变量类型是否符合
     * @param offset
     * @return
     */
    public FnParam getOffsetParam(int offset){
        return paramTable.get(offset);
    }

    public boolean haveRet(){
        return this.returnSlots > 0;
    }

    /**
     * 变量和函数参数不重复
     * @param name 变量名
     * @param curPos 位置
     * @throws AnalyzeError 重复
     */
    public void notInFnParams(String name, Pos curPos) throws AnalyzeError{
        for(FnParam f: paramTable){
            if(f.getParamName().equals(name))
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        }
    }

    /**
     * 检查函数传入参数类型是否合法
     * @param curPos 当前位置
     * @param params 调用参数的类型
     * @throws AnalyzeError 类型不匹配或数量不匹配
     */
    public void checkParams(Pos curPos, ArrayList<String> params) throws AnalyzeError {
        if(params.size() != this.paramSlots)
            throw new AnalyzeError(ErrorCode.FuncParamSizeMismatch, curPos);
        for(int i=0; i<params.size(); i++){
            if(!params.get(i).equals(this.paramTable.get(i).getType())){
                throw new AnalyzeError(ErrorCode.TypeMismatch, curPos);
            }
        }
    }

    /**
     * 设置为已返回
     */
    public void returnFn(String ty, Pos curPos) throws AnalyzeError{
        if(!ty.equals(this.returnType)){
            throw new AnalyzeError(ErrorCode.TypeMismatch, curPos);
        }
        this.returned = true;
    }


    public String getFnName() {
        return fnName;
    }

    public void setFnName(String fnName) {
        this.fnName = fnName;
    }

    public int getReturnSlots() {
        return returnSlots;
    }

    public void setReturnSlots(int returnSlots) {
        this.returnSlots = returnSlots;
    }

    public int getParamSlots() {
        return paramSlots;
    }

    public void setParamSlots(int paramSlots) {
        this.paramSlots = paramSlots;
    }

    public int getLocSlots() {
        return locSlots;
    }

    public void setLocSlots(int locSlots) {
        this.locSlots = locSlots;
    }

    public ArrayList<Instruction> getFnBody() {
        return fnBody;
    }

    public void setFnBody(ArrayList<Instruction> fnBody) {
        this.fnBody = fnBody;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public ArrayList<FnParam> getParamTable() {
        return paramTable;
    }

    public void setParamTable(ArrayList<FnParam> paramTable) {
        this.paramTable = paramTable;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public int getFnBodyCount(){
        return this.fnBody.size();
    }

    public int getFnNumber(){
        return MidCode.getMidCode().getFnNumber(this.fnName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("fn [").append(MidCode.getMidCode().getFnNumber(this.fnName)).
                append("]").append(locSlots).append(" ").append(paramSlots).append(" -> ").
                append(returnSlots).append(" {\n");

        for(Instruction i : fnBody){
            sb.append(i).append("\n");
        }
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * 检查是否所有的路径都有返回
     * @return 是否有返回 true有 false没有
     */
    public boolean checkReturnRoutes(){
        if(this.getReturnType().equals("void")){
            if(!this.fnBody.get(this.fnBody.size()-1).getOpt().equals(Operation.ret)){
                addInstruction(new Instruction(Operation.ret));
            }
            return true;
        }
        else {
            return dfs(0, new HashSet<Integer>());
        }
    }

    private boolean dfs(int i, HashSet<Integer> routes){
        if( i > fnBody.size()-1){
            return false;
        }
        else if(routes.contains(i)){
            return true;
        }
        else if(fnBody.get(i).getOpt().equals(Operation.br_true)){
            routes.add(i);
            boolean ret = dfs(i+1, routes);
            return ret && dfs(i+2, routes);
        }
        else if(fnBody.get(i).getOpt().equals(Operation.br)){
            routes.add(i);
            return dfs(i+fnBody.get(i).getIntX()+1, routes);
        }
        else if(fnBody.get(i).getOpt().equals(Operation.ret)){
            return true;
        }
        else{
            routes.add(i);
            return dfs(i+1, routes);
        }
    }
}
