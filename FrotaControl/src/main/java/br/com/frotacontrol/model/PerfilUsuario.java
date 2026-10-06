package br.com.frotacontrol.model;

/**
 * Perfis de acesso ao sistema.
 * ADMINISTRADOR: acesso total (inclusive cadastro de usuários).
 * OPERADOR: cadastros de veículos/motoristas e registros de saída e devolução.
 */
public enum PerfilUsuario {

    ADMINISTRADOR("Administrador"),
    OPERADOR("Operador");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
