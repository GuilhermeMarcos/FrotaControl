# FrotaControl

Projeto Integrador - Etapa 4: sistema desktop de controle de frota em Java Swing,
com banco de dados MySQL.

## Como preparar e executar

1. **Criar o banco de dados.** Abra o arquivo `banco/frotacontrol.sql` no MySQL Workbench
   e execute o script inteiro. Ele cria o banco `frotacontrol`, as quatro tabelas e os
   dados iniciais de teste. Pode ser executado de novo sempre que quiser voltar ao estado inicial.
2. **Informar o usuário e a senha do MySQL.** Edite o arquivo
   `src/main/resources/banco.properties` e preencha `banco.usuario` e `banco.senha`
   com os dados do MySQL instalado no computador.
3. **Executar.** No NetBeans, abra a pasta `FrotaControl` (projeto Maven) e pressione F6.
   A classe principal é `br.com.frotacontrol.Main`. Na primeira execução o NetBeans
   baixa o driver do MySQL, por isso é preciso estar conectado à internet.

Se o sistema não conseguir conectar, ele mostra uma mensagem com o que conferir.

## Usuários cadastrados pelo script

| Usuário | Senha | Perfil |
|---|---|---|
| admin | admin123 | Administrador |
| jose | jose1234 | Operador |
| paula | paula1234 | Operador (inativo, não consegue entrar) |

## Banco de dados

| Tabela | Conteúdo |
|---|---|
| usuario | Quem acessa o sistema. A senha é gravada como hash SHA-256. |
| veiculo | Veículos da frota, com situação e quilometragem. |
| motorista | Motoristas autorizados, com a validade da CNH. |
| registro_uso | Saídas e devoluções, com chaves estrangeiras para as outras três tabelas. |

## Estrutura do código

- `br.com.frotacontrol.Main` - classe principal
- `br.com.frotacontrol.model` - Usuario, Veiculo, Motorista, RegistroUso e enums
- `br.com.frotacontrol.dao` - conexão e acesso ao banco de dados
  - `Conexao` - abre as conexões usando o arquivo `banco.properties`
  - `UsuarioDAO`, `VeiculoDAO`, `MotoristaDAO`, `RegistroUsoDAO` - consultas e gravações
- `br.com.frotacontrol.service` - `ControleFrota` (regras de negócio) e `Senhas` (hash)
- `br.com.frotacontrol.ui` - telas e componentes visuais

As telas chamam apenas a classe `ControleFrota`, que valida as regras de negócio
e usa os DAOs para ler e gravar no MySQL. A saída e a devolução alteram duas tabelas
e são gravadas em transação.

## Atalhos de teclado

| Tecla | Ação |
|---|---|
| Alt + I, V, M, S, H, U | Abrir Início, Veículos, Motoristas, Saída e devolução, Histórico, Usuários |
| F5 / F6 | Registrar saída / registrar devolução |
| Ctrl+N / Ctrl+S | Novo registro / salvar |
| Esc | Cancelar a edição |
| Enter | No quadro de chaves, abre a saída ou a devolução do veículo selecionado |
