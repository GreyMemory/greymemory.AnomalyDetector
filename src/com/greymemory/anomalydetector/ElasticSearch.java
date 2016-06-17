/*
 * Copyright (c) 2015 Anton Mazhurin to present
 *   * 
 */
package com.greymemory.anomalydetector;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 *
 * @author anton
 */
public class ElasticSearch {
    
    public ElasticSearch(){
    }
    
    public void connect(String host, int port, String user, String password)  {
        
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(
            new HttpClientConfig.Builder(host + ":" + port)
                .defaultCredentials(user, password)
                .build()
        );
        
        JestClient client = factory.getObject();
        
        
        /*
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
        */
        
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
    }
    
    void test(){
        
        
        
        
    }
    
}
