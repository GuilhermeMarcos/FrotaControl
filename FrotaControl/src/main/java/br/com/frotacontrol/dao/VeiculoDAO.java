package br.com.frotacontrol.dao;

import br.com.frotacontrol.model.StatusVeiculo;
import br.com.frotacontrol.model.Veiculo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela "veiculo".
 */
public class VeiculoDAO {

    private static final String COLUNAS =
            "id, placa, marca, modelo, ano, quilometragem, status, inicio_manutencao, ativo";

    public List<Veiculo> listar() {
        return consultar("SELECT " + COLUNAS + " FROM veiculo ORDER BY id");
    }

    public List<Veiculo> listarAtivos() {
        return consultar("SELECT " + COLUNAS + " FROM veiculo WHERE ativo = 1 ORDER BY id");
    }

    /** Veículos que podem sair agora: ativos e com a situação "Disponível". */
    public List<Veiculo> listarDisponiveis() {
        return consultar("SELECT " + COLUNAS + " FROM veiculo WHERE ativo = 1 AND status = 'DISPONIVEL' ORDER BY placa");
    }

    public Veiculo buscarPorId(int id) {
        return primeiro(consultar("SELECT " + COLUNAS + " FROM veiculo WHERE id = ?", id));
    }

    public Veiculo buscarPorPlaca(String placa) {
        return primeiro(consultar("SELECT " + COLUNAS + " FROM veiculo WHERE placa = ?", placa));
    }

    public void inserir(Veiculo veiculo) {
        String sql = "INSERT INTO veiculo (placa, marca, modelo, ano, quilometragem, status, inicio_manutencao, ativo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(comando, veiculo);
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    veiculo.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar o veículo.", erro);
        }
    }

    public void atualizar(Veiculo veiculo) {
        String sql = "UPDATE veiculo SET placa = ?, marca = ?, modelo = ?, ano = ?, quilometragem = ?, "
                + "status = ?, inicio_manutencao = ?, ativo = ? WHERE id = ?";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            preencher(comando, veiculo);
            comando.setInt(9, veiculo.getId());
            comando.executeUpdate();
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar o veículo.", erro);
        }
    }

    private void preencher(PreparedStatement comando, Veiculo veiculo) throws SQLException {
        comando.setString(1, veiculo.getPlaca());
        comando.setString(2, veiculo.getMarca());
        comando.setString(3, veiculo.getModelo());
        comando.setInt(4, veiculo.getAno());
        comando.setInt(5, veiculo.getQuilometragem());
        comando.setString(6, veiculo.getStatus().name());
        comando.setDate(7, veiculo.getInicioManutencao() == null ? null : Date.valueOf(veiculo.getInicioManutencao()));
        comando.setBoolean(8, veiculo.isAtivo());
    }

    private List<Veiculo> consultar(String sql, Object... parametros) {
        List<Veiculo> veiculos = new ArrayList<>();
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    veiculos.add(montar(resultado, ""));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível consultar os veículos.", erro);
        }
        return veiculos;
    }

    private Veiculo primeiro(List<Veiculo> veiculos) {
        return veiculos.isEmpty() ? null : veiculos.get(0);
    }

    /**
     * Monta um veículo a partir da linha atual do resultado.
     * O prefixo permite reaproveitar o método em consultas com JOIN (por exemplo, "v_").
     */
    static Veiculo montar(ResultSet resultado, String prefixo) throws SQLException {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(resultado.getInt(prefixo + "id"));
        veiculo.setPlaca(resultado.getString(prefixo + "placa"));
        veiculo.setMarca(resultado.getString(prefixo + "marca"));
        veiculo.setModelo(resultado.getString(prefixo + "modelo"));
        veiculo.setAno(resultado.getInt(prefixo + "ano"));
        veiculo.setQuilometragem(resultado.getInt(prefixo + "quilometragem"));
        veiculo.setStatus(StatusVeiculo.valueOf(resultado.getString(prefixo + "status")));
        Date inicio = resultado.getDate(prefixo + "inicio_manutencao");
        veiculo.setInicioManutencao(inicio == null ? null : inicio.toLocalDate());
        veiculo.setAtivo(resultado.getBoolean(prefixo + "ativo"));
        return veiculo;
    }
}
