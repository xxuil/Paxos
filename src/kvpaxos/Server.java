package kvpaxos;
import paxos.Paxos;
import paxos.State;
import java.util.*;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements KVPaxosRMI {

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;

    String[] servers;
    int[] ports;
    KVPaxosRMI stub;

    // Your definitions here
    boolean DEBUG = true;
    int oldID;
    Op va;
    Map<String, Integer> KVlog;
    Map<Integer, Integer> iLog;
    Map<Integer, Object> rLog;



    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        // Your initialization code here
        this.oldID = -1;
        this.va = null;
        this.KVlog = new HashMap<>();
        this.iLog = new HashMap<>();
        this.rLog = new HashMap<>();

        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Op wait(int seq){
        int to = 10;
        while(true){
            Paxos.retStatus ret = this.px.Status(seq);
            if(ret.state == State.Decided){
              return Op.class.cast(ret.v);
            } try {
                Thread.sleep(to);
            } catch(Exception e) {
                e.printStackTrace();
            }
            if(to < 1000){
                to = to * 2;
            }
        }
    }

    public Op start(int seq, Object v){
        px.Start(seq, v);
        Op o = wait(seq);
        return o;
    }

    public void agree(Op o){
        while(true){
            int seq = px.Max() + 1;

            Op p = start(seq, o);

            if(p.equals(o)){
                break;
            }
        }
    }

    public Object apply(Op o){
        if(o.op == "Get"){
            return KVlog.get(o.key);
        } else {
            KVlog.put(o.key, o.value);
        }

        Object max = iLog.get(o.ClientID);

        if(max != null){
            int m = (int) max;
            if(m < o.ClientSeq){
                iLog.put(o.ClientID, o.ClientSeq);
            }
        } else {
            iLog.put(o.ClientID, o.ClientSeq);
        }
        return null;
    }


    // RMI handlers
    public Response Get(Request req){
        // Your code here
        mutex.lock();
        Response res;
        boolean ok;

        Op op = new Op("Get", req.seq, req.ID, req.key, null);
        int ID = req.ID;

        Object m = iLog.get(ID);
        if(m != null && req.seq <= (int) m){
            ok = true;
            res = new Response(ok,KVlog.get(op.key));
            return res;
        }

        int sq = oldID + 1;

        while(true){
            px.Start(sq, op);
            Op decided = wait(sq);

            if(op.equals(decided)){
                break;
            }
        }

        while(oldID < sq){
            Op decided = wait(oldID + 1);
            apply(decided);
            oldID++;
        }

        res = new Response(true, req.v);
        px.Done(oldID);

        mutex.unlock();
        return res;
    }

    public Response Put(Request req){
        // Your code here
        mutex.lock();
        Response res;

        Op op = new Op("Put", req.seq, req.ID, req.key, req.v);
        int ID = req.ID;

        Object m = iLog.get(ID);
        if(m != null && req.seq <= (int) m){
            res = new Response(true, req.v);
            return res;
        }

        int sq = oldID + 1;

        while(true){
            px.Start(sq, op);
            Op decided = wait(sq);

            if(op.equals(decided)){
                break;
            }
            sq++;
        }

        res = new Response(true, req.v);

        mutex.unlock();
        return res;
    }
}
