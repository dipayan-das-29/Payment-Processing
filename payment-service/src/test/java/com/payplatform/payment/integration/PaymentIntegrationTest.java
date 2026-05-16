package com.payplatform.payment.integration;

import com.payplatform.payment.application.BankGatewayPort;
import com.payplatform.payment.application.IdempotencyService;
import com.payplatform.payment.infrastructure.KafkaPaymentPublisher;
import com.payplatform.payment.infrastructure.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
class PaymentIntegrationTest {

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private KafkaPaymentPublisher kafkaPaymentPublisher;

    @MockBean
    private BankGatewayPort bankGatewayPort;

    @MockBean
    private IdempotencyService idempotencyService;

    @Test
    void contextLoads_withoutDockerDependencies() {
    }
}