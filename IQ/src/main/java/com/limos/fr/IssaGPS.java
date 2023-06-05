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

public class IssaGPS {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "issa-consumer-group"+UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GPSSerde.GPSDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(WithoutJSONDBConfig.DB_TYPE_CONFIG, "postgresql");
        props.put(WithoutJSONDBConfig.DB_HOST_CONFIG, "localhost");
        props.put(WithoutJSONDBConfig.DB_PORT_CONFIG, "5432");
        props.put(WithoutJSONDBConfig.DB_NAME_CONFIG, "samuelelanghi");
        props.put(WithoutJSONDBConfig.DB_USER_CONFIG, "samuelelanghi");
        props.put(WithoutJSONDBConfig.DB_PASSWORD_CONFIG, "");


        props.put(ExperimentConfig.CONSTRAINT_STRICTNESS, args[0]);
        props.put(ExperimentConfig.INCONSISTENCY_PERCENTAGE, args[1]);
        props.put(ExperimentConfig.WINDOW_SIZE_MS, args[2]);
        props.put(ExperimentConfig.WINDOW_SLIDE_MS, args[3]);
        props.put(ExperimentConfig.RESULT_FILE_DIR, args[4]);
        props.put(ExperimentConfig.EVENTS_MAX, args[5]);
        props.put(ExperimentConfig.EVENTS_GRANULARITY, args[6]);

        props.put(ExperimentConfig.RESULT_FILE_SUFFIX, "issa-gps");
        //TODO: insert ExperimentConfig

        int constraintStrictness = Integer.parseInt(props.getProperty(ExperimentConfig.CONSTRAINT_STRICTNESS));
        Duration size = Duration.ofMillis(Long.parseLong(props.getProperty(ExperimentConfig.WINDOW_SIZE_MS)));
        Duration advance = Duration.ofMillis(Long.parseLong(props.getProperty(ExperimentConfig.WINDOW_SLIDE_MS)));


        long eventMax = Long.parseLong(props.getProperty(ExperimentConfig.EVENTS_MAX));

        String topic = args[7];

        KafkaConsumer<String, GPS> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try (Connection conn = DriverManager.getConnection(WithoutJSONDBConfig.getURL(props), props.getProperty(WithoutJSONDBConfig.DB_USER_CONFIG), null)) {

            SpeedCostraintGPSFactory speedCostraintFactory = new SpeedCostraintGPSFactory("GPS", "x", "ts", "y", 0.001/constraintStrictness, -0.001/constraintStrictness);

            PerformanceProcessor performanceProcessor = new PerformanceProcessor(props);
            long counter = 0L;
            long counterEvent = 0L;
            LinkedList<EventAndConstraint<GPS>> list = new LinkedList<>();
            boolean terminated = false;
            while (!terminated) {
                StringBuilder constraintBuilder = new StringBuilder();
                StringBuilder timestampConstraintBuilder = new StringBuilder();


                ConsumerRecords<String, GPS> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, GPS> record : records) {
                    if (counterEvent<eventMax){
                        counterEvent++;
                        performanceProcessor.registerIterativePerf();
//                        if (record.value().ts >= counter * windowSlide.toMillis() + windowSize.toMillis()) {
                            Iterator<EventAndConstraint<GPS>> iterator = list.iterator();
                            while (iterator.hasNext()){
                                EventAndConstraint<GPS> next = iterator.next();
                                if (next.getEvent().getTs()<counter * advance.toMillis()){
                                    iterator.remove();
                                } else {
                                    initializeNwDatabaseInstance(iterator, record.value(), conn, speedCostraintFactory, constraintBuilder, timestampConstraintBuilder);
                                    props.put(WithoutJSONDBConfig.DB_CONSTRAINTS_CONFIG, constraintBuilder.toString());
                                    props.put(WithoutJSONDBConfig.DB_CONSTRAINTS_TIMESTAMP_CONFIG, timestampConstraintBuilder.toString().replaceFirst(";", ""));
                                    constraintBuilder = new StringBuilder();
                                    timestampConstraintBuilder = new StringBuilder();

                                    PreProcessImplWithoutJSON service = new PreProcessImplWithoutJSON();
                                    QueryImpWithoutJSON queryImpWithoutJSON = new QueryImpWithoutJSON(props);
                                    service.doPreprocess(props);

                                    QueryEnvironment queryEnvironment =
                                            new QueryEnvironment(-2, 3, QueryEnvironment.Operator.TOPK, Collections.singleton(QueryEnvironment.Measure.CBM));

                                    String s = queryImpWithoutJSON.runQuery(queryEnvironment.getJSONConfig());

//                                    System.out.println(s);

                                    while (record.value().ts >= counter * advance.toMillis() + size.toMillis())
                                        counter++;
                                }
                            }
//                        }
                        list.add(new EventAndConstraint<>(record.value(), speedCostraintFactory.getSpeedConstraintGPS(record.value()) ));
                    } else terminated = true;
                }
            }
            performanceProcessor.end();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            consumer.close();
        }
    }

    private static void initializeNwDatabaseInstance(Iterator<EventAndConstraint<GPS>> iterator, GPS value, Connection conn, SpeedCostraintGPSFactory speedCostraintFactory, StringBuilder constraintGatherer, StringBuilder constraintTsGatherer){
        try {
            PreparedStatement dropStmt = conn.prepareStatement("DROP TABLE IF EXISTS GPS;");
            dropStmt.executeUpdate();
            PreparedStatement dropStmt2 = conn.prepareStatement("DROP TABLE IF EXISTS c.c;");
            dropStmt2.executeUpdate();
            PreparedStatement createStmt = conn.prepareStatement("CREATE TABLE GPS (x double precision, y double precision, ts bigint);");
            createStmt.executeUpdate();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO GPS (x, y, ts) VALUES (?, ?, ?)");


            insertRecordAndConstraint(stmt, value, speedCostraintFactory, constraintGatherer, constraintTsGatherer);

            while (iterator.hasNext()){
                EventAndConstraint<GPS> next = iterator.next();
                insertRecordAndConstraint(stmt, next.getEvent(), speedCostraintFactory, constraintGatherer, constraintTsGatherer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertRecordAndConstraint(PreparedStatement stmt, GPS record, SpeedCostraintGPSFactory speedCostraintFactory, StringBuilder constraintBuilder, StringBuilder timestampConstraintBuilder) throws SQLException {
        stmt.setDouble(1, record.x);
        stmt.setDouble(2, record.y);
        stmt.setLong(3, record.ts);

        stmt.executeUpdate();
        String speedConstraintGPS = speedCostraintFactory.getSpeedConstraintGPS(record);
        //Keep Track of the constraint index when considering the selected constraints
        constraintBuilder.append(speedConstraintGPS);
        timestampConstraintBuilder.append(";").append(record.ts);
    }
}
