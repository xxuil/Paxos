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
    int na;
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
        this.na = -1;
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


    private int ndecided(Paxos[] pxa, int seq){
        int counter = 0;
        Object v = null;
        Paxos.retStatus ret;
        for(int i = 0; i < pxa.length; i++){
            if(pxa[i] != null){
                ret = pxa[i].Status(seq);
                if(ret.state == State.Decided) {
                    //assertFalse("decided values do not match: seq=" + seq + " i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    if(counter > 0 && !v.equals(ret.v)){
                        throw new RuntimeException();
                    }
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void wait(Paxos[] pxa, int seq, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(pxa, seq) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
                System.out.print("ERROR");
            }
            if(to < 1000){
                to = to * 2;
            }
        }
        int nd = ndecided(pxa, seq);
        //assertFalse("too few decided; seq=" + seq + " ndecided=" + nd + " wanted=" + wanted, nd < wanted);
        if(nd < wanted){
            throw new RuntimeException();
        }
    }


    // RMI handlers
    public Response Get(Request req){
        // Your code here
        mutex.lock();
        Response res;

        Op op;

        mutex.unlock();
        return res;
    }

    public Response Put(Request req){
        // Your code here
        mutex.lock();
        Response res;


        mutex.unlock();
        return res;
    }
}
