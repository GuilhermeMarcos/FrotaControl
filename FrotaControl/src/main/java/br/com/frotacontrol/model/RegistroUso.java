package br.com.frotacontrol.model;

import java.time.LocalDateTime;

/**
 * Registro de utilização de um veículo: guarda a saída e, depois, a devolução.
 * Corresponde à tabela "registro_uso" do banco de dados,
 * com chaves estrangeiras para veiculo, motorista e usuario.
 */
public class RegistroUso {

    private int id;
    private Veiculo veiculo;
    private Motorista motorista;
    private Usuario responsavel;
    private LocalDateTime dataSaida;
    private LocalDateTime dataDevolucao;
    private int kmSaida;
    private int kmDevolucao;
    private String destino;
    private String observacao;

    public RegistroUso() {
    }

    public RegistroUso(Veiculo veiculo, Motorista motorista, Usuario responsavel, String destino) {
        this.veiculo = veiculo;
        this.motorista = motorista;
        this.responsavel = responsavel;
        this.destino = destino;
        this.dataSaida = LocalDateTime.now().withNano(0);
        this.kmSaida = veiculo.getQuilometragem();
    }

    /**
     * Um registro está em aberto enquanto o veículo não foi devolvido.
     */
    public boolean isEmAberto() {
        return dataDevolucao == null;
    }

    /**
     * Fecha o registro com os dados da devolução.
     */
    public void registrarDevolucao(int kmFinal, String observacao) {
        this.kmDevolucao = kmFinal;
        this.observacao = observacao;
        this.dataDevolucao = LocalDateTime.now().withNano(0);
    }

    /**
     * Quilômetros rodados nesta utilização (zero enquanto estiver em aberto).
     */
    public int getKmRodados() {
        return isEmAberto() ? 0 : kmDevolucao - kmSaida;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public void setMotorista(Motorista motorista) {
        this.motorista = motorista;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDateTime dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public int getKmSaida() {
        return kmSaida;
    }

    public void setKmSaida(int kmSaida) {
        this.kmSaida = kmSaida;
    }

    public int getKmDevolucao() {
        return kmDevolucao;
    }

    public void setKmDevolucao(int kmDevolucao) {
        this.kmDevolucao = kmDevolucao;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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
        return id == ((RegistroUso) outro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
