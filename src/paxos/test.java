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
                    if(counter > 0 && !v.equals(ret.v)){
                        int xxx = 0;
                    }
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

    private static void waitmajority(Paxos[] pxa, int seq){
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
        final int npaxos = 6;
        Paxos[] pxa = initPaxos(npaxos);

        System.out.println("Test: Forgetting ...");

        for(int i = 0; i < npaxos; i++){
            int m = pxa[i].Min();
            assertFalse("Wrong initial Min() " + m, m > 0);
        }

        pxa[0].Start(0,"00");
        pxa[1].Start(1,"11");
        pxa[2].Start(2,"22");
        pxa[0].Start(6,"66");
        pxa[1].Start(7,"77");

        waitn(pxa, 0, npaxos);
        for(int i = 0; i < npaxos; i++){
            int m = pxa[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        waitn(pxa, 1, npaxos);
        for(int i = 0; i < npaxos; i++){
            int m = pxa[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        for(int i = 0; i < npaxos; i++){
            pxa[i].Done(0);
        }

        for(int i = 1; i < npaxos; i++){
            pxa[i].Done(1);
        }

        for(int i = 0; i < npaxos; i++){
            pxa[i].Start(8+i, "xx");
        }

        boolean ok = false;
        for(int iters = 0; iters < 12; iters++){
            ok = true;
            for(int i = 0; i < npaxos; i++){
                int s = pxa[i].Min();
                if(s != 1){
                    ok = false;
                }
            }
            if(ok) break;
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        assertFalse("Min() did not advance after Done()", ok != true);
        System.out.println("... Passed");
        cleanup(pxa);


        System.out.println("END");
        System.exit(0);
    }
}
