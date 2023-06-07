package com.example.customer;

import com.example.amqp.RabbitMQMessageProducer;
import com.example.clients.fraud.FraudCheckResponse;
import com.example.clients.fraud.FraudClient;
import com.example.clients.notification.NotificationClient;
import com.example.clients.notification.NotificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public record CustomerService(CustomerRepository customerRepository, RestTemplate restTemplate,
                              FraudClient fraudClient, NotificationClient notificationClient,
                              RabbitMQMessageProducer rabbitMQMessageProducer) {

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder().
                firstname(request.firstname()).
                lastname(request.lastname()).
                email(request.email()).
                build();

        customerRepository.saveAndFlush(customer);

        FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                "Welcome to the microservices project!!!");

       // notificationClient.sendNotification(notificationRequest);

        rabbitMQMessageProducer.publish(notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}

