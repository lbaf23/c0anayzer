package c0anayzer.analyser;

public class SymbolEntry {
    String symbolName;
    String type;
    boolean isConst;
    boolean isInitialized;
    int stackOffset;
    int symbolRank;

    /**
     * @param isConst
     * @param isDeclared
     * @param stackOffset
     */
    public SymbolEntry(String symbolName, String type, boolean isConst, boolean isDeclared, int stackOffset,int rank) {
        this.symbolName = symbolName;
        this.type = type;
        this.isConst = isConst;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.symbolRank = rank;
    }

    /**
     * @return the stackOffset
     */
    public int getStackOffset() {
        return stackOffset;
    }

    /**
     * @return the isConstant
     */
    public boolean isConst() {
        return isConst;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConst) {
        this.isConst = isConst;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @param stackOffset the stackOffset to set
     */
    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }

    public int getSymbolRank() {
        return symbolRank;
    }

    public void setSymbolRank(int symbolRank) {
        this.symbolRank = symbolRank;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
