# ms-rutaexpress-audit

Microservicio encargado de consumir eventos de envíos y de auditoría desde Kafka para guardar el timeline en Oracle. Solo lectura.

## 🚀 Tecnologías
- Java 21 / Spring Boot
- Spring Data JPA + Oracle (H2 en local)
- Spring Kafka
- Maven

## ⚙️ Variables de Entorno
Revisar el archivo `.env.example`.

## 🔌 Endpoints Principales
- `GET /api/v1/audit/shipments/{id}` (Retorna el timeline de un envío, solo lectura)
- `GET /actuator/health` (Estado del microservicio para Docker)

## 🏗️ Cómo ejecutar en local
1. Asegurarse de tener Kafka corriendo.
2. Ejecutar `mvn spring-boot:run`.
3. Consumir los endpoints vía `http://localhost:8087`.

## 🐳 Docker
```bash
docker build -t ms-rutaexpress-audit .