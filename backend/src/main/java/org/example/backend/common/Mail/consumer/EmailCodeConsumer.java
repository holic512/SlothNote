/**
 * File Name: EmailCodeConsumer.java
 * Description: 邮箱服务类
 * Author: holic512
 * Created Date: 2024-09-10
 * Version: 1.0
 * Usage:
 * 用于发送邮件的邮箱服务类,如提供给rabbit consumer(消费者)调用
 */
package org.example.backend.common.Mail.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.example.backend.common.Mail.Service.MailCodeService;
import org.example.backend.common.Mail.dto.MailCodeMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailCodeConsumer {
    private final ObjectMapper objectMapper;
    private final MailCodeService mailCodeService;

    @Autowired
    public EmailCodeConsumer(MailCodeService mailCodeService) {
        this.objectMapper = new ObjectMapper(); // 用于对象反序列化
        this.mailCodeService = mailCodeService;
    }

    // 监听邮件队列
    @RabbitListener(queues = "emailQueue", containerFactory = "rabbitListenerContainerFactory")
    public void listenToEmailQueue(Channel channel, Message message) throws IOException {
        try {
            // 逆序列化
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            MailCodeMessage mailCodeMessage = objectMapper.readValue(messageBody, MailCodeMessage.class);

            // 调用服务并根据返回的 boolean 值判断发送是否成功
            boolean isSent = mailCodeService.sendVerificationCode(mailCodeMessage);

            if (isSent) {
                // 如果发送成功，手动确认消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                // 如果发送失败，拒绝消息并重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }

        } catch (Exception e) {
            // 捕获异常，拒绝消息并重新入队
            System.out.println("出现异常" + e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }


}
