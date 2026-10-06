package br.com.frotacontrol.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;

/**
 * Selo de situação: ícone + texto sobre um fundo colorido.
 * Usado nas tabelas, no resumo da frota e nos formulários.
 */
public class Selo extends JComponent {

    public enum Tipo {
        OK(Icone.Forma.VISTO, Tema.VERDE, Tema.VERDE_FUNDO),
        USO(Icone.Forma.SETA, Tema.AZUL_PLACA, Tema.SELECAO),
        MANUTENCAO(Icone.Forma.LOSANGO, Tema.AMBAR, Tema.AMBAR_FUNDO),
        ERRO(Icone.Forma.XIS, Tema.VERMELHO, Tema.VERMELHO_FUNDO),
        INATIVO(Icone.Forma.ANEL, Tema.TEXTO_APOIO, Tema.INATIVO_FUNDO);

        final Icone.Forma forma;
        final Color cor;
        final Color fundo;

        Tipo(Icone.Forma forma, Color cor, Color fundo) {
            this.forma = forma;
            this.cor = cor;
            this.fundo = fundo;
        }

        public Icone icone(int tamanho) {
            return new Icone(forma, cor, tamanho);
        }

        public Color getCor() {
            return cor;
        }
    }

    private static final int ALTURA = 24;

    private String texto;
    private Tipo tipo;

    public Selo(String texto, Tipo tipo) {
        setFont(Tema.LEGENDA_FORTE);
        definir(texto, tipo);
    }

    public final void definir(String texto, Tipo tipo) {
        this.texto = texto;
        this.tipo = tipo;
        revalidate();
        repaint();
    }

    public String getTexto() {
        return texto;
    }

    /** Permite que leitores de tela anunciem o texto do selo. */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.LABEL;
                }

                @Override
                public String getAccessibleName() {
                    return texto;
                }
            };
        }
        return accessibleContext;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics medidas = getFontMetrics(getFont());
        return new Dimension(26 + medidas.stringWidth(texto) + 12, ALTURA);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g.create());
        int largura = getPreferredSize().width;
        int y = (getHeight() - ALTURA) / 2;
        g2.setColor(tipo.fundo);
        g2.fill(new RoundRectangle2D.Float(0, y, largura, ALTURA, ALTURA, ALTURA));
        tipo.icone(14).paintIcon(this, g2, 7, y + 5);
        g2.setFont(getFont());
        g2.setColor(tipo.cor);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(texto, 26, y + (ALTURA + medidas.getAscent() - medidas.getDescent()) / 2);
        g2.dispose();
    }
}
