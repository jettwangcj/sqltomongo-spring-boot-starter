package com.rrtv.common;

import lombok.Data;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

@Data
public class MongoParserResult {

   private Aggregation aggregation;

   private String collectionName;

   public MongoParserResult(){}

   public MongoParserResult(Aggregation aggregation, String collectionName){
       this.aggregation = aggregation;
       this.collectionName = collectionName;
   }



}
