package kvpaxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=11L;
    // Your data here
    public int seq;
    public int n;
    public Object v;
    int done = -1;
    int me = -1;

    public Request(int seq, int n, Object v){
        this.seq = seq;
        this.n = n;
        this.v = v;
    }

    @Override
    public String toString() {
        return "Req Seq:" + seq + " n:" + n + " v:" + v;
    }
}
