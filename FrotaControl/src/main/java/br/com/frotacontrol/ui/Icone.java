package br.com.frotacontrol.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Ícones de situação. Cada situação tem uma FORMA diferente (e não só uma
 * cor diferente), para que a informação não dependa apenas da cor.
 */
public class Icone implements Icon {

    public enum Forma { VISTO, SETA, LOSANGO, XIS, ANEL }

    private final Forma forma;
    private final Color cor;
    private final int tamanho;

    public Icone(Forma forma, Color cor, int tamanho) {
        this.forma = forma;
        this.cor = cor;
        this.tamanho = tamanho;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = Tema.suavizar(g.create());
        g2.translate(x, y);
        float t = tamanho;
        g2.setColor(cor);
        switch (forma) {
            case VISTO:
                g2.fill(new Ellipse2D.Float(t * 0.07f, t * 0.07f, t * 0.86f, t * 0.86f));
                tracar(g2, t, 0.29f, 0.50f, 0.44f, 0.67f, 0.73f, 0.33f);
                break;
            case XIS:
                g2.fill(new Ellipse2D.Float(t * 0.07f, t * 0.07f, t * 0.86f, t * 0.86f));
                tracar(g2, t, 0.33f, 0.33f, 0.67f, 0.67f);
                tracar(g2, t, 0.67f, 0.33f, 0.33f, 0.67f);
                break;
            case SETA:
                g2.fill(poligono(t, 0.14f, 0.07f, 0.93f, 0.50f, 0.14f, 0.93f));
                break;
            case LOSANGO:
                g2.fill(poligono(t, 0.50f, 0.04f, 0.96f, 0.50f, 0.50f, 0.96f, 0.04f, 0.50f));
                break;
            default:
                g2.setStroke(new BasicStroke(t * 0.13f));
                g2.draw(new Ellipse2D.Float(t * 0.14f, t * 0.14f, t * 0.72f, t * 0.72f));
        }
        g2.dispose();
    }

    private void tracar(Graphics2D g2, float t, float... pontos) {
        Graphics2D traco = (Graphics2D) g2.create();
        traco.setColor(Color.WHITE);
        traco.setStroke(new BasicStroke(t * 0.11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D caminho = new Path2D.Float();
        caminho.moveTo(pontos[0] * t, pontos[1] * t);
        for (int i = 2; i < pontos.length; i += 2) {
            caminho.lineTo(pontos[i] * t, pontos[i + 1] * t);
        }
        traco.draw(caminho);
        traco.dispose();
    }

    private Path2D poligono(float t, float... pontos) {
        Path2D caminho = new Path2D.Float();
        caminho.moveTo(pontos[0] * t, pontos[1] * t);
        for (int i = 2; i < pontos.length; i += 2) {
            caminho.lineTo(pontos[i] * t, pontos[i + 1] * t);
        }
        caminho.closePath();
        return caminho;
    }

    @Override
    public int getIconWidth() {
        return tamanho;
    }

    @Override
    public int getIconHeight() {
        return tamanho;
    }
}
