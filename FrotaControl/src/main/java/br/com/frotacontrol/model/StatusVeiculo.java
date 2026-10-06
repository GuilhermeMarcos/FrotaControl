package br.com.frotacontrol.model;

/**
 * Situações possíveis de um veículo da frota.
 */
public enum StatusVeiculo {

    DISPONIVEL("Disponível"),
    EM_USO("Em uso"),
    EM_MANUTENCAO("Em manutenção");

    private final String descricao;

    StatusVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
