package br.com.frotacontrol.model;

/**
 * Usuário que acessa o sistema (tela de login).
 * Corresponde à tabela "usuario" do banco de dados. O campo senha guarda o hash, nunca a senha digitada.
 */
public class Usuario {

    private int id;
    private String nome;
    private String login;
    private String senha;
    private PerfilUsuario perfil;
    private boolean ativo = true;

    public Usuario() {
    }

    public Usuario(String nome, String login, String senha, PerfilUsuario perfil) {
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil;
    }

    public boolean isAdministrador() {
        return perfil == PerfilUsuario.ADMINISTRADOR;
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    @Override
    public String toString() {
        return nome + " (" + perfil.getDescricao() + ")";
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
        return id == ((Usuario) outro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
