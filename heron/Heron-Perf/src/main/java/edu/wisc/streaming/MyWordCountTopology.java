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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;


/**
 * This is a topology that does simple word frequency counts.
 * 
 * In this topology,
 * 1. The spout is a KafkaSpout which reads from the kafka.
 * 2. During every "nextTuple" call, each spout picks a word from the kafka and emits it.
 * 3. Bolts maintain an in-memory map, which is keyed by the word emitted by the spouts,
 * and updates the count when it receives a tuple.
 */


public final class MyWordCountTopology {
  private MyWordCountTopology() {
  }

	class Constants {
	  public static final long NTUPLES = 1000000; /* One Million */
	};
   /**
   * A bolt that counts the words that it receives
   */
  public static class ConsumerBolt extends BaseRichBolt {
    private static final long serialVersionUID = -5470591933906954522L;

    private OutputCollector collector;
    public static Map<String, Integer> countMap;
    static long ProcessedTupleCount;
    int done = 0;

    @SuppressWarnings("rawtypes")
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
      collector = outputCollector;
      countMap = new HashMap<String, Integer>();
      ProcessedTupleCount = 0;
    }

    @Override
    public void execute(Tuple tuple) {
      String key = (String) tuple.getValue(0);
      //System.out.println("KEY = " + key );     
      Integer val = null;
      if (countMap.get(key) == null) {
        countMap.put(key, 1);
      } else {
        val = countMap.get(key);
        countMap.put(key, ++val);
      }

      if (key.equals("NULL") && done == 0) {
        done = 1;
        System.out.println("==================== DONE with the input " + System.currentTimeMillis() + "==========================");
      }
      ProcessedTupleCount++;
      if (ProcessedTupleCount % Constants.NTUPLES == 0) {
        System.out.println("Processed " + ProcessedTupleCount + " Tuples " +
        " With current one being String = " + key + " count = " + val.toString());
      }
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
    System.out.println("====================MAIN STARTED at " +  System.currentTimeMillis() + " ==========================");
    if (args.length < 1) {
      throw new RuntimeException("Specify topology name");
    }

    int parallelism = 1;
    if (args.length > 1) {
      System.out.println("\n" + args[1] + "\n\n"); 
    } else {
    	System.out.println("Please enter the topic name");
    	return;
    }
    /* Earlier Implmentation was to directly read from the the file. Now it
     * is done by kafka
     */
    /*
    FileInputStream fstream = new FileInputStream(args[1]);
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
    Vector <String>v = new Vector <String> (1000000, 1000000);
    
    String line;
    while ((line = br.readLine()) != null) {
      v.addElement(new String(line));
      //System.out.println(line);
    }
    br.close();
    */
    
    System.out.println("==================== DONE reading the file " +
    System.currentTimeMillis() + " ==========================");
    
    ZkHosts hosts = new ZkHosts("rockhopper-04.cs.wisc.edu:2181");
    SpoutConfig spoutConfig = new SpoutConfig(hosts, args[1], "/" + args[1], UUID.randomUUID().toString());
    
    spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
    KafkaSpout spout = new KafkaSpout(spoutConfig);
    
    // WordSpout spout = new WordSpout();
    ConsumerBolt bolt = new ConsumerBolt();
  
    TopologyBuilder builder = new TopologyBuilder();
    builder.setSpout("word", spout, parallelism);
    builder.setBolt("consumer", bolt, parallelism)
        .fieldsGrouping("word", new Fields("word"));
    Config conf = new Config();
    conf.setNumStmgrs(parallelism);

    /*
    Set config here
    */
    
    conf.setComponentRam("word", 2L * 1024 * 1024 * 1024);
    conf.setComponentRam("consumer", 3L * 1024 * 1024 * 1024);
    conf.setContainerCpuRequested(6);
    LocalCluster local = new LocalCluster();
    System.out.println("==================== Submitting the topology " +
    System.currentTimeMillis() + " ==========================");
    local.submitTopology(args[0], conf, builder.createTopology());
    //StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    
    backtype.storm.utils.Utils.sleep(10000);

    System.out.println("==================== Killing the toplogy " +
    System.currentTimeMillis() + " ==========================");
    
    local.killTopology("MyWordCount");
    local.shutdown();

   /*
    System.out.println("Total Tuples Produced = " +
    spout.getProducedTupleCount());
   */
    
    System.out.println("Total Tuples Processed = " +
    bolt.getProcessedTupleCount());
    

  }
}


