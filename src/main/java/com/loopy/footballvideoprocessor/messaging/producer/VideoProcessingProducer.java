package com.loopy.footballvideoprocessor.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.loopy.footballvideoprocessor.messaging.dto.VideoProcessingMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.video-processing}")
    private String videoProcessingExchange;

    @Value("${rabbitmq.routing-key.video-processing}")
    private String videoProcessingRoutingKey;

    public void sendVideoProcessingMessage(VideoProcessingMessage message) {
        try {
            log.info("Gửi video processing message với videoId: {}", message.getVideoId());
            rabbitTemplate.convertAndSend(videoProcessingExchange, videoProcessingRoutingKey, message);
            log.info("Message đã được gửi thành công");
        } catch (Exception e) {
            log.error("Lỗi khi gửi video processing message: {}", e.getMessage(), e);
            throw e;
        }
    }
}
