package com.mealsynx.bigdata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BigDataPipelineService {
    private final Queue<String> kafkaStreamBuffer = new ConcurrentLinkedQueue<>();

    public BigDataPipelineService() {
        publishEvent("mealsynx-orders-topic", "CAN-2", "{\"item\":\"Ambur Chicken Dum Biryani\",\"qty\":2,\"busyStatus\":\"Busy\"}");
        publishEvent("mealsynx-weather-topic", "CHENNAI-CAMPUS", "{\"tempC\":34.8,\"season\":\"SUMMER_HOT\",\"coldDrinkBoost\":1.42}");
        publishEvent("mealsynx-waste-loop", "CAN-3", "{\"item\":\"Schezwan Veg Hakka Noodles\",\"wasteKg\":11.4,\"action\":\"TrimPrep10Pct\"}");
    }

    public void publishEvent(String topic, String key, String jsonPayload) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        kafkaStreamBuffer.offer(String.format("[%s] KAFKA_TOPIC=%s | KEY=%s | %s", ts, topic, key, jsonPayload));
    }

    public List<String> getStreamEvents() {
        return new ArrayList<>(kafkaStreamBuffer);
    }
}
