package com.company.discard;

import com.company.discard.client.DiscardClient;
import com.company.discard.server.DiscardServer;

/**
 * Created by emrahsoytekin on 03/06/2017.
 */
public class DiscardExecutor {

    public static void main(String args[]) throws InterruptedException {
        int port = 5000;
        String host = "localhost";
        new DiscardServer (5000).run();
    }
}
