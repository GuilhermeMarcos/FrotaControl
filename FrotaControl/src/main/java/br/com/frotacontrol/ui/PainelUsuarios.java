package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.PerfilUsuario;
import br.com.frotacontrol.model.Usuario;
import br.com.frotacontrol.service.ControleFrota;
import br.com.frotacontrol.service.RegraNegocioException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Cadastro de usuários (RF02). Só o administrador acessa esta tela (RN09).
 */
public class PainelUsuarios extends TelaCadastro {

    private final JTextField nome = Tema.campo();
    private final JTextField login = Tema.campo();
    private final JPasswordField senha = Tema.campoSenha();
    private final JPasswordField confirmacao = Tema.campoSenha();
    private final JRadioButton administrador = Tema.opcao("Administrador");
    private final JRadioButton operador = Tema.opcao("Operador");
    private final JCheckBox ativo = Tema.caixa("Usuário ativo");
    private final CampoForm campoNome = new CampoForm("Nome completo", nome, true, null);
    private final CampoForm campoLogin = new CampoForm("Usuário (login)", login, true, null);
    private final CampoForm campoSenha = new CampoForm("Senha", senha, true, "");
    private final CampoForm campoConfirmacao = new CampoForm("Confirmar senha", confirmacao, true, null);

    private final List<Usuario> linhas = new ArrayList<>();
    private Usuario selecionado;

    public PainelUsuarios(JanelaPrincipal janela) {
        super(janela, "Usuários", "Quem pode entrar no sistema", "Novo usuário",
                "Nome", "Usuário", "Perfil", "Situação");
        tabela.larguras(210, 140, 150, 140).colunaSituacao(3);

        // Explicação dos perfis, em linguagem simples
        Tema.Painel explicacao = new Tema.Painel();
        explicacao.setLayout(new BoxLayout(explicacao, BoxLayout.Y_AXIS));
        explicacao.add(Tema.texto("O que cada perfil pode fazer", Tema.TITULO_PAINEL, Tema.TEXTO));
        explicacao.add(Box.createVerticalStrut(8));
        explicacao.add(Tema.texto(Dialogos.html("<b>Administrador:</b> acessa todas as telas, inclusive esta.", 400),
                Tema.TEXTO_TABELA, Tema.TEXTO));
        explicacao.add(Box.createVerticalStrut(6));
        explicacao.add(Tema.texto(Dialogos.html("<b>Operador:</b> registra saídas e devoluções e cadastra veículos e "
                + "motoristas. Não vê o item Usuários no menu.", 400), Tema.TEXTO_TABELA, Tema.TEXTO));
        explicacao.setAlignmentX(Component.LEFT_ALIGNMENT);
        explicacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, explicacao.getPreferredSize().height));
        rodapeLista.add(Box.createVerticalStrut(12));
        rodapeLista.add(explicacao);

        adicionar(campoNome, 10);
        adicionar(campoLogin, 10);
        adicionar(campoSenha, 10);
        adicionar(campoConfirmacao, 12);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(administrador);
        grupo.add(operador);
        JPanel perfis = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        perfis.setOpaque(false);
        perfis.add(administrador);
        perfis.add(Box.createHorizontalStrut(20));
        perfis.add(operador);
        perfis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        adicionar(Tema.texto("Perfil *", Tema.ROTULO, Tema.TEXTO), 4);
        adicionar(perfis, 8);
        ativo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        adicionar(ativo, 14);
        adicionar(botoesSalvarCancelar(), 0);
    }

    @Override
    public void atualizar() {
        carregando = true;
        linhas.clear();
        tabela.limpar();
        int linhaSelecionada = -1;
        for (Usuario usuario : controle.listarUsuarios()) {
            if (selecionado != null && usuario.getId() == selecionado.getId()) {
                linhaSelecionada = linhas.size();
                selecionado = usuario;      // usa os dados recém-lidos do banco
            }
            linhas.add(usuario);
            tabela.linha(usuario.getNome(), usuario.getLogin(), usuario.getPerfil().getDescricao(),
                    Tabela.Situacao.de(usuario));
        }
        tabela.selecionar(linhaSelecionada);
        if (linhaSelecionada < 0) {
            selecionado = null;
        }
        carregando = false;
        contagem.setText(linhas.size() + (linhas.size() == 1 ? " usuário" : " usuários"));
        preencherFormulario();
    }

    @Override
    protected void aoSelecionar(int linha) {
        selecionado = linha >= 0 ? linhas.get(linha) : null;
        preencherFormulario();
    }

    private void preencherFormulario() {
        limparErros();
        senha.setText("");
        confirmacao.setText("");
        String regraDaSenha = "Mínimo de " + ControleFrota.TAMANHO_MINIMO_SENHA + " caracteres.";
        if (selecionado == null) {
            tituloFormulario.setText("Novo usuário");
            nome.setText("");
            login.setText("");
            operador.setSelected(true);
            ativo.setSelected(true);
            campoSenha.setAjuda(regraDaSenha);
            return;
        }
        tituloFormulario.setText("Dados do usuário");
        nome.setText(selecionado.getNome());
        login.setText(selecionado.getLogin());
        administrador.setSelected(selecionado.isAdministrador());
        operador.setSelected(!selecionado.isAdministrador());
        ativo.setSelected(selecionado.isAtivo());
        campoSenha.setAjuda("Deixe em branco para manter a senha atual.");
    }

    private void limparErros() {
        campoNome.limparErro();
        campoLogin.limparErro();
        campoSenha.limparErro();
        campoConfirmacao.limparErro();
    }

    @Override
    public void novo() {
        selecionado = null;
        carregando = true;
        tabela.clearSelection();
        carregando = false;
        preencherFormulario();
        nome.requestFocusInWindow();
    }

    @Override
    public void salvar() {
        limparErros();

        boolean valido = preenchido(campoNome, nome, "Informe o nome.");
        valido &= preenchido(campoLogin, login, "Informe o usuário (login).");

        String novaSenha = new String(senha.getPassword());
        boolean manterSenha = selecionado != null && novaSenha.isEmpty();
        if (!manterSenha && novaSenha.length() < ControleFrota.TAMANHO_MINIMO_SENHA) {
            campoSenha.setErro("A senha deve ter pelo menos " + ControleFrota.TAMANHO_MINIMO_SENHA + " caracteres.");
            valido = false;
        } else if (!manterSenha && !novaSenha.equals(new String(confirmacao.getPassword()))) {
            campoConfirmacao.setErro("As senhas não são iguais.");
            valido = false;
        }
        if (!valido) {
            return;
        }

        Usuario dados = new Usuario();
        dados.setNome(nome.getText().trim());
        dados.setLogin(login.getText().trim());
        dados.setPerfil(administrador.isSelected() ? PerfilUsuario.ADMINISTRADOR : PerfilUsuario.OPERADOR);
        dados.setAtivo(ativo.isSelected());
        if (selecionado != null) {
            dados.setId(selecionado.getId());
        }
        try {
            // A senha vai separada: o sistema grava apenas o hash dela
            selecionado = controle.salvarUsuario(dados, manterSenha ? null : novaSenha);
            atualizar();
            janela.informar("Usuário " + selecionado.getNome() + " salvo.");
        } catch (RegraNegocioException erro) {
            if (erro.getMessage().contains("login")) {
                campoLogin.setErro(erro.getMessage());
                login.requestFocusInWindow();
            } else {
                Dialogos.erro(this, "Não foi possível salvar", erro.getMessage(), null);
            }
        }
    }

    @Override
    public void cancelar() {
        preencherFormulario();
    }
}
