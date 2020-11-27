package c0anayzer.midcode;

import c0anayzer.error.AnalyzeError;
import c0anayzer.error.ErrorCode;
import c0anayzer.util.Pos;

import java.util.ArrayList;

public class MidCode {
    public int magic = 0x72303b3e;
    public int version = 0x00000001;
    public String startFn = "_start";
    public ArrayList<GlobalVar> gdList = new ArrayList<>();
    public ArrayList<FnInstruct> fnList = new ArrayList<>();


    // 函数名和全局变量
    public ArrayList<String> globalSymbol = new ArrayList<>();

    public int getGlobalCounts(){
        return this.globalSymbol.size();
    }

    public int getGlobalVarNum(){
        return this.gdList.size();
    }

    private int globalVarNum = 0;

    private static MidCode midCode = new MidCode();

    public static MidCode getMidCode(){
        return midCode;
    }

    public int getNextGlobalVarOffset(){
        return gdList.size();
    }

    /**
     * 添加一个全局变量到表中
     * @param g
     */
    public void addGlobalVar(GlobalVar g){
        gdList.add(g);
    }
    public boolean inGlobalVarList(String name){
        for(GlobalVar g: gdList){
            if(g.getVarName().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * 变量不在全局符号表中
     * @param name 变量名
     * @param curPos 位置
     * @throws AnalyzeError 变量重复
     */
    public void notInGlobalSymbol(String name, Pos curPos) throws AnalyzeError {
        if(globalSymbol.indexOf(name) >= 0){
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        }
    }

    /**
     * 添加一个符号到全局符号表，非全局变量
     * @param name 变量名
     * @param curPos 位置
     * @throws AnalyzeError 变量重复
     */
    public void addGlobalSymbol(String name, Pos curPos) throws AnalyzeError{
        notInGlobalSymbol(name, curPos);
        globalSymbol.add(name);
    }

    /**
     * 向全局符号表的倒数第二个位置插入一条符号
     * @param name
     * @param curPos
     * @throws AnalyzeError
     */
    public void addGlobalSymbolToLastPos(String name, Pos curPos) throws AnalyzeError{
        notInGlobalSymbol(name, curPos);
        globalSymbol.add(globalSymbol.size()-2, name);
    }
    // 添加一个全局变量进去
    public void addGlobalVar(String name, Pos curPos) throws AnalyzeError{
        notInGlobalSymbol(name, curPos);
        globalSymbol.add(globalVarNum++, name);
    }

    /**
     * 添加一个函数块
     * @param f 函数块
     */
    public void addFunction(FnInstruct f){
        fnList.add(f);
    }



    /**
     * 从函数列表中去掉某个函数
     * @param fnName 函数名
     * @return 成功与否
     */
    public boolean removeFunction(String fnName){
        return fnList.removeIf(f -> f.getFnName().equals(fnName));
    }

    /**
     * 在全局符号表中插入一条库函数的记录，不会插入到fn列表中
     * @param fnName 函数
     * @return 插入的偏移量，callname 返回值即可
     */
    public int insertLibFunctionBefore(String fnName, String libFn){
        int i=0;
        for(String f: globalSymbol){
            if(f.equals(fnName)){
                break;
            }
            i++;
        }
        globalSymbol.add(i, libFn);
        return i;
    }

    /**
     * 获取函数的偏移量，从1开始
     * @param fnName 函数名
     * @return 偏移量
     */
    public int getFnAddress(String fnName) {
        int i=1;
        for(FnInstruct f:fnList){
            if(f.getFnName().equals(fnName)){
                return i;
            }
            i++;
        }
        return -1;
    }

    public int getFnNumber(String fnName) {
        int i=0;
        for(String s: globalSymbol){
            if(s.equals(fnName)){
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 获取函数
     * @param fnName
     * @param curPos
     * @return
     * @throws AnalyzeError
     */
    public FnInstruct getFn(String fnName, Pos curPos) throws AnalyzeError{
        for(FnInstruct f:fnList){
            if(f.getFnName().equals(fnName)){
                return f;
            }
        }
        throw new AnalyzeError(ErrorCode.NoSuchSymbol, curPos);
    }

    /**
     * 获取全局符号表中某个符号的位置，不存在则返回-1
     * @param name 符号
     * @return 位置
     */
    public int getSymbolAddress(String name){
        return globalSymbol.indexOf(name);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(GlobalVar g:gdList){
            sb.append(g.getVarName()).append('\n');
        }

        /*
        for(FnInstruct f : fnList){
            sb.append(f.getFnName()).append('\n');
        }*/



        for(int i=this.getGlobalVarNum(); i<this.globalSymbol.size(); i++){
            sb.append(this.globalSymbol.get(i)).append('\n');
        }
/*
        for(String s:globalSymbol){
            sb.append(s).append("\n");
        }
*/

        sb.append("\n");
        for(FnInstruct f : fnList){
            if(f.getFnName().equals("_start")){
                sb.append(f).append('\n');
            }
        }
        for(FnInstruct f : fnList){
            if(!f.getFnName().equals("_start") ){
                sb.append(f).append('\n');
            }
        }

        return sb.toString();
    }

}
