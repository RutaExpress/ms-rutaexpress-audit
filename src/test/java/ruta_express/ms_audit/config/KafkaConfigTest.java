package ruta_express.ms_audit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// test unitario de la configuración de Kafka: a qué tópico va un mensaje que falló después de los reintentos
class KafkaConfigTest {

    // test 1 (contrato con infra): venga de cualquiera de los 2 tópicos, el fallido va a audit.DLT en la misma partición
    @Test
    @DisplayName("Mensaje fallido: va a audit.DLT en la misma partición")
    void failedRecordGoesToAuditDlt() {
        // arrange (preparar): un mensaje de cada tópico que lee audit
        ConsumerRecord<String, String> deTimeline = new ConsumerRecord<>("audit.timeline", 1, 5L, "aud-1", "{}");
        ConsumerRecord<String, String> deEnvios = new ConsumerRecord<>("shipments.events", 2, 9L, "42", "{}");

        // act (actuar)
        TopicPartition destino1 = KafkaConfig.deadLetterDestination(deTimeline, new RuntimeException("falló"));
        TopicPartition destino2 = KafkaConfig.deadLetterDestination(deEnvios, new RuntimeException("falló"));

        // assert (revisar): audit.DLT existe en el clúster (crear-topicos.sh); los nombres por defecto (.DLT) no
        assertEquals("audit.DLT", destino1.topic());
        assertEquals(1, destino1.partition());
        assertEquals("audit.DLT", destino2.topic());
        assertEquals(2, destino2.partition());
    }
}
