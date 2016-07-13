/*
 * Copyright (c) 2015 Anton Mazhurin to present
 *   * 
 */
package com.greymemory.anomalydetector;

import com.greymemory.anomaly.DataSample;
import com.greymemory.anomaly.DataSource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class DataSourceElasticSearch extends DataSource{
    
    private String host;
    private int port;
    private String user;
    private String password;
    private Calendar calendar;
    private String target_host;
    int samle_rate_in_minutes = 5;
    private boolean use_training_mode;
    
    public String get_target_host(){return target_host;}
            
    public DataSourceElasticSearch(Date dateStart, 
            int samle_rate_in_minutes,
            boolean monitoring,
            boolean use_training_mode,
            String target_host,
            String host, int port, String user, String password) {
        super(dateStart, monitoring);
        this.host = host;
        this.user = user;
        this.port = port;
        this.password = password;
        this.target_host = target_host;
        this.use_training_mode = use_training_mode;
        this.samle_rate_in_minutes= samle_rate_in_minutes;
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }
    
    private DataSample get_sample(Date date, float value){
        DataSample sample = new DataSample();
        sample.date = date;
        sample.timestamp = sample.date.getTime()/1000;
        calendar.setTime(sample.date);
        sample.data = new double[1+2];

        sample.data[0] = calendar.get(Calendar.DAY_OF_WEEK);        

        // hour
        sample.data[1] = calendar.get(Calendar.HOUR);

        sample.data[2] = value;
        return sample;
    }
    
    public static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }
    
    
    private boolean training_period = false;
    @Override
    public boolean is_training_period (){
        return training_period;
    }
    
    
    @Override
    public void run() {
        try {
            ElasticSearch es;
            es = new ElasticSearch();
            es.connect(host, port, user, password);
            
            Date dateCurrent = dateStart;
            
            if(use_training_mode)
                training_period = true;
            while(true){
                float rate = es.get_http_response_rate(target_host, dateCurrent, 
                        samle_rate_in_minutes);
                OnData(get_sample(dateCurrent, rate), this);
                
                dateCurrent = addMinutesToDate(samle_rate_in_minutes, dateCurrent);
                Date dateNow = UTC_time.GetUTCdatetimeAsDate();
                if(dateNow.before(dateCurrent)){
                    break;
                }
            }
            training_period = false;
            
            System.out.println("\n\n\nMonitoring " + target_host + "..." );
            // read the historic data from the files
            if(monitoring){
                while(true){
                    if(Thread.interrupted())
                        break;
                    Date dateNow = UTC_time.GetUTCdatetimeAsDate();
                    float rate = es.get_http_response_rate(target_host, dateNow, samle_rate_in_minutes);
                    System.out.println("Host: " + target_host + ",rate = " + Float.toString(rate));
                    OnData(get_sample(dateNow, rate), this);
                    Thread.sleep(samle_rate_in_minutes*60*1000);
                }
            }
            
            es.disconnect();
            
        } catch (InterruptedException ex) {
        }
    }
    
    
}
