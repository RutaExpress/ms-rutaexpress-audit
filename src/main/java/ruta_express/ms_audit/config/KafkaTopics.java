package ruta_express.ms_audit.config;

// nombres de Kafka centralizados (los mismos que crea rutaexpress-infra/kafka/crear-topicos.sh)
public final class KafkaTopics {

    // tópicos que lee audit (los publica shipments)
    public static final String AUDIT_TIMELINE = "audit.timeline";
    public static final String SHIPMENTS_EVENTS = "shipments.events";

    // tópico de mensajes fallidos de audit (un DLT por consumidor, caso sección 9)
    public static final String AUDIT_DLT = "audit.DLT";

    // constructor privado: solo constantes
    private KafkaTopics() {}
}
