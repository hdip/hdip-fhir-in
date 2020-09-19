package com.hdip.in.fhir;

import com.hdip.in.fhir.ResourceCreatedInterceptor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HdipConfig{

    @Value("${topic.resourceCreated}")
    private String resourceCreatedTopic;
    @Bean
    public ResourceCreatedInterceptor resourceCreatedInterceptor(KafkaTemplate template){
        return new ResourceCreatedInterceptor(kafkaTemplate());
    }

    @Bean
    public ProducerFactory<Long, IBaseResource> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.2:9092");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<Long,IBaseResource> kafkaTemplate() {
        return new KafkaTemplate<Long,IBaseResource>(producerFactory());
    }
    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.2:9092");
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic resourceCreatedTopic() {
        return TopicBuilder.name("resourceCreated")
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

}
