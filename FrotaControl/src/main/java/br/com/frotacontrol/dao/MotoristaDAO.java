package br.com.frotacontrol.dao;

import br.com.frotacontrol.model.Motorista;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela "motorista".
 */
public class MotoristaDAO {

    private static final String COLUNAS = "id, nome, cpf, cnh, validade_cnh, telefone, ativo";

    public List<Motorista> listar() {
        return consultar("SELECT " + COLUNAS + " FROM motorista ORDER BY nome");
    }

    /**
     * Motoristas que podem retirar um veículo agora: ativos, com a CNH
     * dentro da validade e sem nenhum registro de uso em aberto.
     */
    public List<Motorista> listarAptos() {
        return consultar("SELECT " + COLUNAS + " FROM motorista "
                + "WHERE ativo = 1 AND validade_cnh >= CURDATE() "
                + "AND id NOT IN (SELECT motorista_id FROM registro_uso WHERE data_devolucao IS NULL) "
                + "ORDER BY nome");
    }

    public Motorista buscarPorId(int id) {
        return primeiro(consultar("SELECT " + COLUNAS + " FROM motorista WHERE id = ?", id));
    }

    public Motorista buscarPorCpf(String cpf) {
        return primeiro(consultar("SELECT " + COLUNAS + " FROM motorista WHERE cpf = ?", cpf));
    }

    public void inserir(Motorista motorista) {
        String sql = "INSERT INTO motorista (nome, cpf, cnh, validade_cnh, telefone, ativo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(comando, motorista);
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    motorista.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar o motorista.", erro);
        }
    }

    public void atualizar(Motorista motorista) {
        String sql = "UPDATE motorista SET nome = ?, cpf = ?, cnh = ?, validade_cnh = ?, telefone = ?, ativo = ? "
                + "WHERE id = ?";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            preencher(comando, motorista);
            comando.setInt(7, motorista.getId());
            comando.executeUpdate();
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar o motorista.", erro);
        }
    }

    private void preencher(PreparedStatement comando, Motorista motorista) throws SQLException {
        comando.setString(1, motorista.getNome());
        comando.setString(2, motorista.getCpf());
        comando.setString(3, motorista.getCnh());
        comando.setDate(4, Date.valueOf(motorista.getValidadeCnh()));
        comando.setString(5, motorista.getTelefone());
        comando.setBoolean(6, motorista.isAtivo());
    }

    private List<Motorista> consultar(String sql, Object... parametros) {
        List<Motorista> motoristas = new ArrayList<>();
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    motoristas.add(montar(resultado, ""));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível consultar os motoristas.", erro);
        }
        return motoristas;
    }

    private Motorista primeiro(List<Motorista> motoristas) {
        return motoristas.isEmpty() ? null : motoristas.get(0);
    }

    /**
     * Monta um motorista a partir da linha atual do resultado.
     * O prefixo permite reaproveitar o método em consultas com JOIN (por exemplo, "m_").
     */
    static Motorista montar(ResultSet resultado, String prefixo) throws SQLException {
        Motorista motorista = new Motorista();
        motorista.setId(resultado.getInt(prefixo + "id"));
        motorista.setNome(resultado.getString(prefixo + "nome"));
        motorista.setCpf(resultado.getString(prefixo + "cpf"));
        motorista.setCnh(resultado.getString(prefixo + "cnh"));
        motorista.setValidadeCnh(resultado.getDate(prefixo + "validade_cnh").toLocalDate());
        motorista.setTelefone(resultado.getString(prefixo + "telefone"));
        motorista.setAtivo(resultado.getBoolean(prefixo + "ativo"));
        return motorista;
    }
}
