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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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


public final class PerceptronTopology {
  private PerceptronTopology() {
  }
	class Constants {
	  public static final long ONE_25_MILLION= 1250000; /* One Million */
	  public static final long ONE_MILLION= 1000000; /* One Million */
	  public static final long HALF_MILLION= 500000; /* One Million */
	  public static final long QUARTER_MILLION= 250000; /* One Million */
	  public static final long ONE_HUNDRED_THOUSAND= 100000; /* One Million */
	  public static final long ONE_25_THOUSAND = 125000;
	};
   /**
   * A bolt that counts the words that it receives
   */
  public static class ConsumerBolt extends BaseRichBolt {
    public static long start_time = 0;
    private OutputCollector collector;
    // public static Map<String, Integer> countMap;
    static public long ProcessedTupleCount = 0;
    BinaryFeature[] features = new BinaryFeature[20];
    Double[] w_arr = new Double[] {-0.30,0.20,0.20,0.10,-0.00,-0.10,-0.30,0.20,0.40,-0.10,0.10,0.40,-0.10,-0.70,-0.20,-0.20,0.10,-0.50,-0.00,-0.90,-0.10};
    Integer NUM_OF_FEATURES = 20;
    ListOfExamples testExamplesSet = new ListOfExamples();
    Perceptron p1 = new Perceptron(w_arr, NUM_OF_FEATURES);
    
    public ConsumerBolt() {
    }

    @SuppressWarnings("rawtypes")
    public void prepare(Map map, TopologyContext topologyContext,
      OutputCollector outputCollector) {
      collector = outputCollector;
      // countMap = new ConcurrentHashMap<String, Integer>();
      // countMap = new HashMap<String, Integer>();
      ProcessedTupleCount = 0;
      
      features[0] = new BinaryFeature("FixedAcidityGt47","T","F");
      features[1] = new BinaryFeature("volatileAcidityGt17","T","F");
      features[2] = new BinaryFeature("volatileAcidityGt29","T","F");
      features[3] = new BinaryFeature("citricAcidGt30","T","F");
      features[4] = new BinaryFeature("residualSugarGtMean","T","F");
      features[5] = new BinaryFeature("chloridesGt9","T","F");
      features[6] = new BinaryFeature("freeSulfurDioxideGtMean","T","F");
      features[7] = new BinaryFeature("totalSulfurDioxideGt27","T","F");
      features[8] = new BinaryFeature("totalSulfurDioxideGt37","T","F");
      features[9] = new BinaryFeature("totalSulfurDioxideGt54","T","F");
      features[10] = new BinaryFeature("densityGt18","T","F");
      features[11] = new BinaryFeature("densityGt41","T","F");
      features[12] = new BinaryFeature("pHGtMean","T","F");
      features[13] = new BinaryFeature("sulphatesGt12","T","F");
      features[14] = new BinaryFeature("sulphatesGt15","T","F");
      features[15] = new BinaryFeature("sulphatesGt19","T","F");
      features[16] = new BinaryFeature("sulphatesGt44","T","F");
      features[17] = new BinaryFeature("alcoholGt22","T","F");
      features[18] = new BinaryFeature("alcoholGt33","T","F");
      features[19] = new BinaryFeature("alcoholGt47","T","F");
      
      testExamplesSet.setFeatures(NUM_OF_FEATURES, features);
    }

    @Override
    public void execute(Tuple tuple) {
      String key = (String) tuple.getValue(0);
      int ret = 0;

      ProcessedTupleCount++;
      if (ProcessedTupleCount == 1) {
        start_time = System.currentTimeMillis();
      }

      /* ============================================================ */
      String example_str = key;
      
      // Read in the examples from the files.

      //p1.printWeights(testExamplesSet);
      Example e = ListOfExamples.parseExampleFromString(
        example_str, testExamplesSet);
      
      ret = p1.runExample(e);

    /* ==================================================================*/

      if (ProcessedTupleCount % Constants.ONE_MILLION == 0 ||
        ProcessedTupleCount == 26760892) {
        double time = (System.currentTimeMillis() - start_time);
        double div = 1000;
        System.out.print("Bolt: " + ProcessedTupleCount + ", "
          + time / div);
         //System.out.println("  Processed tuple " + key + " with ret = " + ret );
      }
      collector.ack(tuple);
    }

    public static long getProcessedTupleCount() { return ProcessedTupleCount; }
    public static void incrementProcessedTupleCount() {
      ProcessedTupleCount++;
    } 
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

    
    if (args.length > 4) {
      System.out.println("Kafka Server = " + args[1] + " Topic name = " +
        args[2] + " SubmissionType = " +
        ((args[3].equals("0")) ? "local" : "Cluster") +
        " Paralellism = " + args[4] + "\n\n"); 
    } else {
    	System.out.println("Please enter the host, topic name," + 
        "local(0)/topology(1), parallelism");
    	return;
    }
    int TopologySubmission = Integer.parseInt(args[3]);
    int parallelism = Integer.parseInt(args[4]);
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
    
    ZkHosts hosts = new ZkHosts(args[1] + ":2181");
    SpoutConfig spoutConfig = new SpoutConfig(hosts, args[2], "/" + args[2],
      UUID.randomUUID().toString());
    
    spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
    KafkaSpout spout = new KafkaSpout(spoutConfig);
    
    // WordSpout spout = new WordSpout();
    ConsumerBolt bolt = new ConsumerBolt();
  
    TopologyBuilder builder = new TopologyBuilder();
    // builder.setSpout("word", spout);
    builder.setSpout("example", spout, 1).setNumTasks(1);
    /* One Bolt which will read all values from the parallel spouts and will
     * perform the word frequecy count using a concurrent/hashmap
     */
    builder.setBolt("consumer", bolt, 1).setNumTasks(1)
        .shuffleGrouping("example");

    Config conf = new Config();
    //conf.setNumStmgrs(parallelism);
    //conf.setNumWorkers(parallelism);

    /*
    Set config here
    */
    
    //conf.setComponentRam("word", 2L * 1024 * 1024 * 1024);
    //conf.setComponentRam("consumer", 3L * 1024 * 1024 * 1024);
    conf.setContainerCpuRequested(10);
    conf.setContainerDiskRequested(2L * 1024 * 1024 * 1024);
    // conf.put("TOPOLOGY_ACKER_EXECUTORS", parallelism);
    // conf.put("topology.component.parallelism", parallelism);
    /* Enable Acking */
    // conf.setEnableAcking(true); conf.put("topology.max.spout.pending", 20000);
    
    System.out.println("==================== Submitting the topology " +
      System.currentTimeMillis() + " ==========================");
    /* Set the maximum amount of tuples that spouts are waiting to be acked.
     * If set very large, then toplogy may be overwhelmed, acking timeouts will
     * happen and tuples are retransmitted. If set low, then thoughput will hit
     * So need to set this value based on some experiments to figure out the
     * right value.
     */

    ConsumerBolt.start_time = System.currentTimeMillis();
    if (TopologySubmission == 1) {
      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    } else {
      LocalCluster local = new LocalCluster();
      local.submitTopology(args[0], conf, builder.createTopology());
    }
    System.out.println("==================== Done Submitting the topology " +
      System.currentTimeMillis() + " ==========================");
    
    if (true) {
      return;
    }

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
    System.out.println(ConsumerBolt.ProcessedTupleCount + ", " + time / div);
    
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


