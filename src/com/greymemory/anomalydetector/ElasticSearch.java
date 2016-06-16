/*
 * Copyright (c) 2015 Anton Mazhurin to present
 *   * 
 */
package com.greymemory.anomalydetector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 *
 * @author anton
 */
public class ElasticSearch {
    private TransportClient client;
    
    public ElasticSearch(){
    }
    
    public void connect(String host, int port, String user, String password) throws UnknownHostException {
        try{
            Settings settings;
            
            settings = Settings.builder()
                .put("client.transport.sniff", true)
                //.put("cluster.name", "my-cluster").build()
                    .build();
            
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));                
            
        } catch (Exception ex) {
            Logger.getLogger(AnomalyDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
/*        
        TransportClient client = TransportClient.builder()
            .settings(Settings.builder().build())
            .build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

        String token = basicAuthHeaderValue(user, new SecuredString("changeme".toCharArray()));

        client.prepareSearch().putHeader("Authorization", token).get();




        client = TransportClient.builder().build()
            .addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName(host), port));
        
   */     

//        client = TransportClient.builder().build()
//            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

    }
    
    public void disconnect(){
        client.close();        
    }
    
    void test(){
        
        
        
        
    }
    
}
