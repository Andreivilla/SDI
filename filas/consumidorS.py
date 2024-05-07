import pika
import threading
import time

# Server configuration
RESOURCE_LIMIT = 200  # Initial resource units
HIGH_PRIORITY_QUEUE = 'high_priority'
LOW_PRIORITY_QUEUE = 'low_priority'

class Server:
    def __init__(self, resource_limit):
        self.resources = resource_limit
        self.connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        self.channel = self.connection.channel()

        # Declare queues
        self.channel.queue_declare(queue=HIGH_PRIORITY_QUEUE)
        self.channel.queue_declare(queue=LOW_PRIORITY_QUEUE)

    def process_task(self, ch, method, properties, body):
        execution_time, resource_cost = map(int, body.decode().split())
        print(f"Processing task: Execution Time: {execution_time}s, Resource Cost: {resource_cost}")

        if self.resources >= resource_cost:
            self.resources -= resource_cost
            print(f"Resources remaining: {self.resources}")
            time.sleep(execution_time)  # Simulate task processing
            message = 'Task Completed'
            # Permanent reduction of resources
        else:
            print("Insufficient resources, skipping task")
            message = 'Insufficient Resources'

        ch.basic_ack(delivery_tag=method.delivery_tag)

        # Send acknowledgment to client with resource status
        if properties.reply_to:
            self.channel.basic_publish(
                exchange='',
                routing_key=properties.reply_to,
                body=message
            )

    def start(self):
        self.channel.basic_consume(queue=HIGH_PRIORITY_QUEUE, on_message_callback=self.process_task)
        self.channel.basic_consume(queue=LOW_PRIORITY_QUEUE, on_message_callback=self.process_task)
        print("Server started...")
        self.channel.start_consuming()

if __name__ == "__main__":
    server = Server(RESOURCE_LIMIT)
    threading.Thread(target=server.start).start()
