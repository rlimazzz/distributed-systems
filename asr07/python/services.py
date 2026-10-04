"""Operações expostas por ambos os servidores XML-RPC da ASR 07."""


def soma(a, b):
    return float(a) + float(b)


def subtracao(a, b):
    return float(a) - float(b)


def maiuscula(texto):
    return texto.upper()


def inverter(texto):
    return texto[::-1]


def contar_palavras(texto):
    return len(texto.split())
