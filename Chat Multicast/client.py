import socket
import threading

# Configurações do cliente
MCAST_GROUP = '224.0.0.1'  # Grupo multicast
MCAST_PORT = 5007  # Porta multicast

# Função para receber mensagens do servidor
def receive_messages():
    while True:
        try:
            message, _ = client_socket.recvfrom(1024)
            print("\n" + message.decode())
        except socket.error as e:
            print("Erro ao receber mensagem:", e)
            break

# Cria o socket do cliente
client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Inicia a thread para receber mensagens
receive_thread = threading.Thread(target=receive_messages)
receive_thread.start()

user_name = input("Digite seu nome: ")
client_socket.sendto(user_name.encode(), (MCAST_GROUP, MCAST_PORT))


# Loop principal do cliente para enviar mensagens
while True:
    message = input("")
    client_socket.sendto(message.encode(), (MCAST_GROUP, MCAST_PORT))
