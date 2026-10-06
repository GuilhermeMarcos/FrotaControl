package br.com.frotacontrol.model;

import java.time.LocalDate;

/**
 * Motorista autorizado a retirar veículos da frota.
 * Corresponde à tabela "motorista" do banco de dados.
 */
public class Motorista {

    private int id;
    private String nome;
    private String cpf;
    private String cnh;
    private LocalDate validadeCnh;
    private String telefone;
    private boolean ativo = true;

    public Motorista() {
    }

    public Motorista(String nome, String cpf, String cnh, LocalDate validadeCnh, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.cnh = cnh;
        this.validadeCnh = validadeCnh;
        this.telefone = telefone;
    }

    /**
     * A CNH é considerada válida até o dia do vencimento (inclusive).
     */
    public boolean isCnhValida() {
        return validadeCnh != null && !validadeCnh.isBefore(LocalDate.now());
    }

    /**
     * Regra de negócio: só pode dirigir quem está ativo e com a CNH em dia.
     */
    public boolean podeDirigir() {
        return ativo && isCnhValida();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCnh() {
        return cnh;
    }

    public void setCnh(String cnh) {
        this.cnh = cnh;
    }

    public LocalDate getValidadeCnh() {
        return validadeCnh;
    }

    public void setValidadeCnh(LocalDate validadeCnh) {
        this.validadeCnh = validadeCnh;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return nome + " (CNH " + cnh + ")";
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
        return id == ((Motorista) outro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
