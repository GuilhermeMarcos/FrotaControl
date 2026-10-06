package br.com.frotacontrol.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Mensagens em janela: erro de regra de negócio e confirmação.
 * A mensagem sempre diz o que aconteceu e o que a pessoa pode fazer.
 */
public final class Dialogos {

    private Dialogos() {
    }

    /** Erro: um único botão "Fechar", que já recebe o foco (Enter fecha). */
    public static void erro(Component pai, String titulo, String mensagem, String orientacao) {
        Botao fechar = new Botao("Fechar", Botao.Tipo.PRINCIPAL);
        mostrar(pai, titulo, conteudo(Selo.Tipo.ERRO, mensagem, orientacao), new Botao[]{fechar}, fechar);
    }

    /**
     * Confirmação antes de uma ação importante. O foco inicial fica em
     * "Cancelar", para evitar uma confirmação por engano.
     *
     * @return true se a pessoa confirmou
     */
    public static boolean confirmar(Component pai, String titulo, String pergunta, String explicacao,
            String textoConfirmar) {
        Botao cancelar = new Botao("Cancelar (Esc)", Botao.Tipo.SECUNDARIO);
        Botao confirmar = new Botao(textoConfirmar, Botao.Tipo.PERIGO);
        Object escolha = mostrar(pai, titulo, conteudo(Selo.Tipo.MANUTENCAO, pergunta, explicacao),
                new Botao[]{cancelar, confirmar}, cancelar);
        return escolha == confirmar;
    }

    private static JPanel conteudo(Selo.Tipo tipo, String principal, String detalhe) {
        JLabel icone = new JLabel(tipo.icone(30));
        icone.setVerticalAlignment(SwingConstants.TOP);
        icone.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 16));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.texto(html(principal, 360), Tema.TITULO_PAINEL, Tema.TEXTO));
        if (detalhe != null) {
            JLabel linha = Tema.texto(html(detalhe, 360), Tema.TEXTO_NORMAL, Tema.TEXTO);
            linha.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            textos.add(linha);
        }

        JPanel painel = new JPanel(new BorderLayout());
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        painel.add(icone, BorderLayout.WEST);
        painel.add(textos, BorderLayout.CENTER);
        return painel;
    }

    /** Quebra o texto em linhas dentro da largura indicada. */
    static String html(String texto, int largura) {
        return "<html><div style='width:" + largura + "px'>" + texto + "</div></html>";
    }

    private static Object mostrar(Component pai, String titulo, JPanel conteudo, Botao[] botoes, Botao inicial) {
        JOptionPane painel = new JOptionPane(conteudo, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, botoes, inicial);
        painel.setBackground(Tema.SUPERFICIE);
        for (Botao botao : botoes) {
            botao.addActionListener(evento -> painel.setValue(botao));
        }
        JDialog janela = painel.createDialog(pai, titulo);
        deixarBranco(painel);
        janela.setVisible(true);
        janela.dispose();
        return painel.getValue();
    }

    private static void deixarBranco(java.awt.Container container) {
        for (Component filho : container.getComponents()) {
            if (filho instanceof JPanel) {
                ((JPanel) filho).setOpaque(false);
                deixarBranco((JPanel) filho);
            }
        }
    }
}
