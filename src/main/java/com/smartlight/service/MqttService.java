package com.smartlight.service;

import com.smartlight.entity.LightData;
import com.smartlight.repository.LightDataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MqttService {
    
    @Autowired
    private LightDataRepository lightDataRepository;
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;
    
    @Value("${mqtt.client.id:smart_light_backend}")
    private String clientId;
    
    @Value("${mqtt.topic.control:/light/control}")
    private String controlTopic;
    
    @Value("${mqtt.topic.status:/light/status}")
    private String statusTopic;
    
    private MqttClient mqttClient;
    private String lastStatus = "off";
    private Double lastCurrent = 0.0;
    private Double lastPower = 0.0;
    private boolean connected = false;
    
    @PostConstruct
    public void init() {
        connectToBroker();
    }
    
    @PreDestroy
    public void cleanup() {
        disconnect();
    }
    
    private void connectToBroker() {
        try {
            log.info("Connecting to MQTT broker: {}", brokerUrl);
            mqttClient = new MqttClient(brokerUrl, clientId + "_" + System.currentTimeMillis(), new MemoryPersistence());
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);
            
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT connection lost: {}", cause.getMessage());
                    connected = false;
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    log.info("MQTT message received - Topic: {}, Message: {}", topic, payload);
                    handleMessage(topic, payload);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.debug("MQTT message delivered");
                }
            });
            
            mqttClient.connect(options);
            connected = true;
            log.info("Successfully connected to MQTT broker: {}", brokerUrl);
            
            // Subscribe to status topic to receive feedback from ESP32
            mqttClient.subscribe(statusTopic, 1);
            mqttClient.subscribe("/light/#", 1);
            log.info("Subscribed to topics: {}, /light/#", statusTopic);
            
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker: {}", e.getMessage());
            connected = false;
        }
    }
    
    private void handleMessage(String topic, String payload) {
        try {
            boolean shouldSave = false;
            
            if (topic.equals(statusTopic) || topic.contains("status")) {
                // Parse JSON status if available
                if (payload.contains("\"state\"")) {
                    // Parse state value chính xác: "state":"on" hoặc "state":"off"
                    try {
                        String stateStr = payload.replaceAll(".*\"state\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                        if (stateStr.equals("on") || stateStr.equals("off")) {
                            lastStatus = stateStr;
                        }
                    } catch (Exception e) {
                        log.warn("Could not parse state from JSON: {}", e.getMessage());
                    }
                    
                    // Parse current and power from JSON status
                    try {
                        // Extract current value: "current":0.000
                        if (payload.contains("\"current\"")) {
                            String currentStr = payload.replaceAll(".*\"current\"\\s*:\\s*([0-9.]+).*", "$1");
                            lastCurrent = Double.parseDouble(currentStr);
                        }
                        // Extract power value: "power":0.0
                        if (payload.contains("\"power\"")) {
                            String powerStr = payload.replaceAll(".*\"power\"\\s*:\\s*([0-9.]+).*", "$1");
                            lastPower = Double.parseDouble(powerStr);
                        }
                    } catch (Exception e) {
                        log.warn("Could not parse current/power from status JSON: {}", e.getMessage());
                    }
                    
                    // Kiểm tra nếu là từ công tắc vật lý - lưu ngay lập tức
                    if (payload.contains("\"source\":\"physical_switch\"")) {
                        log.info("Physical switch state change detected: {}", lastStatus);
                        shouldSave = true;
                    } else {
                        shouldSave = true; // Save when receiving full status
                    }
                } else if (payload.equals("on") || payload.equals("off")) {
                    lastStatus = payload;
                }
            } else if (topic.contains("current")) {
                lastCurrent = Double.parseDouble(payload);
            } else if (topic.contains("power")) {
                lastPower = Double.parseDouble(payload);
            }
            
            // Save to database when receiving status message (contains all data)
            if (shouldSave) {
                saveToDatabase();
            }
        } catch (Exception e) {
            log.error("Error handling MQTT message: {}", e.getMessage());
        }
    }
    
    private void saveToDatabase() {
        try {
            LightData lightData = new LightData();
            lightData.setStatus(lastStatus);
            lightData.setCurrent(lastCurrent);
            lightData.setPower(lastPower);
            
            lightDataRepository.save(lightData);
            log.info("Saved light data to database - Status: {}, Current: {}, Power: {}", 
                    lastStatus, lastCurrent, lastPower);
        } catch (Exception e) {
            log.error("Failed to save light data to database: {}", e.getMessage());
        }
    }
    
    public void publishControl(String command) {
        publish(controlTopic, command);
    }
    
    public void publish(String topic, String message) {
        System.out.println("========== MQTT PUBLISH START ==========");
        System.out.println("Topic: " + topic);
        System.out.println("Message: " + message);
        System.out.println("Connected: " + connected);
        System.out.println("Client null: " + (mqttClient == null));
        if (mqttClient != null) {
            System.out.println("Client connected: " + mqttClient.isConnected());
        }
        
        if (!connected || mqttClient == null || !mqttClient.isConnected()) {
            log.warn("MQTT not connected, attempting to reconnect...");
            System.out.println("MQTT not connected, attempting to reconnect...");
            connectToBroker();
        }
        
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(1);
                mqttMessage.setRetained(false);
                mqttClient.publish(topic, mqttMessage);
                log.info("Successfully published to {}: {}", topic, message);
                System.out.println("SUCCESS: Published to " + topic + ": " + message);
                
                // Update local state
                if (message.equals("1") || message.equalsIgnoreCase("on")) {
                    lastStatus = "on";
                } else if (message.equals("0") || message.equalsIgnoreCase("off")) {
                    lastStatus = "off";
                }
            } else {
                log.error("MQTT client not connected, cannot publish message");
                System.out.println("ERROR: MQTT client not connected, cannot publish message");
            }
        } catch (MqttException e) {
            log.error("Failed to publish MQTT message: {}", e.getMessage());
            System.out.println("EXCEPTION: Failed to publish - " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("========== MQTT PUBLISH END ==========");
    }
    
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            log.error("Error disconnecting from MQTT broker: {}", e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }
    
    public String getLastStatus() {
        return lastStatus;
    }
    
    public Double getLastCurrent() {
        return lastCurrent;
    }
    
    public Double getLastPower() {
        return lastPower;
    }
}
