package br.com.frotacontrol.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Campo de formulário completo: rótulo acima, campo no meio e, abaixo,
 * um texto de ajuda ou a mensagem de erro (com ícone e contorno vermelho).
 */
public class CampoForm extends JPanel {

    private final JComponent campo;
    private final JLabel mensagem = new JLabel();
    private String ajuda;

    public CampoForm(String rotulo, JComponent campo) {
        this(rotulo, campo, false, null);
    }

    public CampoForm(String rotulo, JComponent campo, boolean obrigatorio, String ajuda) {
        this.campo = campo;
        this.ajuda = ajuda;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = Tema.texto(rotulo + (obrigatorio ? " *" : ""), Tema.ROTULO, Tema.TEXTO);
        titulo.setLabelFor(campo);            // liga o rótulo ao campo para leitores de tela
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        mensagem.setAlignmentX(Component.LEFT_ALIGNMENT);
        mensagem.setIconTextGap(5);

        add(titulo);
        add(Box.createVerticalStrut(5));
        add(campo);
        add(Box.createVerticalStrut(3));
        add(mensagem);
        limparErro();
    }

    public final void setErro(String texto) {
        Tema.marcarErro(campo, true);
        mensagem.setIcon(Selo.Tipo.ERRO.icone(14));
        mensagem.setText(texto);
        mensagem.setFont(Tema.LEGENDA_FORTE);
        mensagem.setForeground(Tema.VERMELHO);
        mensagem.setVisible(true);
    }

    public final void limparErro() {
        Tema.marcarErro(campo, false);
        mensagem.setIcon(null);
        mensagem.setText(ajuda);
        mensagem.setFont(Tema.LEGENDA);
        mensagem.setForeground(Tema.TEXTO_APOIO);
        mensagem.setVisible(ajuda != null);
    }

    /** Troca o texto de ajuda exibido abaixo do campo quando não há erro. */
    public void setAjuda(String texto) {
        this.ajuda = texto;
        limparErro();
    }

    public JComponent getCampo() {
        return campo;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
