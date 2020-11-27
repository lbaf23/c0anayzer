package c0anayzer.instruction;

public enum Operation {
    nop, push, pop, popn, dup, loca, arga, globa, load_8,
    load_16, load_32, load_64, store_8, store_16, store_32, store_64,
    alloc, free, stackalloc,
    add_i, sub_i, mul_i, div_i, add_f, sub_f, mul_f,
    div_f, div_u, shl, shr, and, or, xor, not, inv, cmp_i,
    cmp_f, cmp_u, neg_i, neg_f, itof, ftoi, shrl, set_lt, set_gt,
    br, br_false, br_true, call, ret, callname, scan_i, scan_c,
    scan_f, print_i, print_c, print_f, print_s, println, panic

}
