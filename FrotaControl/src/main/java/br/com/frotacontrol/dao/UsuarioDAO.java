package br.com.frotacontrol.dao;

import br.com.frotacontrol.model.PerfilUsuario;
import br.com.frotacontrol.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela "usuario".
 */
public class UsuarioDAO {

    private static final String COLUNAS = "id, nome, login, senha, perfil, ativo";

    public List<Usuario> listar() {
        return consultar("SELECT " + COLUNAS + " FROM usuario ORDER BY id");
    }

    public Usuario buscarPorId(int id) {
        return primeiro(consultar("SELECT " + COLUNAS + " FROM usuario WHERE id = ?", id));
    }

    public Usuario buscarPorLogin(String login) {
        return primeiro(consultar("SELECT " + COLUNAS + " FROM usuario WHERE login = ?", login));
    }

    public int contarAdministradoresAtivos() {
        String sql = "SELECT COUNT(*) FROM usuario WHERE perfil = 'ADMINISTRADOR' AND ativo = 1";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql);
                ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getInt(1);
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível consultar os usuários.", erro);
        }
    }

    public void inserir(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, login, senha, perfil, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(comando, usuario);
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getInt(1));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar o usuário.", erro);
        }
    }

    public void atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, login = ?, senha = ?, perfil = ?, ativo = ? WHERE id = ?";
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            preencher(comando, usuario);
            comando.setInt(6, usuario.getId());
            comando.executeUpdate();
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível gravar o usuário.", erro);
        }
    }

    private void preencher(PreparedStatement comando, Usuario usuario) throws SQLException {
        comando.setString(1, usuario.getNome());
        comando.setString(2, usuario.getLogin());
        comando.setString(3, usuario.getSenha());
        comando.setString(4, usuario.getPerfil().name());
        comando.setBoolean(5, usuario.isAtivo());
    }

    private List<Usuario> consultar(String sql, Object... parametros) {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexao = Conexao.abrir();
                PreparedStatement comando = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                comando.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    usuarios.add(montar(resultado, ""));
                }
            }
        } catch (SQLException erro) {
            throw new BancoDeDadosException("Não foi possível consultar os usuários.", erro);
        }
        return usuarios;
    }

    private Usuario primeiro(List<Usuario> usuarios) {
        return usuarios.isEmpty() ? null : usuarios.get(0);
    }

    /**
     * Monta um usuário a partir da linha atual do resultado.
     * O prefixo permite reaproveitar o método em consultas com JOIN (por exemplo, "u_").
     */
    static Usuario montar(ResultSet resultado, String prefixo) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(resultado.getInt(prefixo + "id"));
        usuario.setNome(resultado.getString(prefixo + "nome"));
        usuario.setLogin(resultado.getString(prefixo + "login"));
        usuario.setSenha(resultado.getString(prefixo + "senha"));
        usuario.setPerfil(PerfilUsuario.valueOf(resultado.getString(prefixo + "perfil")));
        usuario.setAtivo(resultado.getBoolean(prefixo + "ativo"));
        return usuario;
    }
}
