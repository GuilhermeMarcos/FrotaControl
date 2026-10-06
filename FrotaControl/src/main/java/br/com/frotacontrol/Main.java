package br.com.frotacontrol;

import br.com.frotacontrol.dao.BancoDeDadosException;
import br.com.frotacontrol.dao.Conexao;
import br.com.frotacontrol.service.ControleFrota;
import br.com.frotacontrol.ui.Dialogos;
import br.com.frotacontrol.ui.JanelaPrincipal;
import br.com.frotacontrol.ui.TelaLogin;
import br.com.frotacontrol.ui.Tema;
import javax.swing.SwingUtilities;

/**
 * Projeto Integrador - Etapa 4
 * FrotaControl: sistema desktop de controle de frota, com banco de dados MySQL.
 *
 * Classe principal. Aplica o tema visual, confere a conexão com o banco
 * de dados e abre a tela de login.
 */
public class Main {

    public static void main(String[] args) {
        // Qualquer falha não tratada nas telas (por exemplo, o banco sair do ar) cai aqui
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) ->
                SwingUtilities.invokeLater(() -> mostrarErro(erro)));

        // Toda a interface Swing deve ser criada na thread de eventos
        SwingUtilities.invokeLater(() -> {
            Tema.aplicar();
            try {
                Conexao.testar();
            } catch (BancoDeDadosException erro) {
                Dialogos.erro(null, "Banco de dados", "Não foi possível conectar ao banco de dados.",
                        "Confira se o MySQL está em execução, se o script <b>banco/frotacontrol.sql</b> foi "
                        + "executado no MySQL Workbench e se o usuário e a senha estão corretos no arquivo "
                        + "<b>src/main/resources/banco.properties</b>.<br><br>Detalhe: " + erro.getCause().getMessage());
                System.exit(1);
            }
            abrirLogin(new ControleFrota());
        });
    }

    /** Abre o login; depois de entrar, abre a janela principal. "Sair do sistema" volta para cá. */
    private static void abrirLogin(ControleFrota controle) {
        TelaLogin login = new TelaLogin(controle,
                usuario -> new JanelaPrincipal(controle, usuario, () -> abrirLogin(controle)).setVisible(true));
        login.setVisible(true);
    }

    private static void mostrarErro(Throwable erro) {
        if (erro instanceof BancoDeDadosException) {
            Dialogos.erro(null, "Banco de dados", erro.getMessage(),
                    "Verifique se o MySQL está em execução e tente de novo.<br><br>Detalhe: "
                    + erro.getCause().getMessage());
        } else {
            Dialogos.erro(null, "Erro inesperado", "O sistema encontrou um problema e não concluiu a operação.",
                    "Detalhe: " + erro);
        }
    }
}
