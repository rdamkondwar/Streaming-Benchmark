//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package edu.wisc.streaming;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.tuple.Tuple;
//import javassist.bytecode.Descriptor.Iterator;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;


/**
 * This is a topology that does simple word frequency counts.
 * 
 * In this topology,
 * 1. The spout is a KafkaSpout which reads from the kafka.
 * 2. During every "nextTuple" call, each spout picks a word from the kafka and emits it.
 * 3. Bolts maintain an in-memory map, which is keyed by the word emitted by the spouts,
 * and updates the count when it receives a tuple.
 */


public final class MyWordCountTopology2 {
  private MyWordCountTopology2() {
  }

  class Constants {
    public static final long ONE_25_MILLION= 1250000; /* One Million */
    public static final long ONE_MILLION= 1000000; /* One Million */
    public static final long HALF_MILLION= 500000; /* One Million */
    public static final long QUARTER_MILLION= 250000; /* One Million */
    public static final long ONE_HUNDRED_THOUSAND= 100000; /* One Million */
    public static final long ONE_25_THOUSAND = 125000;
  };
  
  public static class DistributorBolt extends BaseRichBolt {
    public static long start_time = 0;
    private OutputCollector collector;
    public Map<String, Integer> countMap;
    public long ProcessedTupleCount;
    int done = 0;
    

    @SuppressWarnings("rawtypes")
      public void prepare(Map map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        collector = outputCollector;
        countMap = new HashMap<String, Integer>();
        ProcessedTupleCount = 0;
      }

    @Override
      public void execute(Tuple tuple) {
        String key = (String) tuple.getValue(0);
        ProcessedTupleCount++;
        if ( ProcessedTupleCount % 2 == 1) {
          collector.emit("stream1", new Values(key));
        } else {
          collector.emit("stream2", new Values(key));
        }
        // System.out.println("Processed '" + key + "' at distributor ");
        collector.ack(tuple);
      }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declerator) {
      declerator.declareStream("stream1", new Fields("field1"));
      declerator.declareStream("stream2", new Fields("field2"));
      // declerator.declareStream("B", new Fields("stream2"));
      //declerator.declare(new Fields("stream2"));
    }
    public long getProcessedTupleCount() { return ProcessedTupleCount; }
  }

  public static class ConsumerBolt extends BaseRichBolt {
    public static long start_time = 0;
    private OutputCollector collector;
    public Map<String, Integer> countMap;
    public long ProcessedTupleCount;
    String myId;
    int done = 0;

    ConsumerBolt(String id) {
      myId = new String(id);
    }
    @SuppressWarnings("rawtypes")
      public void prepare(Map map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        collector = outputCollector;
        countMap = new HashMap<String, Integer>();
        ProcessedTupleCount = 0;
      }

    @Override
      public void execute(Tuple tuple) {
        String key = (String) tuple.getValue(0);
        Integer val = null;
        if (countMap.get(key) == null) {
          countMap.put(key, 1);
          val = new Integer(1);
        } else {
          val = countMap.get(key);
          countMap.put(key, ++val);
        }

        ProcessedTupleCount++;
        if (ProcessedTupleCount == 1) {
          start_time = System.currentTimeMillis();
        }
        // System.out.println("Processed '" + key + "' at bolt " + myId);
        collector.emit(new Values(key, val));
        collector.ack(tuple);
      }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declerator) {
      declerator.declare(new Fields(new String("tweet" + myId),
        new String("count" + myId)));   
    }
    public long getProcessedTupleCount() { return ProcessedTupleCount; }
    /*
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
      OutputFieldsDeclerator.declare(new 
    } */
  }
  /**
   * A bolt that counts the words that it receives
   */
  public static class AggregatorBolt extends BaseRichBolt {
    public static long start_time = 0;
    private OutputCollector collector;
    public static Map<String, Integer> FinalMap;
    static public long ProcessedTupleCount;
    int done = 0;

    @SuppressWarnings("rawtypes")
      public void prepare(Map map, TopologyContext topologyContext,
        OutputCollector outputCollector) {
        collector = outputCollector;
        FinalMap = new ConcurrentHashMap<String, Integer>();
        ProcessedTupleCount = 0;
      }

    @Override
      public void execute(Tuple tuple) {
        String key = tuple.getString(0);
        Integer val = tuple.getInteger(1);

        ProcessedTupleCount++;
        
        if (FinalMap.get(key) == null) {
          FinalMap.put(key, 1);
          val = new Integer(1);
        } else {
          val = FinalMap.get(key);
          FinalMap.put(key, ++val);
        }
        
        // System.out.println("Aggregator Key = " +  key + " Integer = " + val);

        if (ProcessedTupleCount == 1) {
          start_time = System.currentTimeMillis();
        }
        double time = (System.currentTimeMillis() - start_time);
        double div = 1000;
        //System.out.println(ProcessedTupleCount + ", " + time / div);
        collector.ack(tuple);
      }

    public static long getProcessedTupleCount() { return ProcessedTupleCount; }
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
  }

  /**
   * Main method
   */                                                                                                                                                                                                                                                  
  public static void main(String[] args) throws AlreadyAliveException,
    InvalidTopologyException, Exception {
    System.out.println("====================MAIN STARTED at " + 
      System.currentTimeMillis() + " ==========================");
     if (args.length < 1) {
       throw new RuntimeException("Specify topology name");
     }


     if (args.length > 3) {
       System.out.println("Kafka Server = " + args[1] + " Topic name = " +
            args[2] + "\n\n"); 
     } else {
       System.out.println("Please enter the host, topic name, parallelism");
       return;
     }
     int parallelism = Integer.parseInt(args[3]);

     ZkHosts hosts = new ZkHosts(args[1] + ":2181");
     SpoutConfig spoutConfig = new SpoutConfig(hosts, args[2], "/" + args[2],
       UUID.randomUUID().toString());

     spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
     KafkaSpout spout = new KafkaSpout(spoutConfig);

     // WordSpout spout = new WordSpout();
     DistributorBolt disBolt = new DistributorBolt();
     ConsumerBolt bolt = new ConsumerBolt("1");
     ConsumerBolt bolt2 = new ConsumerBolt("2");
     AggregatorBolt aggrBolt = new AggregatorBolt();

     TopologyBuilder builder = new TopologyBuilder();
     builder.setSpout("word", spout, parallelism);
     builder.setBolt("distributor", disBolt, parallelism)
      .shuffleGrouping("word");
     builder.setBolt("consumer1", bolt, parallelism)
      .shuffleGrouping("distributor", "stream1");
     builder.setBolt("consumer2", bolt2, parallelism)
      .shuffleGrouping("distributor", "stream2");
     /*
     builder.setBolt("Aggregator", aggrBolt, parallelism)
     .fieldsGrouping("consumer1", new Fields("tweet1"))
     .fieldsGrouping("consumer2", new Fields("tweet2"));
     */
     builder.setBolt("Aggregator", aggrBolt, parallelism)
     .fieldsGrouping("consumer1", new Fields("tweet1", "count1"))
     .fieldsGrouping("consumer2", new Fields("tweet2", "count2"));

     Config conf = new Config();
     conf.setNumStmgrs(parallelism);

     /*
        Set config here
      */

     // conf.setComponentRam("word", 2L * 1024 * 1024 * 1024);
     // conf.setComponentRam("consumer", 3L * 1024 * 1024 * 1024);
     // conf.setContainerCpuRequested(6);
     conf.setContainerDiskRequested(1L * 1024 * 1024 * 1024);
     System.out.println("==================== Submitting the topology " +
       System.currentTimeMillis() + " ==========================");

     LocalCluster local = new LocalCluster();
     ConsumerBolt.start_time = System.currentTimeMillis();
     // local.submitTopology(args[0], conf, builder.createTopology());

      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
     System.out.println("==================== Done Submitting the topology " + System.currentTimeMillis() + " ==========================");

     if (true)
       return;

     long prev = 0, curr = 0;
     curr = bolt.getProcessedTupleCount();

     /* Wait till there is no processing */
     while ( (curr - prev) == 0) {
       backtype.storm.utils.Utils.sleep(100);
       prev = curr;
       curr = bolt.getProcessedTupleCount();
     }

     /* Wait till there is some tuples processed and when the number
      * of processed tuples is zero, kill the topology.
      */
     while ((curr - prev) != 0) {
       backtype.storm.utils.Utils.sleep(100);
       prev = curr;
       curr = bolt.getProcessedTupleCount();    
     }

     //backtype.storm.utils.Utils.sleep(30000); 

     double time = (System.currentTimeMillis() - ConsumerBolt.start_time);
     double div = 1000;
     System.out.println(aggrBolt.ProcessedTupleCount + ", " + time / div);

     //System.out.println("Total Tuples Processed = " +   bolt.getProcessedTupleCount());


     /*
        System.out.println("==================== Killing the toplogy " +
        System.currentTimeMillis() + " ==========================");
        local.killTopology("MyWordCount");
        local.shutdown();
      */

     /*
        System.out.println("Total Tuples Produced = " +
        spout.getProducedTupleCount());
      */ 



     /*
        Iterator it = ConsumerBolt.countMap.entrySet().iterator();
        while(it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        System.out.println("Key = " + (String) pair.getKey() + " | value = " + ((Integer)pair.getValue()).toString());
        }
      */
   }
}


