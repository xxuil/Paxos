package kvpaxos;

/*
 * EE 360P HW 5 Paxos Assignment
 * Date: 04/30/2018
 * Name: Xiangxing Liu
 * EID1: xl5587
 * Name: Kravis Cho
 * EID2: kyc375
 */

import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=11L;
    // Your data here
    public String op;
    public int ID;
    public String key;
    public int v;
    public int seq;

    public Request(int seq, int ID, String k){
        this.op = "Get";
        this.seq = seq;
        this.ID = ID;
        this.key = k;
    }

    public Request(int seq, int ID, String k, int v){
        this.op = "Put";
        this.seq = seq;
        this.ID = ID;
        this.key = k;
        this.v = v;
    }

    @Override
    public String toString() {
        if(this.op.equals("Get")){
            return "Req Get ID:" + ID + " key: " + key;
        }
        return "Req Seq:";
    }
}
