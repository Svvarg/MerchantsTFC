package com.aleksey.merchants.Extended;

/**
 *
 * @author Swarg
 */
public class EditPayParams {
    public int p1;
    public int p2;
    public int p3;
    public int p4;
    

    public EditPayParams(int p1, int p2, int p3, int p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;        
    }

    public EditPayParams() {
        this.p1 = 0;
        this.p2 = 0;
        this.p3 = 0;
        this.p4 = 0;        
    }
    
    public EditPayParams(int p1 ) {
        this.p1 = p1;
        this.p2 = 0;
        this.p3 = 0;
        this.p4 = 0;
    }
}
