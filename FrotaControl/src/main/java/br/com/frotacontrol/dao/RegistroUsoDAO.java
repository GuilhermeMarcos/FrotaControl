package br.com.frotacontrol.dao;

import br.com.frotacontrol.model.RegistroUso;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela "registro_uso".
 *
 * As consultas usam JOIN para trazer, na mesma leitura, o veículo, o
 * motorista e o usuário de cada registro. A saída e a devolução alteram
 * duas tabelas ao mesmo tempo e por isso são feitas em transação: ou as
 * duas alterações são gravadas, ou nenhuma.
 */
public class RegistroUsoDAO {

    private static final String CONSULTA =
            "SELECT r.id, r.data_saida, r.data_devolucao, r.km_saida, r.km_devolucao, r.destino, r.observacao, "
            + "v.id AS v_id, v.placa AS v_placa, v.marca AS v_marca, v.modelo AS v_modelo, v.ano AS v_ano, "
            + "v.quilometragem AS v_quilometragem, v.status AS v_status, "
            + "v.inicio_manutencao AS v_inicio_manutencao, v.ativo AS v_ativo, "
            + "m.id AS m_id, m.nome AS m_nome, m.cpf AS m_cpf, m.cnh AS m_cnh, "
            + "m.validade_cnh AS m_validade_cnh, m.telefone AS m_telefone, m.ativo AS m_ativo, "
            + "u.id AS u_id, u.nome AS u_nome, u.login AS u_login, u.senha AS u_senha, "
            + "u.perfil AS u_perfil, u.ativo AS u_ativo "
            + "FROM registro_uso r "
            + "JOIN veiculo v ON v.id = r.veiculo_id "
            + "JOIN motorista m ON m.id = r.motorista_id "
            + "JOIN usuario u ON u.id = r.usuario_id ";

    /**
     * Histórico de utilização, do mais recente para o mais antigo.
     *
     * @param veiculoId   id do veículo ou null para todos
     * @param motoristaId id do motorista ou null para todos
     */
    public List<RegistroUso> consultar(LocalDate inicio, LocalDate fim, Integer veiculoId, Integer motoristaId) {
        StringBuilder sql = new StringBuilder(CONSULTA).append("WHERE r.data_saida >= ? AND r.data_saida < ? ");
        List<Object> parametros = new ArrayList<>();
        parametros.add(Date.valueOf(inicio));
        parametros.add(Date.valueOf(fim.plusDays(1)));     // inclui o dia final inteiro
        if (veiculoId != null) {
            sql.append("AND r.veiculo_id = ? ");
            parametros.add(veiculoId);
        }
        if (motoristaId != null) {
            sql.append("AND r.motorista_id = ? ");
            parametros.add(motoristaId);
        }
        sql.append("ORDER BY r.data_saida DESC, r.id DESC");
        return consultar(sql.toString(), parametros.toArray());
    }

    /** Veículos que estão na rua: registros ainda sem data de devolução. */
    public List<RegistroUso> listarEmAberto() {
        return consultar(CONSULTA + "WHERE r.data_devolucao IS NULL ORDER BY r.data_saida");
    }

    public RegistroUso buscarAbertoPorVeiculo(int veiculoId) {
        return primeiro(consultar(CONSULTA + "WHERE r.data_devolucao IS NULL AND r.veiculo_id = ?", veiculoId));
    }

    public RegistroUso buscarAbertoPorMotorista(int motoristaId) {
        return primeiro(consultar(CONSULTA + "WHERE r.data_devolucao IS NULL AND r.motorista_id = ?", motoristaId));
    }

    /**
     * Grava a saída e muda a situação do veículo para "Em uso", em uma única transação.
     *
     * @return false se o veículo deixou de estar disponível (nada é gravado)
     */
    public boolean registrarSaida(RegistroUso registro) {
        String inserir = "INSERT INTO registro_uso (veiculo_id, motorista_id, usuario_id, data_saida, km_saida, destino) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String ocupar = "UPDATE veiculo SET status = 'EM_USO' WHERE id = ? AND status = 'DISPONIVEL' AND ativo = 1";
        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement saida = conexao.prepareStatement(inserir, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement veiculo = conexao.prepareStatement(ocupar)) {
                veiculo.setInt(1, registro.getVeiculo().getId());
                if (veiculo.executeUpdate() == 0) {
                    conexao.rollback();
                    return false;
                }
                saida.setInt(1, registro.getVeiculo().getId());
                saida.setInt(2, registro.getMotorista().getId());
                saida.setInt(3, registro.getResponsavel().getId());
                saida.setTimestamp(4, Timestamp.valueOf(registro.getDataSaida()));
                saida.setInt(5, registro.getKmSaida());
                saida.setString(6, registro.getDestino());
                saida.executeUpdate();
                try (ResultSet chaves = saida.getGeneratedKeys()) {
                    if (chaves.next()) {
                        registro.setId(chaves.getInt(1));
                    }
                }
                conexao.commit();
                return true;
            } catch (SQLException erro) {
                conexao.rollback();
                throw erro;
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar a saída.", erro);
        }
    }

    /**
     * Grava a devolução, atualiza a quilometragem do veículo e o deixa
     * "Disponível" novamente, em uma única transação.
     *
     * @return false se o registro já tinha sido finalizado (nada é gravado)
     */
    public boolean registrarDevolucao(RegistroUso registro) {
        String fechar = "UPDATE registro_uso SET data_devolucao = ?, km_devolucao = ?, observacao = ? "
                + "WHERE id = ? AND data_devolucao IS NULL";
        String liberar = "UPDATE veiculo SET quilometragem = ?, status = 'DISPONIVEL' WHERE id = ?";
        try (Connection conexao = Conexao.abrir()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement devolucao = conexao.prepareStatement(fechar);
                    PreparedStatement veiculo = conexao.prepareStatement(liberar)) {
                devolucao.setTimestamp(1, Timestamp.valueOf(registro.getDataDevolucao()));
                devolucao.setInt(2, registro.getKmDevolucao());
                devolucao.setString(3, registro.getObservacao());
                devolucao.setInt(4, registro.getId());
                if (devolucao.executeUpdate() == 0) {
                    conexao.rollback();
                    return false;
                }
                veiculo.setInt(1, registro.getKmDevolucao());
                veiculo.setInt(2, registro.getVeiculo().getId());
                veiculo.executeUpdate();
                conexao.commit();
                return true;
            } catch (SQLException erro) {
                conexao.rollback();
                throw erro;
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar a devolução.", erro);
        }
    }

    private List<RegistroUso> consultar(String sql, Object... parametros) {
        List<RegistroUso> registros = new ArrayList<>();
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    registros.add(montar(resultado));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível consultar os registros de uso.", erro);
        }
        return registros;
    }

    private RegistroUso primeiro(List<RegistroUso> registros) {
        return registros.isEmpty() ? null : registros.get(0);
    }

    private RegistroUso montar(ResultSet resultado) throws SQLException {
        RegistroUso registro = new RegistroUso();
        registro.setId(resultado.getInt("id"));
        registro.setVeiculo(VeiculoDAO.montar(resultado, "v_"));
        registro.setMotorista(MotoristaDAO.montar(resultado, "m_"));
        registro.setResponsavel(UsuarioDAO.montar(resultado, "u_"));
        registro.setDataSaida(resultado.getTimestamp("data_saida").toLocalDateTime());
        Timestamp devolucao = resultado.getTimestamp("data_devolucao");
        registro.setDataDevolucao(devolucao == null ? null : devolucao.toLocalDateTime());
        registro.setKmSaida(resultado.getInt("km_saida"));
        registro.setKmDevolucao(resultado.getInt("km_devolucao"));
        registro.setDestino(resultado.getString("destino"));
        registro.setObservacao(resultado.getString("observacao"));
        return registro;
    }
}
