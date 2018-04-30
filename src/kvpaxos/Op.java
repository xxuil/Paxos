package kvpaxos;
import java.io.Serializable;

/**
 * You may find this class useful, free to use it.
 */
public class Op implements Serializable{
    static final long serialVersionUID=33L;
    String op;
    int ClientSeq;
    int ClientID;
    String key;
    Integer value;

    public Op(String op, int ClientSeq, int ClintID, String key, Integer value){
        this.op = op;
        this.ClientSeq = ClientSeq;
        this.key = key;
        this.value = value;
        this.ClientID = ClintID;
    }

    public boolean equals(Op that) {
        if(this.key.equals(that.key)){
            if(that.value == null || this.value == null){
                if(this.value == null && that.value == null){
                    return true;
                }
                return false;
            }
            if(this.value.equals(that.value)){
                return true;
            }
        }
        return false;
    }
}
