import socket
import struct

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

# Loop principal do servidor
print("Servidor iniciado. Aguardando mensagens...")
while True:
    data, address = server_socket.recvfrom(1024)
    data = data.decode()
    # Adiciona novo cliente se ainda não estiver na lista
    if address not in clients:
        clients[address] = data
        data = data + " se conectou"
        print(f"{clients[address]}: {data}")            
    else:
        data = clients[address] + ": " + data
    
    
    # Retransmite a mensagem para todos os clientes
    for client_address in clients:
        if client_address != address:  # Não envia a mensagem de volta para o remetente original
            server_socket.sendto(data.encode(), client_address)
