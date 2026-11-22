package ruta_express.ms_audit.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import tools.jackson.core.JacksonException;

// manejo de errores de los consumidores: reintentos y, si sigue fallando, el mensaje va a audit.DLT
@Configuration
public class KafkaConfig {

    // 3 reintentos con 2 segundos de espera; después, el mensaje se publica en audit.DLT
    // un JSON mal escrito no se reintenta: nunca se va a arreglar solo
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template, KafkaConfig::deadLetterDestination);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3));
        handler.addNotRetryableExceptions(JacksonException.class);
        return handler;
    }

    // deadLetterDestination (destino de mensajes fallidos): audit.DLT en la MISMA partición, venga del tópico que venga
    // (sin esto Spring usaría audit.timeline.DLT o shipments.events.DLT, que no existen)
    public static TopicPartition deadLetterDestination(ConsumerRecord<?, ?> record, Exception ex) {
        return new TopicPartition(KafkaTopics.AUDIT_DLT, record.partition());
    }
}
