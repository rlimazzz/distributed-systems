"""Cliente TCP para executar comandos no servidor de Sistemas Distribuídos."""

import sys
from socket import AF_INET, SOCK_STREAM, socket

from constCS import HOST, PORT


EXEMPLO = "SOMA:3:4;SUB:10:2;MAIUSCULA:ola mundo;INVERTER:distribuidos;PALAVRAS:ola mundo"


def main():
    # A requisição pode ser passada na linha de comando; sem argumentos, usa o exemplo.
    requisicao = " ".join(sys.argv[1:]).strip() or EXEMPLO

    try:
        with socket(AF_INET, SOCK_STREAM) as conexao:
            conexao.connect((HOST, PORT))
            conexao.sendall(requisicao.encode("utf-8"))
            resposta = conexao.recv(1024).decode("utf-8")
    except OSError as erro:
        raise SystemExit(f"Não foi possível comunicar com o servidor: {erro}") from erro

    print(f"Requisição: {requisicao}")
    print(f"Resposta: {resposta}")


if __name__ == "__main__":
    main()
