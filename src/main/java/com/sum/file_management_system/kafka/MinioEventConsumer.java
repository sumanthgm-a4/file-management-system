package com.sum.file_management_system.kafka;

import java.util.List;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sum.file_management_system.entity.Document;
import com.sum.file_management_system.entity.UploadStatus;
import com.sum.file_management_system.repository.DocumentRepository;

import io.minio.messages.Event;
import io.minio.messages.EventType;
import io.minio.messages.NotificationRecords;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioEventConsumer {

    private final DocumentRepository documentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"file-events"}, groupId = "minioEventsGroup")
    public void consume(
        @Header(name = KafkaHeaders.RECEIVED_KEY) String objectKey, 
        String payload
    )
    {
        JsonNode node = null;
        try {
            node = objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new RuntimeException("Message Consumption has failed");
        }
        JsonNode record = node.get("Records").get(0);

        String eventType =
            record.get("eventName").asText();

        Long fileSize = Optional.ofNullable(record)
                .map(node1 -> node1.get("s3"))
                .map(node2 -> node2.get("object"))
                .map(node3 -> node3.get("size"))
                .map(JsonNode::asLong)
                .orElse(null);

        log.info("Object key is : {}", objectKey);

        if (eventType.startsWith("s3:ObjectCreated:")) {
            putMetadata(fileSize, objectKey);
        } else if (eventType.startsWith("s3:ObjectRemoved:")) {
            removeMetadata(objectKey);
        }
    }

    @Transactional
    private void putMetadata(Long fileSize, String objectKey) {
        Document document = documentRepository.findByObjectKey(objectKey)
            .orElseThrow(() -> new RuntimeException("objectKey not found for upload"));

        document.setSize(fileSize);
        document.setUploadStatus(UploadStatus.UPLOADED);

        documentRepository.save(document);

        log.info("Object is uploaded successfully");
    }

    @Transactional
    private void removeMetadata(String objectKey) {
        Document document = documentRepository.findByObjectKey(objectKey)
            .orElseThrow(() -> new RuntimeException("objectKey not found for delete"));

        documentRepository.delete(document);

        log.info("Object is deleted successfully");
    }
}
