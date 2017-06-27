package TwitterAnalysis.TrendAnalysis


import java.util.HashMap

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.{ SparkContext, SparkConf }
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import com.mongodb.spark._
import org.bson.Document
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BsonInt64
import java.text.SimpleDateFormat
import java.util.{Calendar, Properties, UUID}
import org.apache.spark.sql.DataFrame
import java.util.{Date, Properties}
import scala.util.Random
//import com.mongodb.casbah.Imports._
//import com.mongodb.spark.MongoConnector
//import com.mongodb.casbah.MongoClient
//import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}

object SparkTrend {
   def main(args: Array[String]) {
      /*if (args.length < 4) {
         System.err.println("Usage: KafkaWordCount <zkQuorum><group> <topics> <numThreads>")
         System.exit(1)*/
      //}

     // val Array(zkQuorum, group, topics;, numThreads) ;= args
     //case class aggregateddoc(date_time : String, tag : String, count : Int)
     
      val zkQuorum="52.42.244.153:2181"
      val topics="twitter-topic"
      val numThreads=1
      val group = "twitter-sample"
      val sparkConf = new SparkConf().setMaster("local[4]").setAppName("twittersample")
      sparkConf.set("spark.mongodb.output.uri","mongodb://prabhakar:prabhakar@ds115701.mlab.com:15701/twitter_mongo")
      sparkConf.set("spark.mongodb.output.collection","tweets")
      sparkConf.set("spark.mongodb.output.writeConcern.w","majority")
      sparkConf.set("spark.mongodb.output.databaseName","twitter_mongo")
      val sc = new SparkContext(sparkConf)
      sc.setLogLevel("ERROR")
      val ssc = new StreamingContext(sc, Seconds(5))
      //ssc.checkpoint("checkpoint")
      // Kafka writer starts
      val events = 1000
      val topic = "spark-result"
      val brokers = "52.42.244.153:9092"
     val rnd = new Random()
      val props = new Properties()
      props.put("bootstrap.servers", brokers)
      props.put("client.id", "ScalaSparkResult")
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  
       val producer = new KafkaProducer[String, String](props)
      val t = System.currentTimeMillis()
      
      
      val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
      val stream = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
      
    
    // Split the stream on space and extract hashtags 
    val hashTags = stream.flatMap(_.split(" ").filter(_.startsWith("#")))

    // Get the top hashtags over the previous 60 sec window
    val topCounts60 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))

    // Get the top hashtags over the previous 10 sec window
    val topCounts10 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(10))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))

      
    // print tweets in the currect DStream 
    stream.print()
  
    topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      topList.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count)) }
      
    })
    
     topCounts60.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 60 seconds (%s total):".format(rdd.count()))
      val today = new Date().getTime()
      topList.foreach { case (count, tag) => producer.send(new ProducerRecord[String, String](topic, today.toString() + "," + tag + "," + count)) }
      
    })
    topCounts10.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      topList.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count)) }
    
    })
    //topCounts60.foreachRDD{ (rdd : RDD[String] ,time)
    
  
//    val data = topCounts60.map(tweet=> {
//    val now = Calendar.getInstance().getTime
//    val doc = new Document()
//    doc.put("count",tweet._1)
//    doc.put("topic",tweet._2)
//    doc.put("timestamp", now)
//    //doc.put("time",tweet.getCreatedAt)
//    doc
//   })
    
//      val data = topCounts60.map(tweet=> {
//    //val now = Calendar.getInstance().getTime
//    /*val doc = new Document()
//    doc.put("count",tweet._1)
//    doc.put("topic",tweet._2)
//    doc.put("timestamp", now)*/
//    val msg = 
//    val data_kafka = new ProducerRecord[String, String](topic, msg)
    
    //doc.put("time",tweet.getCreatedAt)
    //doc
//   })
   // println(doc.toString()) 
  //  data.print()
//    val tt = topCounts60.foreachRDD(sdd=>{
//    val topList = sdd.take(10)
//    val doc = new Document()
//    doc.put("count",topList)
//    doc
//    
    //doc.put("topic",tweet._2)  
   // })
    
  
//  for (nEvents <- Range(0, events)) {
//    val runtime = new Date().getTime()
//    //val ip = "192.168.2." + rnd.nextInt(255)
//    val msg = 
//    val data_kafka = new ProducerRecord[String, String](topic, msg)
//
//    //async
//    //producer.send(data, (m,e) => {})
//    //sync
//    producer.send(data_kafka)
//
//  }
  
      ssc.start()
      ssc.awaitTermination()
   }
}