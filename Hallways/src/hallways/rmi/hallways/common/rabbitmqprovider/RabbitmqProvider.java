package hallways.rmi.hallways.common.rabbitmqprovider;
import com.rabbitmq.client.*;

import javax.security.auth.callback.Callback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitmqProvider {
    private final ConnectionFactory _factory;
    private String _message;

    public RabbitmqProvider() {
        _factory = new ConnectionFactory();
        _factory.setHost("localhost");
    }

    public void SendMessage(String queueName, String message) throws IOException, TimeoutException {
        try (Connection connection = _factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("RabbitMQ. Sent '" + message + "'");
        }
    }

    public void ReceiveMessage(String queueName, DeliverCallback callback) throws IOException, TimeoutException {
        try (Connection connection = _factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicConsume(queueName, true, callback, consumerTag -> { });
        }
    }
}
