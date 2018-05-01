package paxos;

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
 * This is the interface of Paxos RMI call. You should implement each method defined below.
 * Prepare is the RMI that proposer sends prepare request to acceptors.
 * Accept is the RMI that proposer sends accept request to acceptors.
 * Decide is the RMI that proposer broadcasts decision once consensus reaches.
 * Please don't change the interface.
 */
public interface PaxosRMI extends Remote{
    Response Prepare(Request req) throws RemoteException;
    Response Accept(Request req) throws RemoteException;
    Response Decide(Request req) throws RemoteException;
}
