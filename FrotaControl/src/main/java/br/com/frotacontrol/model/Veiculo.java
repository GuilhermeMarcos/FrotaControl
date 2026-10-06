package br.com.frotacontrol.model;

import java.time.LocalDate;

/**
 * Veículo pertencente à frota da empresa.
 * Corresponde à tabela "veiculo" do banco de dados.
 */
public class Veiculo {

    private int id;
    private String placa;
    private String marca;
    private String modelo;
    private int ano;
    private int quilometragem;
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;
    private LocalDate inicioManutencao;
    private boolean ativo = true;

    public Veiculo() {
    }

    public Veiculo(String placa, String marca, String modelo, int ano, int quilometragem) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.quilometragem = quilometragem;
    }

    /**
     * Só pode sair o veículo ativo e com a situação "Disponível".
     */
    public boolean isDisponivel() {
        return ativo && status == StatusVeiculo.DISPONIVEL;
    }

    public String getDescricao() {
        return marca + " " + modelo;
    }

    public LocalDate getInicioManutencao() {
        return inicioManutencao;
    }

    public void setInicioManutencao(LocalDate inicioManutencao) {
        this.inicioManutencao = inicioManutencao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(int quilometragem) {
        this.quilometragem = quilometragem;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public void setStatus(StatusVeiculo status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return placa + " - " + marca + " " + modelo + " (" + ano + ")";
    }

    /** Dois objetos com o mesmo id representam o mesmo registro do banco de dados. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (outro == null || getClass() != outro.getClass() || id == 0) {
            return false;
        }
        return id == ((Veiculo) outro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
