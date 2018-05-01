package kvpaxos;

/*
 * EE 360P HW 5 Paxos Assignment
 * Date: 04/30/2018
 * Name: Xiangxing Liu
 * EID1: xl5587
 * Name: Kravis Cho
 * EID2: kyc375
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicInteger;


public class Client {
    String[] servers;
    int[] ports;

    // Your data here
    static AtomicInteger ref = new AtomicInteger(0);
    int ID;
    int seq;


    public Client(String[] servers, int[] ports){
        this.servers = servers;
        this.ports = ports;
        // Your initialization code here
        this.ID = ref.getAndIncrement();
        this.seq = 0;
    }

    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;
        KVPaxosRMI stub;
        try{
            Registry registry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(KVPaxosRMI) registry.lookup("KVPaxos");
            if(rmi.equals("Get"))
                callReply = stub.Get(req);
            else if(rmi.equals("Put")){
                callReply = stub.Put(req);}
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            e.printStackTrace();
        }
        return callReply;
    }

    // RMI handlers
    public Integer Get(String key){
        // Your code here
        Request req = new Request(seq, ID, key);
        Response res;
        seq ++;
        while(true){
            for(int i = 0; i < ports.length; i++){
                res = Call("Get", req, i);
                if(res != null){
                    if(res.state){
                        return res.v;
                    }
                    else {
                    }
                }
            }
            ID = ref.getAndIncrement();
        }
    }

    public boolean Put(String key, Integer value){
        // Your code here
        Request req = new Request(seq, ID, key, value);
        Response res;
        seq ++;
        while(true){
            for(int i = 0; i < ports.length; i++){
                res = Call("Put", req, i);
                if(res != null){
                    if(res.state){
                        return true;
                    }
                }
            }
            ID = ref.getAndIncrement();
        }
    }

}
