package client.utility;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class CorbaHelper {
    private static ORB orb;
    private static POA rootPoa;

    public static void init(ORB orb, POA poa) {
        orb = orb; rootPoa = poa;
    }
    public static ORB getOrb()       { return orb; }
    public static POA getRootPoa()   { return rootPoa; }
}
