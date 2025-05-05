from flask import Flask, request, jsonify
import hashlib
import datetime as date
import json
import os
import requests
from flask_cors import CORS
import threading
import time


app = Flask(__name__)
CORS(app)  # Adiciona o CORS ao aplicativo Flask

class Block:
    def __init__(self, index, timestamp, cpf, nome, naturalidade, data_nascimento, previous_hash, hash=None):
        self.index = index
        self.timestamp = timestamp
        self.cpf = cpf
        self.nome = nome
        self.naturalidade = naturalidade
        self.data_nascimento = data_nascimento
        self.previous_hash = previous_hash
        self.hash = hash if hash else self.calculate_hash()

    def calculate_hash(self):
        sha = hashlib.sha256()
        sha.update(
            str(self.index).encode('utf-8')
            + str(self.timestamp).encode('utf-8')
            + self.cpf.encode('utf-8')
            + self.nome.encode('utf-8')
            + self.naturalidade.encode('utf-8')
            + self.data_nascimento.encode('utf-8')
            + str(self.previous_hash).encode('utf-8')
        )
        return sha.hexdigest()

    def to_dict(self):
        return {
            'index': self.index,
            'timestamp': self.timestamp.isoformat(),
            'cpf': self.cpf,
            'nome': self.nome,
            'naturalidade': self.naturalidade,
            'data_nascimento': self.data_nascimento,
            'previous_hash': self.previous_hash,
            'hash': self.hash,
        }

    @classmethod
    def from_dict(cls, block_dict):
        timestamp = date.datetime.fromisoformat(block_dict['timestamp'])
        return cls(
            block_dict['index'],
            timestamp,
            block_dict['cpf'],
            block_dict['nome'],
            block_dict['naturalidade'],
            block_dict['data_nascimento'],
            block_dict['previous_hash'],
            block_dict['hash'],
        )

class Blockchain:
    def __init__(self, filename):
        self.chain = []
        self.filename = filename
        self.load_from_file()

    def create_genesis_block(self):
        return Block(0, date.datetime.now(), '0', 'Genesis Block', 'N/A', 'N/A', '0')

    def add_block(self, cpf, nome, naturalidade, data_nascimento):
        if self.chain:
            new_block = Block(len(self.chain), date.datetime.now(), cpf, nome, naturalidade, data_nascimento, self.chain[-1].hash)
        else:
            new_block = self.create_genesis_block()

        self.chain.append(new_block)
        self.save_to_file()
        return new_block

    def is_valid(self):
        for i in range(1, len(self.chain)):
            current_block = self.chain[i]
            previous_block = self.chain[i - 1]
            if current_block.hash != current_block.calculate_hash() or current_block.previous_hash != previous_block.hash:
                return False
        return True

    def save_to_file(self):
        with open(self.filename, 'w') as f:
            json.dump([block.to_dict() for block in self.chain], f, indent=4)

    def load_from_file(self):
        if os.path.exists(self.filename):
            with open(self.filename, 'r') as f:
                blocks = json.load(f)
                self.chain = [Block.from_dict(block) for block in blocks]
        else:
            genesis_block = self.create_genesis_block()
            self.chain.append(genesis_block)
            self.save_to_file()

# blockchain do servidor 2
blockchain = Blockchain('blockchain2.json')

@app.route('/replicate_block', methods=['POST'])
def replicate_block():
    data = request.get_json()
    block = Block.from_dict(data)
    blockchain.chain.append(block)
    blockchain.save_to_file()
    return 'Block replicated', 201

@app.route('/chain', methods=['GET'])
def get_chain():
    return jsonify([block.to_dict() for block in blockchain.chain])

@app.route('/add_block', methods=['POST'])
def add_block():
    data = request.get_json()
    cpf = data['cpf']
    nome = data['nome']
    naturalidade = data['naturalidade']
    data_nascimento = data['data_nascimento']
    new_block = blockchain.add_block(cpf, nome, naturalidade, data_nascimento)
    
    return jsonify(new_block.to_dict()), 201

def check_server1():
    while True:
        try:
            response = requests.get('http://localhost:5000/chain')
            if response.status_code == 200:
                blockchain.chain = [Block.from_dict(block) for block in response.json()]
                blockchain.save_to_file()
            else:
                print("Servidor 1 está inativo. Assumindo o controle.")
                app.run(port=5000)
        except Exception as e:
            print(f"Erro ao conectar ao servidor 1: {e}")
            print("Servidor 1 está inativo. Assumindo o controle.")
            app.run(port=5000)
        time.sleep(10)

if __name__ == '__main__':
    threading.Thread(target=check_server1).start()
    app.run(port=5001)
