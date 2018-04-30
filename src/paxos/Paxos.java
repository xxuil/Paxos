package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    int[] dones;
    boolean DEBUG = true;
    Set<Integer> seqSet;
    int np;
    int na;
    Object va;
    int maxSeq;
    static AtomicInteger n = new AtomicInteger(0);
    static AtomicInteger ID = new AtomicInteger(0);

    Map<Integer, Instance> log;

    public class Instance{
        int np;
        int na;
        Object va;
        State s;

        public Instance(int np, int na, Object va, State s){
            this.np = np;
            this.na = na;
            this.va = va;
            this.s = s;
        }
    }

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        this.seqSet = new HashSet<>();
        this.np = -1;
        this.na = -1;
        this.va = null;
        this.maxSeq = -1;
        this.log = new HashMap<>();
        this.dones = new int[peers.length];

        for(int i = 0; i < dones.length; i++){
            dones[i] = -1;
        }

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
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

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    public Response selfCall(Request req, String name){
        if(name.equals("Prepare")){
            return Prepare(req);
        }else if(name.equals("Accept")){
            return Accept(req);
        }else if(name.equals("Decide")){
            return Decide(req);
        }
        return null;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
        // Your code here
        if(seqSet.contains(seq)){
            return;
        }

        seqSet.add(seq);

        Proposer p = new Proposer(seq, value);
        Thread t = new Thread(p);
        t.start();
    }

    @Override
    public void run(){
        //Your code here
    }

    // RMI handler
    public Response Prepare(Request req){
        // your code here
        mutex.lock();
        Response res;
        Instance p;
        boolean ok;

        if(!log.containsKey(req.seq)){
            ok = true;
            p = new Instance(req.n, req.n, req.v, State.Pending);

        } else {
            p = log.get(req.seq);

            if(req.n > p.np){
                ok = true;
            } else {
                ok = false;
            }
        }

        if(ok){
            if(DEBUG){ System.out.println("Acceptor:" + me + " receive prepare:" + req + " Accepted"); }
            p.np = req.n;

            log.put(req.seq, p);

            res = new Response(true, p.np, p.na, null, p.va);
        } else {
            if(DEBUG){ System.out.println("Acceptor:" + me + " receive prepare:" + req + " Rejected"); }
            res = new Response(false, p.np, p.na, null, p.va);
        }

        /*
        if(req.n > np){
            if(DEBUG){ System.out.println("Acceptor:" + me + " receive prepare:" + req + " Accepted"); }
            np = req.n;
            res = new Response(true, np, na, null, va);
        } else {
            if(DEBUG){ System.out.println("Acceptor:" + me + " receive prepare:" + req + " Rejected"); }
            res = new Response(false, -1, -1, null, null);
        }
        */

        mutex.unlock();
        return res;
    }

    public Response Accept(Request req){
        // your code here
        mutex.lock();
        Response res;
        if(req.n >= np){
            np = req.n;
            na = req.n;
            va = req.v;
            this.maxSeq = req.seq;

            res = new Response(true, np, na, va, va);

            if(DEBUG){ System.out.println("Acceptor:" + me + " receive accept:" + req + " Accepted");
                System.out.println("Acceptor Response:" + res);}
        } else {
            res = new Response(false, -1, -1, null, null);

            if(DEBUG){ System.out.println("Acceptor:" + me + " receive accept:" + req + " Reject");
                System.out.println("Acceptor Response:" + res);}
        }
        mutex.unlock();
        return res;
    }

    public Response Decide(Request req){
        // your code here
        mutex.lock();
        Response res;
        res = new Response(true, np, na, va, va);

        if(DEBUG){ System.out.println("Acceptor:" + me + " decided:" + req);
            System.out.println("Acceptor Response:" + res);}

        mutex.unlock();
        return res;
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
        mutex.lock();
        if(seq > dones[me]){
            dones[me] = seq;
        }
        mutex.unlock();
    }


    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        // Your code here
        mutex.lock();
        int max = 0;
        for(Instance p : log.values()){
            if(p.seq > max){
                max = p.seq;
            }
        }
        mutex.unlock();
        return max;
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        // Your code here
        mutex.lock();
        int min = dones[me];

        for(int i : dones){
            if(i < min){
                min = i;
            }
        }

        for(Proposer p : log.values()){
            if(p.seq > min){
                continue;
            }
            if(!p.decided){
                continue;
            }

            log.values().remove(p);
        }

        mutex.unlock();
        return min + 1;
    }



    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
        // Your code here
        retStatus ret;
        mutex.lock();

        if(seq < Min()){
            ret = new retStatus(State.Forgotten, null);
        } else {
            Instance p = log.get(seq);
            if(p != null) {
                ret = new retStatus(p.s, p.va);
            } else {
                ret = new retStatus(State.Pending, null);
            }
        }

        mutex.unlock();
        return ret;
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }

    public class Proposer implements Runnable{
        int aCount;     //Num of Acceptors
        int seq;
        int pn;
        Object v;
        Boolean decided;
        State s;

        public Proposer(int seq, Object v){
            this.aCount = peers.length;
            this.seq = seq;
            this.v = v;
            this.pn = n.getAndIncrement();
            this.decided = false;
            this.s = State.Pending;
        }

        public Proposer(int seq, int n, Object v, boolean s){
            if(!s){
                throw new RuntimeException();
            }
            this.seq = seq;
            this.pn = n;
            this.v = v;
            this.decided = s;
            this.s = State.Decided;
        }

        @Override
        public void run() {
            if(DEBUG){
                System.out.println("Proposer seq:" + seq + " on Paxos:" + me + " starting");
            }

            while(true){
                /* Send Prepare(N) to all */
                Request req1 = new Request(this.seq, this.pn, null);
                Response res1;
                int count = 0;
                int replypNumber = pn;
                Object replyValue = v;

                if(DEBUG){ System.out.println("Proposer seq:" + seq + " on Paxos:" + me + " n:" + pn); }

                for(int i = 0; i < aCount; i++){
                    if(i == me){
                        res1 = selfCall(req1, "Prepare");
                    } else {
                        res1 = Call("Prepare", req1, i);
                    }

                    if(res1 == null)
                        continue;
                    if(res1.state){
                        count++;
                        if(res1.na > replypNumber){
                            replypNumber = res1.na;
                            replyValue = res1.va;
                        }
                    }
                }

                if(count > (aCount / 2)){
                    if(DEBUG){ System.out.println("Proposer seq:" + seq + " on Paxos:" + me + " n:" + pn + " prepared"); }

                    Request req2 = new Request(seq, pn, replyValue);
                    Response res2;
                    count = 0;

                    for(int i = 0; i < aCount; i++){
                        if(i == me){
                            res2 = selfCall(req2, "Accept");
                        } else {
                            res2 = Call("Accept", req2, i);
                        }
                        if(res2 == null)
                            continue;
                        if(res2.state){
                            count++;
                        }
                    }

                    if(count > (aCount / 2)){
                        if(DEBUG){ System.out.println("Proposer seq:" + seq + " on Paxos:" + me + " n:" + pn + " accepted"); }

                        Request req3 = new Request(this.seq, this.pn, replyValue);
                        Response res3;
                        count = 0;

                        res3 = selfCall(req3, "Decide");

                        if(res3.state){
                            count++;
                        }

                        for(int i = 0; i < aCount; i++){
                            if(i != me){
                                res3 = Call("Decide", req3, i);
                            }

                            if(res3 == null)
                                continue;

                            if(res3.state){
                                count++;
                            }
                        }

                        if(count > (aCount / 2)){
                            if(DEBUG){ System.out.println("Proposer seq:" + seq + " on Paxos:" + me + " n:" + pn + " Decided v:" + replyValue); }
                            decided = true;
                            s = State.Decided;
                            //v = v2;
                        }

                        break;
                    }
                }

                if(DEBUG){ System.out.println("Proposer seq:" + seq + " on Paxos:" + me + " n:" + n + " rejected"); }

                retStatus check = Status(seq);
                if(check.state == State.Decided){
                    break;
                }


                this.pn = n.getAndIncrement();
            }
        }
    }

}