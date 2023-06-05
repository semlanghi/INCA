package com.limos.fr;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

public class MainStreamingIngestion {
    private final static String TOPIC_NAME = "sample-topic-3";
    private final static Duration windowSize = Duration.ofDays(5);
    private final static Duration windowSlide = Duration.ofDays(1);
    private final static long numberOfWindows = 2000000;

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group"+UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StockSerde.StockDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(WithoutJSONDBConfig.DB_TYPE_CONFIG, "postgresql");
        props.put(WithoutJSONDBConfig.DB_HOST_CONFIG, "localhost");
        props.put(WithoutJSONDBConfig.DB_PORT_CONFIG, "5432");
        props.put(WithoutJSONDBConfig.DB_NAME_CONFIG, "samuelelanghi");
        props.put(WithoutJSONDBConfig.DB_USER_CONFIG, "samuelelanghi");
        props.put(WithoutJSONDBConfig.DB_PASSWORD_CONFIG, "");



        KafkaConsumer<String, Stock> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try (Connection conn = DriverManager.getConnection(WithoutJSONDBConfig.getURL(props), props.getProperty(WithoutJSONDBConfig.DB_USER_CONFIG), null)) {


            PreparedStatement dropStmt = conn.prepareStatement("DROP TABLE IF EXISTS Stock;");
            dropStmt.executeUpdate();
            PreparedStatement dropStmt2 = conn.prepareStatement("DROP TABLE IF EXISTS c.c;");
            dropStmt2.executeUpdate();
            PreparedStatement createStmt = conn.prepareStatement("CREATE TABLE Stock (name text, value double precision, ts bigint);");
            createStmt.executeUpdate();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Stock (name , value , ts) VALUES (?, ?, ?)");
            PreparedStatement stmtDel = conn.prepareStatement("DELETE FROM Stock WHERE ts < ?");
            PreparedStatement stmtDel2 = conn.prepareStatement("DELETE FROM c.c WHERE position < ?");





            SpeedCostraintStockFactory speedCostraintFactory = new SpeedCostraintStockFactory("Stock", "value", "ts", "name", 0.0000000000000001, -0.0000000000000001);

            long counter = 0L;
            LinkedList<Stock> list = new LinkedList<>();
            while (counter < numberOfWindows) {
                StringBuilder constraintBuilder = new StringBuilder();
                StringBuilder timestampConstraintBuilder = new StringBuilder();


                ConsumerRecords<String, Stock> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, Stock> record : records) {
                    if (counter<numberOfWindows) {
                        if (record.value().ts < counter * windowSlide.toMillis() + windowSize.toMillis()) {
                            insertRecordAndConstraint(stmt, record, speedCostraintFactory, constraintBuilder, timestampConstraintBuilder);
                        } else {
                            props.put(WithoutJSONDBConfig.DB_CONSTRAINTS_CONFIG, constraintBuilder.toString());
                            props.put(WithoutJSONDBConfig.DB_CONSTRAINTS_TIMESTAMP_CONFIG, timestampConstraintBuilder.toString().replaceFirst(";", ""));

                            PreProcessImplWithoutJSON service = new PreProcessImplWithoutJSON();
                            QueryImpWithoutJSON queryImpWithoutJSON = new QueryImpWithoutJSON(props);
                            service.doPreprocess(props);

                            QueryEnvironment queryEnvironment =
                                    new QueryEnvironment(-2, 1, QueryEnvironment.Operator.TOPK, Collections.singleton(QueryEnvironment.Measure.CBM));

                            String s = queryImpWithoutJSON.runQuery(queryEnvironment.getJSONConfig());

                            System.out.println(s);

                            // Reset phase
                            constraintBuilder = new StringBuilder();
                            timestampConstraintBuilder = new StringBuilder();
                            while (record.value().ts >= counter * windowSlide.toMillis() + windowSize.toMillis())
                                counter++;
                            stmtDel.setLong(1, counter * windowSlide.toMillis());
                            stmtDel2.setLong(1, counter * windowSlide.toMillis());
                            stmtDel.executeUpdate();
                            stmtDel2.executeUpdate();



                            //adding the record to the table
                            insertRecordAndConstraint(stmt, record, speedCostraintFactory, constraintBuilder, timestampConstraintBuilder);
                        }
                    }
                }

            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            consumer.close();
        }
    }

    private static void insertRecordAndConstraint(PreparedStatement stmt, ConsumerRecord<String, Stock> record, SpeedCostraintStockFactory speedCostraintFactory, StringBuilder constraintBuilder, StringBuilder timestampConstraintBuilder) throws SQLException {
        stmt.setString(1, record.value().name.toString());
        stmt.setDouble(2, record.value().value);
        stmt.setLong(3, record.value().ts);
        stmt.executeUpdate();
        String speedConstraintStock = speedCostraintFactory.getSpeedConstraintStock(record.value());
        //Keep Track of the constraint index when considering the selected constraints
        constraintBuilder.append(speedConstraintStock);
        timestampConstraintBuilder.append(";").append(record.value().ts);
    }
}
