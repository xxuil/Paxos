package paxos;

import static org.junit.Assert.assertFalse;

public class test {
    private static int ndecided(Paxos[] pxa, int seq){
        int counter = 0;
        Object v = null;
        Paxos.retStatus ret;
        for(int i = 0; i < pxa.length; i++){
            if(pxa[i] != null){
                ret = pxa[i].Status(seq);
                if(ret.state == State.Decided) {
                    assertFalse("decided values do not match: seq=" + seq + " i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private static void waitn(Paxos[] pxa, int seq, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(pxa, seq) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }
            if(to < 1000){
                to = to * 2;
            }
        }

        int nd = ndecided(pxa, seq);
        assertFalse("too few decided; seq=" + seq + " ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitmajority(Paxos[] pxa, int seq){
        waitn(pxa, seq, (pxa.length/2) + 1);
    }

    private static void cleanup(Paxos[] pxa){
        for(int i = 0; i < pxa.length; i++){
            if(pxa[i] != null){
                pxa[i].Kill();
            }
        }
    }

    private static Paxos[] initPaxos(int npaxos){
        String host = "127.0.0.1";
        String[] peers = new String[npaxos];
        int[] ports = new int[npaxos];
        Paxos[] pxa = new Paxos[npaxos];
        for(int i = 0 ; i < npaxos; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < npaxos; i++){
            pxa[i] = new Paxos(i, peers, ports);
        }
        return pxa;
    }

    public static void main(String[] args){
        final int npaxos = 5;
        Paxos[] pxa = initPaxos(npaxos);

        System.out.println("Test: Single proposer ...");
        pxa[0].Start(0, "hello");
        waitn(pxa, 0, npaxos);
        System.out.println("... Passed");

        System.out.println("Test: Many proposers, same value ...");
        for(int i = 0; i < npaxos; i++){
            pxa[i].Start(1, 77);
        }
        waitn(pxa, 1, npaxos);
        System.out.println("... Passed");

        cleanup(pxa);
        System.out.println("END");
        System.exit(0);
    }
}
