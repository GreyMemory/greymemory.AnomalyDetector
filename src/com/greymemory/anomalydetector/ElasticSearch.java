/*
 * Copyright (c) 2015 Anton Mazhurin to present
 *   * 
 */
package com.greymemory.anomalydetector;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.indices.IndicesExists;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author anton
 */
public class ElasticSearch {
    
    private JestClient client;
    
    public ElasticSearch(){
    }
    
    public void connect(String host, int port, String user, String password)  {
        
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(
            new HttpClientConfig.Builder(host/* + ":" + port*/)
                .defaultCredentials(user, password)
                .build()
        );
        
        client = factory.getObject();
    }
    
    public void disconnect(){
    }
    
    float get_http_response_rate(String target_host, Date date, int num_minutes){
        
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        String indexDate = df.format(date);

        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'");
        String queryDate = df1.format(date);

        String querySuccessful = 

            "{" +
            "       \"query\": {" +
            "	\"filtered\": {" +
            "		\"query\": {" +
            "			\"query_string\": {" +
            "				\"query\": \"client_request_host : *"+target_host+" AND (http_response_code: 2* or http_response_code: 3*)\"," +
            "				\"analyze_wildcard\": true "+
            "			} "+
            "		}, "+
            "		\"filter\": {" +
            "			\"bool\": {"+ 
            "				\"must\": [ "+
            "				{" +
            "					\"query\": {" +
            "						\"match\": {" +
            "							\"dnet\": {" +
            "								\"query\": \"deflect1\", "+
            "								\"type\": \"phrase\" "+
            "							} "+
            "						} "+
            "					} "+
            "				},"+
            "				{" +
            "					\"range\": {" +
            "						\"@timestamp\": {" +
                
            "							\"gte\": \"" + queryDate + "||-"+Integer.toString(num_minutes)+"m" + "\", "+
            "							\"lte\": \"" + queryDate + "\" "+
                
            //"                                                     \"gte\": \"now-1m" + "\", "+
            //"							\"lte\": \"now\" "+

                "						} "+
            "					} "+
            "				}], "+
            "				\"must_not\": [] "+
            "			} "+
            "		} "+
            "	} "+
            "} "+
             "}";

        Search searchSuccessful = new Search.Builder(querySuccessful)
                .addIndex("deflect.log-"+indexDate).addType("deflect_access")
                .build();

        String queryTotal = 
            "{ \"query\": { "+
            "	\"filtered\": { "+
            "		\"query\": {" +
            "			\"query_string\": {" +
            "				\"query\": \"client_request_host : *"+target_host+"\"," +
            "				\"analyze_wildcard\": true "+
            "			} "+
            "		}, "+
                
            "		\"filter\": { "+
            "			\"bool\": { "+
            "				\"must\": [ "+
            "                { "+
            "                	\"query\": { "+
            "                		\"match\": { "+
            "                			\"dnet\": { "+
            "                				\"query\": \"deflect1\", "+
            "                				\"type\": \"phrase\" "+
            "                			} "+
            "                		} "+
            "                	} "+
            "                }, "+
            "                { "+
            "                	\"range\": { "+
            "                		\"@timestamp\": { "+
"					\"gte\": \"" + queryDate + "||-"+Integer.toString(num_minutes)+"m" + "\", "+
"					\"lte\": \"" + queryDate + "\" "+
            "                		} "+
            "                	} "+
            "                } "+
            "                ], "+
            "                \"must_not\": [] "+
            "            } "+
            "        } "+
            "    } "+
            "} "+
            "}";
                

        float rate = 1f;
        int num_success = 0;
        int num_total = 0;
        
        Search searchTotal = new Search.Builder(queryTotal)
                .addIndex("deflect.log-"+indexDate).addType("deflect_access")
                .build();
        try {
            JestResult resultSuccessful = client.execute(searchSuccessful);
//            System.out.println(resultSuccessful.getJsonString());
            if (resultSuccessful.isSucceeded()){
                JsonObject resultJson = resultSuccessful.getJsonObject();
                JsonObject hits = resultJson.getAsJsonObject("hits");
                if(hits != null){
                    JsonPrimitive total = hits.getAsJsonPrimitive("total");
                    if(total != null && total.isNumber())
                        num_success = total.getAsInt();
                }
            }
            
            JestResult resultTotal = client.execute(searchTotal);
//            System.out.println(resultTotal.getJsonString());
            if (resultTotal.isSucceeded()){
                JsonObject resultJson = resultTotal.getJsonObject();
                JsonObject hits = resultJson.getAsJsonObject("hits");
                if(hits != null){
                    JsonPrimitive total = hits.getAsJsonPrimitive("total");
                    if(total != null && total.isNumber())
                        num_total = total.getAsInt();
                }
            }
            if(num_total != 0){
                rate = num_success*1.0f/num_total;
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(ElasticSearch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ElasticSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return rate;
    }
    
}
