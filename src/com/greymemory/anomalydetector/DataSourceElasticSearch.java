/*
 * Copyright (c) 2015 Anton Mazhurin to present
 *   * 
 */
package com.greymemory.anomalydetector;

import com.greymemory.anomaly.DataSample;
import com.greymemory.anomaly.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class DataSourceElasticSearch extends DataSource{
    
    public DataSourceElasticSearch(Date dateStart, boolean monitoring) {
        super(dateStart, monitoring);
        
        
    }
    
    @Override
    public void run() {
        try {
            
            // read the historic data from the files
            if(!monitoring)
                return;
            
            while(true){
                if(Thread.interrupted())
                    break;
                Thread.sleep(600);
                
            }
            
            
        } catch (InterruptedException ex) {
        }
    }
    
    
}
