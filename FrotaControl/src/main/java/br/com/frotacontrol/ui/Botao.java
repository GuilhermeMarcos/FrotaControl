package br.com.frotacontrol.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Botão do guia de estilo, com três variações:
 * PRINCIPAL (azul), SECUNDARIO (branco com contorno) e PERIGO (contorno vermelho).
 * Quando recebe o foco pelo teclado, ganha um contorno por dentro da borda.
 */
public class Botao extends JButton {

    public enum Tipo { PRINCIPAL, SECUNDARIO, PERIGO }

    private Tipo tipo;

    public Botao(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setFont(Tema.TEXTO_FORTE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics medidas = getFontMetrics(getFont());
        return new Dimension(medidas.stringWidth(getText()) + 40, 36);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Tema.suavizar(g.create());
        RoundRectangle2D corpo = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8);
        boolean pressionado = getModel().isArmed() && getModel().isPressed();
        boolean sobre = getModel().isRollover();

        Color fundo;
        Color contorno;
        Color texto;
        if (!isEnabled()) {
            fundo = Tema.SOMENTE_LEITURA;
            contorno = Tema.LINHA;
            texto = Tema.BORDA;
        } else if (tipo == Tipo.PRINCIPAL) {
            fundo = (pressionado || sobre) ? Tema.AZUL_PLACA_ESCURO : Tema.AZUL_PLACA;
            contorno = fundo;
            texto = Color.WHITE;
        } else if (tipo == Tipo.PERIGO) {
            fundo = (pressionado || sobre) ? Tema.VERMELHO_FUNDO : Tema.SUPERFICIE;
            contorno = Tema.VERMELHO;
            texto = Tema.VERMELHO;
        } else {
            fundo = (pressionado || sobre) ? Tema.SOMENTE_LEITURA : Tema.SUPERFICIE;
            contorno = Tema.BORDA;
            texto = Tema.TEXTO;
        }

        g2.setColor(fundo);
        g2.fill(corpo);
        g2.setColor(contorno);
        g2.setStroke(new BasicStroke(tipo == Tipo.PERIGO ? 1.4f : 1f));
        g2.draw(corpo);

        if (isFocusOwner()) {
            // Contorno de foco: azul nos botões claros e branco, por dentro, no botão azul
            g2.setColor(tipo == Tipo.PRINCIPAL && isEnabled() ? Color.WHITE : Tema.AZUL_PLACA);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(3, 3, getWidth() - 6, getHeight() - 6, 6, 6));
        }

        g2.setFont(getFont());
        g2.setColor(texto);
        FontMetrics medidas = g2.getFontMetrics();
        int x = (getWidth() - medidas.stringWidth(getText())) / 2;
        int y = (getHeight() + medidas.getAscent() - medidas.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
