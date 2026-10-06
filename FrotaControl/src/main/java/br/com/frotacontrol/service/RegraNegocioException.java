package br.com.frotacontrol.service;

/**
 * Exceção lançada quando uma operação viola uma regra de negócio do sistema
 * (por exemplo: tentar liberar um veículo que já está em uso).
 */
public class RegraNegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
