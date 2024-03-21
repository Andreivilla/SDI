import socket
import struct
import threading

# Configurações do servidor
MCAST_GROUP = '224.0.0.1'  # Grupo multicast
MCAST_PORT = 5007  # Porta multicast
SERVER_ADDRESS = ('', MCAST_PORT)

# Cria o socket do servidor
server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Permite reutilizar o endereço
server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

# Conecta-se ao grupo multicast
server_socket.bind(SERVER_ADDRESS)
mreq = struct.pack("4sl", socket.inet_aton(MCAST_GROUP), socket.INADDR_ANY)
server_socket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

# Lista de clientes conectados
clients = {}
clients_lock = threading.Lock()

# Função para enviar mensagem para todos os clientes
def send_to_clients(message):
    with clients_lock:
        for client_address in clients:
            server_socket.sendto(message.encode(), client_address)

# Função para lidar com as mensagens recebidas
def handle_messages():
    print("Thread principal iniciada.")
    while True:
        data, address = server_socket.recvfrom(1024)
        data = data.decode()
        with clients_lock:
            # Adiciona novo cliente se ainda não estiver na lista
            if address not in clients:
                clients[address] = data
                print(f"{clients[address]}: {data} se conectou")
            else:
                data = clients[address] + ": " + data
        # Inicia uma thread para enviar a mensagem para todos os clientes
        send_thread = threading.Thread(target=send_to_clients, args=(data,))
        send_thread.start()
        send_thread.join()  # Espera até que a thread de envio termine

# Inicia a thread principal
main_thread = threading.Thread(target=handle_messages)
main_thread.start()

# Aguarda a thread principal terminar (o que nunca deve acontecer)
main_thread.join()
