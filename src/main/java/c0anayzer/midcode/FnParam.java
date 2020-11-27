package c0anayzer.midcode;

public class FnParam {
    private String paramName;
    private boolean isConst;
    private String type;

    public FnParam(String paramName, boolean isConst, String type){
        this.paramName = paramName;
        this.isConst = isConst;
        this.type = type;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}
