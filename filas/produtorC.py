import pika
import random

# Client configuration
HIGH_PRIORITY_QUEUE = 'high_priority'
LOW_PRIORITY_QUEUE = 'low_priority'

class Client:
    def __init__(self):
        self.connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        self.channel = self.connection.channel()
        self.callback_queue = self.channel.queue_declare(queue='', exclusive=True).method.queue
        self.channel.basic_consume(queue=self.callback_queue, on_message_callback=self.on_response, auto_ack=True)
        self.response = None

    def on_response(self, ch, method, properties, body):
        self.response = body.decode()

    def send_task(self, priority, execution_time, resource_cost):
        queue = HIGH_PRIORITY_QUEUE if priority == 'high' else LOW_PRIORITY_QUEUE
        message = f"{execution_time} {resource_cost}"
        self.response = None

        self.channel.queue_declare(queue=queue)
        self.channel.basic_publish(
            exchange='',
            routing_key=queue,
            body=message,
            properties=pika.BasicProperties(
                reply_to=self.callback_queue
            )
        )

        print(f"Sent task to {queue} queue: {message}")

        # Wait for the response from the server
        while self.response is None:
            self.connection.process_data_events()

        print(f"Received: {self.response}")

        # Stop if resources are insufficient
        if self.response == 'Insufficient Resources':
            return False
        return True

if __name__ == "__main__":
    client = Client()

    while True:  # Keep sending tasks until resources are depleted
        execution_time = random.randint(1, 20)  # Execution time between 1 and 20 seconds
        resource_cost = random.randint(1, 10)   # Resource cost between 1 and 10
        priority = 'high' if random.choice([True, False]) else 'low'

        if not client.send_task(priority, execution_time, resource_cost):
            print("Stopping task generation: server out of resources")
            break
