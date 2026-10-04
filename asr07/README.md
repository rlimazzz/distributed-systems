# ASR 07 — clientes e servidores em Python e Java

Este exemplo reaproveita as operações da ASR 04 e expõe a mesma interface remota nos dois servidores. A interface usa **XML-RPC sobre HTTP**, na rota `/RPC2`, com texto em UTF-8. Python usa `xmlrpc.client` e `xmlrpc.server`; o código Java implementa o pequeno subconjunto de XML-RPC necessário usando apenas o JDK. Não são necessárias bibliotecas de terceiros. Requer Python 3 e JDK 11 ou superior.

| Método | Argumentos | Resultado |
| --- | --- | --- |
| `soma` | 2 números | número (`double`) |
| `subtracao` | 2 números | número (`double`) |
| `maiuscula` | 1 texto | texto |
| `inverter` | 1 texto | texto |
| `contar_palavras` | 1 texto | inteiro |

Em ambos os cenários, inicie o servidor em um terminal e execute o cliente em outro. O endereço padrão dos servidores é `127.0.0.1:8000`; para computadores diferentes, faça o servidor escutar no IP da interface de rede e informe esse IP ao cliente. Use uma porta livre e acessível entre os computadores.

## 1. Cliente Python → servidor Java

Na raiz deste repositório:

```sh
mkdir -p asr07/build
javac -encoding UTF-8 -d asr07/build asr07/java/*.java
java -cp asr07/build JavaServer 127.0.0.1 8000
```

No segundo terminal:

```sh
python3 asr07/python/client.py soma 3 4
python3 asr07/python/client.py maiuscula 'olá, mundo'
python3 asr07/python/client.py contar_palavras 'olá mundo distribuído'
```

Para outro endereço ou porta, acrescente `--host IP --port PORTA` ao comando Python.

## 2. Cliente Java → servidor Python

No primeiro terminal:

```sh
python3 asr07/python/server.py --host 127.0.0.1 --port 8000
```

No segundo terminal, depois de compilar como acima:

```sh
java -cp asr07/build JavaClient 127.0.0.1 8000 soma 3 4
java -cp asr07/build JavaClient 127.0.0.1 8000 inverter 'distribuídos'
java -cp asr07/build JavaClient 127.0.0.1 8000 contar_palavras 'olá mundo distribuído'
```

Encerre o servidor com `Ctrl+C` antes de iniciar o outro na mesma porta. Para executar os testes de integração, use `python3 -m unittest discover -s asr07/tests -v`. Os testes dos pares mistos são ignorados automaticamente se Java ou `javac` não estiverem instalados.

## Texto para o campo da tarefa

Nos dois cenários, XML-RPC funciona como uma camada de middleware entre a lógica da aplicação e o transporte HTTP/TCP. O cliente invoca `soma`, `inverter` ou outra operação por nome; a camada de comunicação codifica nome, argumentos e tipos em XML, envia a mensagem e decodifica a resposta ou uma falha. O mesmo contrato funciona com cliente e servidor escritos em linguagens diferentes, dispensando um formato de bytes próprio para cada par. Isso facilita interoperabilidade, substituição de uma implementação e manutenção da interface. A chamada remota ainda precisa de endereço e porta; este exemplo não implementa descoberta de serviços, replicação, autenticação nem recuperação automática de falhas.

Se os cenários fossem construídos diretamente sobre sockets TCP, seria necessário definir e manter manualmente delimitadores ou tamanho das mensagens, representação de números e caracteres, codificação, leitura parcial, associação entre requisições e respostas e formato dos erros. XML-RPC fornece essas convenções e aproveita clientes e servidores HTTP existentes. O custo é o tamanho e a análise das mensagens XML, além de uma camada extra de processamento; um protocolo binário próprio pode ser mais enxuto e rápido em uma aplicação muito restrita. Middleware também não transforma a chamada remota em chamada local: há latência, indisponibilidade da rede e possibilidade de resposta perdida. Por isso, clientes reais precisam de tempos limite, tratamento de falhas e, ao repetir operações com efeitos, regras de idempotência.
