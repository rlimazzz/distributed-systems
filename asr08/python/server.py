"""Servidor Python para o cliente Java (XML-RPC)."""

import argparse
from xmlrpc.server import SimpleXMLRPCServer

from services import contar_palavras, inverter, maiuscula, soma, subtracao


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8000)
    args = parser.parse_args()

    with SimpleXMLRPCServer((args.host, args.port), allow_none=False, logRequests=True) as server:
        for function in (soma, subtracao, maiuscula, inverter, contar_palavras):
            server.register_function(function)
        print(f"Servidor Python em http://{args.host}:{args.port}/RPC2", flush=True)
        server.serve_forever()


if __name__ == "__main__":
    main()
