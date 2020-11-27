package c0anayzer.midcode;

public class GlobalVar {
    private boolean isConst;
    private String varName;

    public GlobalVar(String name, boolean isConst){
        this.varName = name;
        this.isConst = isConst;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }
}
