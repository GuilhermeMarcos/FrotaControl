package br.com.frotacontrol.dao;

/**
 * Indica uma falha de comunicação com o banco de dados (servidor parado,
 * senha incorreta, tabela inexistente etc.). É uma exceção não verificada:
 * as telas não precisam tratá-la uma a uma, pois a classe Main exibe uma
 * mensagem única para esse tipo de problema.
 */
public class BancoDeDadosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BancoDeDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
