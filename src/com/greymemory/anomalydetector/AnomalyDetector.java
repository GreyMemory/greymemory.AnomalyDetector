/*
 * Copyright(c) 2015 Anton Mazhurin 
 * 
 */

package com.greymemory.anomalydetector;

import com.greymemory.anomaly.DataSourceCSV;
import com.greymemory.anomaly.AnomalyConsumer;
import com.greymemory.anomaly.IndividualAnomaly;
import com.greymemory.anomaly.Anomaly;
import com.greymemory.evolution.Evolver;
import com.greymemory.evolution.Gene;
import com.greymemory.evolution.Individual;
import com.greymemory.evolution.Population;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.in;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

import org.json.*;

/**
 *
 * @author alm
 */
public class AnomalyDetector implements AnomalyConsumer{
    
    private Properties config; 
    private void read_config(){
        // create default config
        config = new Properties();
        
        String config_file = "AnomalyDetector.config";

        // read existing config
        File configFile = new File(config_file);
        if(configFile.exists()){
            try {
                FileReader reader = new FileReader(configFile);
                config.load(reader);
                reader.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }            
            return;
        }
       
        // save the default config
        config.setProperty("input_file", "./data/http_response_ratio.csv");
        config.setProperty("output_file", "./anomaly_log.csv");
        config.setProperty("threshold", "0.95");
        
        configFile = new File(config_file);
        try {
            FileWriter writer = new FileWriter(configFile);
            config.store(writer, "Anomaly Detector configuration");
            writer.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    
    public IndividualAnomaly CreateTrafficIndividual(String file_input, 
            String file_output, Date start_from, int max_samples){
        IndividualAnomaly individual = new IndividualAnomaly(
                file_input,
                file_output, start_from, 10000.0, 2000, max_samples);
                
        individual.genome.genes.add(new Gene("resolution", 10.00f, 90f, 50f));
        individual.genome.genes.add(new Gene("activation", 5f, 30f, 6f));
        
        individual.genome.genes.add(new Gene("activation_day_of_week", 0f, 7f, 7f));
        individual.genome.genes.add(new Gene("activation_hour", 1f, 24f, 24f));
        
        individual.genome.genes.add(new Gene("num_hard_locations", 7f, 90f, 76f));
        individual.genome.genes.add(new Gene("window", 1f, 9f, 1f));
        individual.genome.genes.add(new Gene("forgetting_rate", 1000f, 5000f, 300));
        
        individual.genome.genes.add(new Gene("anomaly_window", 1000f, 5000f, 2200));
        
        individual.genome.genes.add(new Gene("training_period", 1000f, 1000f, 1000));

/*
Best in generation 13: 1.155960 : (-0.003059)
resolution = 100.000000
activation = 35.000000
activation_day_of_week = 7.000000
activation_hour = 16.386154
num_hard_locations = 36.154537
window = 1.555318
forgetting_rate = 4876.823730
anomaly_window = 4266.312012
training_period = 5803.479980
        */
                
        return individual;
    }
    
    public IndividualAnomaly CreateHTTPResponceIndividual(
        String file_input, String file_output, Date start_from, 
            int max_samples){
        
        IndividualAnomaly individual = new IndividualAnomaly(
                file_input,
                file_output, start_from, 0.1, 0.88, max_samples);
                
        individual.genome.genes.add(new Gene("resolution", 0.0000001f, 0.0001f, 0.000054f));
        individual.genome.genes.add(new Gene("activation", 3f, 50f, 29f));
        
        individual.genome.genes.add(new Gene("activation_day_of_week", 0f, 7f, 3f));
        individual.genome.genes.add(new Gene("activation_hour", 1f, 24f, 21f));
        
        individual.genome.genes.add(new Gene("num_hard_locations", 7f, 90f, 96f));
        individual.genome.genes.add(new Gene("window", 1f, 3f, 2f));
        individual.genome.genes.add(new Gene("forgetting_rate", 1000f, 2000f, 200));
        
        individual.genome.genes.add(new Gene("anomaly_window", 1000f, 3000f, 2600));
        
        individual.genome.genes.add(new Gene("training_period", 10f, 1, 10));
        
        return individual;
    }

    public void monitor(){
        
      
        System.out.println("Output file: " + config.getProperty("output_file"));
        System.out.println("Monitoring file: " + config.getProperty("input_file"));
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -60);
        Date start_from = cal.getTime();
        
        IndividualAnomaly individual = 
                CreateHTTPResponceIndividual(
                    config.getProperty("input_file"),
                    config.getProperty("output_file"), 
                    start_from, -1);
        individual.channel = "http_responce";

        DataSourceCSV data_source = null;
        try {
            individual.create_xdm();
            individual.threshold = Double.parseDouble(config.getProperty("threshold", "0.9"));
            
            data_source = new DataSourceCSV(
                    start_from, 
                    true, // monitoring
                    individual.input_file);
            
            individual.clear_log();

            data_source.addListener(individual);
            
            individual.addListener(this);

            System.out.println("Running...");
            data_source.start();
            data_source.join();
        } catch (InterruptedException ex) {
            //Logger.getLogger(IndividualAnomaly.class.getName()).log(Level.SEVERE, null, ex);
            if(data_source != null){
                try {
                    data_source.interrupt();
                    data_source.join();
                } catch (InterruptedException ex1) {
                    //Logger.getLogger(IndividualAnomaly.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(IndividualAnomaly.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
        }
                
        individual.clear_xdm();
        System.out.println("Done.");
    }
    
    public void run(String[] args) {
        
        System.out.println("Copyright (c) 2015 Anton Mazhurin");
        System.out.println("Anomaly Detector. version 1.1");
        
        read_config();
        
        //if(args.length > 0){
        //   config.setProperty("input_file", args[0]);
        //   System.out.println("Input file: " + args[0]);
        //}
        
        //send_zmq_message("Grey Memory: testing zmq");
        //monitor();
      
        
        ElasticSearch es = new ElasticSearch();
        es.connect(config.getProperty("es_host"), 
            Integer.parseInt(config.getProperty("es_port")),
            config.getProperty("es_user"), config.getProperty("es_password"));
        
        Date d = UTC_time.GetUTCdatetimeAsDate();
        float rate = es.get_http_response_rate("bdsmovement.net", d, 1);
        es.disconnect();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        
        try {
            AnomalyDetector detector = new AnomalyDetector();
            detector.run(args);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private boolean is_date_before_launch(Date date){
        long diff = date_launched.getTime() - date.getTime();
        return diff > 0;
    }
    
    private void send_emails(Anomaly anomaly){
        if(!anomaly.start)
            return;
        
        String rate = Double.toString((int)(anomaly.anomaly_rate*100)/100.0);
        String title = "GreyMemory: Anomaly detected(HTTP Responce): " +
                anomaly.sample.get_date_UTC() + ", Rate:" + rate;
        String message = "GreyMemory: Anomaly detected(HTTP responce).\n\n" +
                "Timestmap: " + anomaly.sample.get_date_UTC() + "\n\n" +
                "Anomaly rate: " + rate;
        
        // do not send emails on events before the launch
        if(is_date_before_launch(anomaly.sample.date))
            return;
       
        // do not send emails too often
        Date now = new Date();
        if(last_email != null){
            long diff = now.getTime() - last_email.getTime();
            if(diff < 1000*60*
                    30 // minutes
                    ){
                return;
            }
        }
        
        last_email = now;
        ArrayList<String> emails;
        for(int i = 1; i < 20; i++){
            String email = config.getProperty("mail_alert" + Integer.toString(i));
            if(email != null && email.length() > 0){
                try {
                    // TEST EMAIL
                    GoogleMail.Send(
                            "mazhurin", "v0dkamart1n12752831",
                            email,
                            title,
                            message
                    );
                } catch (MessagingException ex) {
                    Logger.getLogger(AnomalyDetector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private Date last_email = null;
    private Date date_launched = new Date();
    
    //private Anomaly anomaly = null;
    
    @Override
    public void OnAnomaly(Anomaly anomaly) {
      /*  
        if(this.anomaly == null){
            // no anomaly state
            if(anomaly.start){
                // new anomaly
                this.anomaly = anomaly;
            } else {
                // ignore extra stops
                return;
            }
        } else {
            // anomaly state
            if(anomaly.start){
                // ignore extra starts
                return;
            } else{
                // stop anomaly
                
            }
        }
        */
        
        
        System.out.println("GreyMemory: Anomaly detected(HTTP Responce): " + anomaly.sample.get_date_UTC() + 
                ", Rate:" + Double.toString((int)(anomaly.anomaly_rate*100)/100.0)
                 + (anomaly.start ? ", START" : ", STOP"));
        
        send_emails(anomaly);
        
        send_to_bothound(anomaly);
        
    }
    
    protected void send_zmq_message(String message){
        String hostName = "localhost";
        int portNumber = 22623;

        try {
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            
            out.println(message);
            out.flush();
            
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException ex) {
            Logger.getLogger(AnomalyDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void send_to_bothound(Anomaly anomaly){
        // do not send emails on events before the launch
        //if(is_date_before_launch(anomaly.sample.date))
        //    return;
        
        JSONObject root = new JSONObject();
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // { "message type": "anomaly started", "dnet": STRING, "channel": STRING, "anomaly rate" : FLOAT, "time stamp" : "YYYY-MM-DD HH:MM:SS" }
            root.put("message type", anomaly.start ? "anomaly_started" : "anomaly_stopped");
            root.put("dnet", "");
            root.put("channel", "HTTP_response");
            root.put("anomaly_rate", anomaly.anomaly_rate);
            root.put("time_stamp", anomaly.sample.timestamp);
            root.put("date", anomaly.sample.get_date_UTC());
            
            send_zmq_message(root.toString());
            
        } catch (JSONException ex) {
            Logger.getLogger(AnomalyDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
/*
nohup sh anomaly_detector.sh &
*/
