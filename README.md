## Distributed Systems

Exams and some codes bout distributed systems.

## Cliente e servidor TCP

O servidor aceita comandos no formato `NOME:argumento`, separados por `;`.
Além de `SOMA`, `SUB`, `MAIUSCULA` e `INVERTER`, o comando
`PALAVRAS:texto` devolve a quantidade de palavras no texto.

Execute `python server.py` e, em outro terminal, `python client.py` para
enviar a requisição de exemplo. Para escolher os comandos, execute:

```sh
python client.py "PALAVRAS:ola mundo;SOMA:2:5"
```

O endereço e a porta da conexão são definidos em `constCS.py`.
