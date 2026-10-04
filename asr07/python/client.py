"""Cliente Python para o servidor Java (XML-RPC)."""

import argparse
from xmlrpc.client import Fault, ProtocolError, ServerProxy


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("method", choices=("soma", "subtracao", "maiuscula", "inverter", "contar_palavras"))
    parser.add_argument("values", nargs="*")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8000)
    args = parser.parse_args()

    numeric = args.method in ("soma", "subtracao")
    expected = 2 if numeric else 1
    if len(args.values) != expected:
        parser.error(f"{args.method} requer {expected} argumento(s)")
    try:
        values = tuple(map(float, args.values)) if numeric else tuple(args.values)
    except ValueError:
        parser.error("soma e subtracao requerem números")

    try:
        with ServerProxy(f"http://{args.host}:{args.port}/RPC2", allow_none=False) as server:
            result = getattr(server, args.method)(*values)
    except (OSError, Fault, ProtocolError) as exc:
        parser.exit(1, f"Erro na chamada remota: {exc}\n")
    print(result)


if __name__ == "__main__":
    main()
