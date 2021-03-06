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
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=22L;
    // your data here
    boolean state;  //True = accept, False = reject
    int v;

    // Your constructor and methods here
    public Response(boolean s, int v){
        this.state = s;
        this.v = v;
    }


    @Override
    public String toString() {
        return "State:" + state + " v:" + v;
    }
}
