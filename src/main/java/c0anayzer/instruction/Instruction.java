package c0anayzer.instruction;

import java.util.HashMap;
import java.util.Objects;

public class Instruction {
    public static HashMap<String, Number> INSTRUCT = new HashMap<>();
    static{
        INSTRUCT.put("nop", 0x00);INSTRUCT.put("push", 0x01);
        INSTRUCT.put("pop", 0x02);INSTRUCT.put("popn", 0x03);
        INSTRUCT.put("dup", 0x04);INSTRUCT.put("loca", 0x0a);
        INSTRUCT.put("arga", 0x0b);INSTRUCT.put("globa", 0x0c);
        INSTRUCT.put("load_8", 0x10);INSTRUCT.put("load_16", 0x11);
        INSTRUCT.put("load_32", 0x12);INSTRUCT.put("load_64", 0x13);
        INSTRUCT.put("store_8", 0x14);INSTRUCT.put("store_16", 0x15);
        INSTRUCT.put("store_32", 0x16);INSTRUCT.put("store_64", 0x17);
        INSTRUCT.put("alloc", 0x18);INSTRUCT.put("free", 0x19);
        INSTRUCT.put("stackalloc", 0x1a);INSTRUCT.put("add_i", 0x20);
        INSTRUCT.put("sub_i", 0x21);INSTRUCT.put("mul_i", 0x22);
        INSTRUCT.put("div_i", 0x23);INSTRUCT.put("add_f", 0x24);
        INSTRUCT.put("sub_f", 0x25);INSTRUCT.put("mul_f", 0x26);
        INSTRUCT.put("div_f", 0x27);INSTRUCT.put("div_u", 0x28);
        INSTRUCT.put("shl", 0x29);INSTRUCT.put("shr", 0x2a);
        INSTRUCT.put("and", 0x2b);INSTRUCT.put("or", 0x2c);
        INSTRUCT.put("xor", 0x2d);INSTRUCT.put("not", 0x2e);
        INSTRUCT.put("cmp_i", 0x30);
        INSTRUCT.put("cmp_f", 0x31);INSTRUCT.put("cmp_u", 0x32);
        INSTRUCT.put("neg_i", 0x34);INSTRUCT.put("neg_f", 0x35);
        INSTRUCT.put("itof", 0x36);INSTRUCT.put("ftoi", 0x37);
        INSTRUCT.put("shrl", 0x38);INSTRUCT.put("set_lt", 0x39);
        INSTRUCT.put("set_gt", 0x3a);INSTRUCT.put("br", 0x41);
        INSTRUCT.put("br_false", 0x42);INSTRUCT.put("br_true", 0x43);
        INSTRUCT.put("call", 0x48);INSTRUCT.put("ret", 0x49);
        INSTRUCT.put("callname", 0x4a);INSTRUCT.put("scan_i", 0x50);
        INSTRUCT.put("scan_c", 0x51);INSTRUCT.put("scan_f", 0x52);
        INSTRUCT.put("print_i", 0x54);INSTRUCT.put("print_c", 0x55);
        INSTRUCT.put("print_f", 0x56);INSTRUCT.put("print_s", 0x57);
        INSTRUCT.put("println", 0x58);INSTRUCT.put("panic", 0xfe);
    }

    private Operation opt;
    long x;
    int size;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0L;
        size = 0;
    }

    public Instruction(Operation opt, int x, int size) {
        this.opt = opt;
        this.x = x;
        this.size = size;
    }
    public Instruction(Operation opt, long x, int size) {
        this.opt = opt;
        this.x = x;
        this.size = size;
    }
    public Instruction(Operation opt, double d){
        this.opt = opt;
        this.x = Double.doubleToLongBits(d);
        this.size = 8;
    }

    public Instruction(){
        this.opt = Operation.nop;
        this.x = 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Long getX() {
        return x;
    }

    public int getOptValue(){
        return INSTRUCT.get(String.valueOf(this.opt)).intValue();
    }

    public int getIntX(){
        return (int)x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public boolean hasX(){
        switch (this.opt) {
            case nop:
            case pop:
            case dup:
            case load_8:
            case load_16:
            case load_32:
            case load_64:
            case store_8:
            case store_16:
            case store_32:
            case store_64:
            case alloc:
            case free:
            case add_i:
            case sub_i:
            case mul_i:
            case div_i:
            case add_f:
            case sub_f:
            case mul_f:
            case div_f:
            case div_u:
            case shl:
            case shr:
            case and:
            case or:
            case xor:
            case not:
            case inv:
            case cmp_i:
            case cmp_f:
            case cmp_u:
            case neg_i:
            case neg_f:
            case itof:
            case ftoi:
            case shrl:
            case set_lt:
            case set_gt:
            case ret:
            case scan_i:
            case scan_c:
            case scan_f:
            case print_i:
            case print_c:
            case print_f:
            case print_s:
            case println:
            case panic:
                return false;
            case push:
            case popn:
            case loca:
            case arga:
            case globa:
            case stackalloc:
            case br:
            case br_true:
            case br_false:
            case call:
            case callname:
                return true;
            default:
                return false;
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        switch (this.opt) {
            case nop:
            case pop:
            case dup:
            case load_8:
            case load_16:
            case load_32:
            case load_64:
            case store_8:
            case store_16:
            case store_32:
            case store_64:
            case alloc:
            case free:
            case add_i:
            case sub_i:
            case mul_i:
            case div_i:
            case add_f:
            case sub_f:
            case mul_f:
            case div_f:
            case div_u:
            case shl:
            case shr:
            case and:
            case or:
            case xor:
            case not:
            case inv:
            case cmp_i:
            case cmp_f:
            case cmp_u:
            case neg_i:
            case neg_f:
            case itof:
            case ftoi:
            case shrl:
            case set_lt:
            case set_gt:
            case ret:
            case scan_i:
            case scan_c:
            case scan_f:
            case print_i:
            case print_c:
            case print_f:
            case print_s:
            case println:
            case panic:
                return String.format("%s", this.opt);
                //return String.format("%s",INSTRUCT.get(this.opt.toString()));
            case push:
            case popn:
            case loca:
            case arga:
            case globa:
            case stackalloc:
            case br:
            case br_true:
            case br_false:
            case call:
            case callname:
                return String.format("%s %s", this.opt, this.x);
                //return String.format("%s%s",INSTRUCT.get(this.opt.toString()),this.x);
            default:
                return "nop";
        }
    }



    public static void main(String[] args){
        System.out.println(Double.doubleToLongBits(1.1010));
    }
}
