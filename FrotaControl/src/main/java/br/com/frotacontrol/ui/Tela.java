package br.com.frotacontrol.ui;

import br.com.frotacontrol.service.ControleFrota;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Base de todas as telas internas: cabeçalho com título, subtítulo e botões
 * de ação à direita, e uma área de conteúdo logo abaixo.
 */
public abstract class Tela extends JPanel {

    protected final JanelaPrincipal janela;
    protected final ControleFrota controle;
    protected final JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    protected final JPanel conteudo = new JPanel(new BorderLayout(24, 12));

    protected Tela(JanelaPrincipal janela, String titulo, String subtitulo) {
        this.janela = janela;
        this.controle = janela.getControle();
        setLayout(new BorderLayout(0, 14));
        setBackground(Tema.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 20));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.texto(titulo, Tema.TITULO_TELA, Tema.TEXTO));
        textos.add(Tema.texto(subtitulo, Tema.TEXTO_NORMAL, Tema.TEXTO_APOIO));

        acoes.setOpaque(false);
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(textos, BorderLayout.WEST);
        cabecalho.add(acoes, BorderLayout.EAST);

        conteudo.setOpaque(false);
        add(cabecalho, BorderLayout.NORTH);
        add(conteudo, BorderLayout.CENTER);
    }

    /** Chamado sempre que a tela é exibida, para recarregar os dados. */
    public abstract void atualizar();

    /** Atalho Ctrl+N. As telas de cadastro sobrescrevem. */
    public void novo() {
    }

    /** Atalho Ctrl+S. As telas com formulário sobrescrevem. */
    public void salvar() {
    }

    /** Atalho Esc. As telas com formulário sobrescrevem. */
    public void cancelar() {
    }
}
