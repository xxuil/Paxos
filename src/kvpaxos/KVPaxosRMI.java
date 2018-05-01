package kvpaxos;

/*
 * EE 360P HW 5 Paxos Assignment
 * Date: 04/30/2018
 * Name: Xiangxing Liu
 * EID1: xl5587
 * Name: Kravis Cho
 * EID2: kyc375
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the interface of KVPaxos RMI call. You should implement each method defined below.
 * Please don't change the interface.
 */
public interface KVPaxosRMI extends Remote{
    Response Get(Request req) throws RemoteException;
    Response Put(Request req) throws RemoteException;
}
