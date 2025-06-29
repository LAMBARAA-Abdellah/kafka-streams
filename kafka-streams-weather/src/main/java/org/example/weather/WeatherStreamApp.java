package org.example.weather;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class WeatherStreamApp {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "weather-stream-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> weatherStream = builder.stream("weather-data");

        KStream<String, String> fahrenheitStream = weatherStream
                .filter((key, value) -> {
                    // Ignorer les messages mal formatés
                    String[] parts = value.split(",");
                    return parts.length == 3;
                })
                .mapValues(value -> {
                    String[] parts = value.split(",");
                    return new WeatherData(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                })
                .filter((k, data) -> data.getTemperature() > 30.0)
                .mapValues(data -> {
                    double fahrenheit = data.getTemperature() * 9 / 5 + 32;
                    return data.getStation() + "," + fahrenheit + "," + data.getHumidity();
                });

        KGroupedStream<String, String> grouped = fahrenheitStream
                .groupBy((key, value) -> value.split(",")[0]);

        KTable<String, String> averages = grouped.aggregate(
                () -> "0.0,0.0,0",
                (key, value, aggregate) -> {
                    String[] fields = value.split(",");
                    double temp = Double.parseDouble(fields[1]);
                    double hum = Double.parseDouble(fields[2]);

                    String[] agg = aggregate.split(",");
                    double sumTemp = Double.parseDouble(agg[0]) + temp;
                    double sumHum = Double.parseDouble(agg[1]) + hum;
                    int count = Integer.parseInt(agg[2]) + 1;

                    return sumTemp + "," + sumHum + "," + count;
                }
        ).mapValues(agg -> {
            String[] parts = agg.split(",");
            double avgTemp = Double.parseDouble(parts[0]) / Integer.parseInt(parts[2]);
            double avgHum = Double.parseDouble(parts[1]) / Integer.parseInt(parts[2]);
            return "Température Moyenne = " + avgTemp + "°F, Humidité Moyenne = " + avgHum + "%";
        });

        // Affichage dans la console + publication Kafka
        averages.toStream()
                .peek((key, value) -> System.out.println(">> " + key + " => " + value))
                .to("station-averages", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
