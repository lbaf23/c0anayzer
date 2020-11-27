package c0anayzer.analyser;

import java.util.HashMap;
import java.util.Stack;

public class OperationList {
    Stack<String> opList = new Stack<>();

    static HashMap<String, Integer> priTable = new HashMap<>();
    static {
        priTable.put("#", 0); priTable.put("neg", 4);
        priTable.put("+", 2); priTable.put("-", 2);
        priTable.put("*", 3); priTable.put("/", 3);
        priTable.put("==", 1); priTable.put("!=", 1);
        priTable.put("<", 1); priTable.put(">", 1);
        priTable.put("<=", 1); priTable.put(">=", 1);
    }

    public OperationList(){
        opList.add("#");
    }

    /**
     * 入栈
     * @param op 符号
     */
    public void addList(String op){
        opList.push(op);
    }

    /**
     * @return
     */
    public String popList(){
        return opList.pop();
    }

    /**
     * 栈顶符号
     * @return 符号
     */
    public String peek(){
        if(opList.size() > 0)
            return opList.peek();
        return "#";
    }

    public boolean isEmpty(){
        return this.opList.size() <= 1;
    }

    /**
     * 比较op1和op2的优先级，若op1大则返回true
     * @param op1
     * @param op2
     * @return
     */
    public static boolean cmpOp(String op1, String op2){
        return priTable.get(op1) >= priTable.get(op2);
    }

}
