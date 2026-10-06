package br.com.frotacontrol.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.JTextComponent;

/**
 * Cores, fontes e componentes básicos definidos no guia de estilo (Etapa 2).
 * Todas as telas usam esta classe, para manter o mesmo padrão visual.
 */
public final class Tema {

    // ---------------- cores ----------------
    public static final Color ASFALTO = new Color(0x22272E);
    public static final Color ASFALTO_ATIVO = new Color(0x343C46);
    public static final Color AMARELO_FAIXA = new Color(0xF2B705);
    public static final Color MENU_TEXTO = new Color(0xF1F3F5);
    public static final Color MENU_TEXTO_APOIO = new Color(0xAEB6C0);
    public static final Color AZUL_PLACA = new Color(0x0B4EA2);
    public static final Color AZUL_PLACA_ESCURO = new Color(0x083B7C);
    public static final Color FUNDO = new Color(0xEEF1F4);
    public static final Color AREA_DE_TRABALHO = new Color(0xDDE2E8);
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color LINHA = new Color(0xD3D9E0);
    public static final Color BORDA = new Color(0x6B7480);
    public static final Color TEXTO = new Color(0x1C2128);
    public static final Color TEXTO_APOIO = new Color(0x4B5563);
    public static final Color SOMENTE_LEITURA = new Color(0xF1F3F5);
    public static final Color SELECAO = new Color(0xE3EDFA);
    public static final Color CABECALHO_TABELA = new Color(0xF6F8FA);
    public static final Color VERDE = new Color(0x14602E);
    public static final Color VERDE_FUNDO = new Color(0xE2F3E7);
    public static final Color AMBAR = new Color(0x7A4100);
    public static final Color AMBAR_FUNDO = new Color(0xFCEBD0);
    public static final Color VERMELHO = new Color(0xA1201A);
    public static final Color VERMELHO_FUNDO = new Color(0xFBE7E5);
    public static final Color INATIVO_FUNDO = new Color(0xE7EAEE);

    // ---------------- fontes ----------------
    private static final List<String> FONTES_INSTALADAS =
            Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    private static final String FAMILIA = escolher("Segoe UI", "Inter", Font.SANS_SERIF);
    private static final String FAMILIA_PLACA = escolher("Consolas", "Roboto Mono", Font.MONOSPACED);

    public static final Font TEXTO_NORMAL = new Font(FAMILIA, Font.PLAIN, 14);
    public static final Font TEXTO_FORTE = forte(14f);
    public static final Font TEXTO_TABELA = TEXTO_NORMAL.deriveFont(13.5f);
    public static final Font ROTULO = forte(13f);
    public static final Font APOIO = TEXTO_NORMAL.deriveFont(13f);
    public static final Font LEGENDA = TEXTO_NORMAL.deriveFont(12.5f);
    public static final Font LEGENDA_FORTE = forte(12.5f);
    public static final Font TITULO_TELA = forte(23f);
    public static final Font TITULO_PAINEL = forte(16f);
    public static final Font PLACA = new Font(FAMILIA_PLACA, Font.BOLD, 13);

    private Tema() {
    }

    private static String escolher(String... familias) {
        for (String familia : familias) {
            if (FONTES_INSTALADAS.contains(familia)) {
                return familia;
            }
        }
        return familias[familias.length - 1];
    }

    /** No Windows usa a variação Semibold da Segoe UI; nos demais casos, negrito. */
    private static Font forte(float tamanho) {
        if (FONTES_INSTALADAS.contains(FAMILIA + " Semibold")) {
            return new Font(FAMILIA + " Semibold", Font.PLAIN, 12).deriveFont(tamanho);
        }
        return new Font(FAMILIA, Font.BOLD, 12).deriveFont(tamanho);
    }

    /**
     * Ajusta os padrões do Swing uma única vez, antes de abrir a primeira janela.
     */
    public static void aplicar() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception erro) {
            // Se não for possível trocar a aparência, o sistema segue com a padrão.
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        for (Object chave : UIManager.getLookAndFeelDefaults().keySet().toArray()) {
            if (chave.toString().endsWith(".font")) {
                UIManager.put(chave, TEXTO_NORMAL);
            }
        }
        UIManager.put("Panel.background", FUNDO);
        UIManager.put("OptionPane.background", SUPERFICIE);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("ComboBox.background", SUPERFICIE);
        UIManager.put("ComboBox.selectionBackground", SELECAO);
        UIManager.put("ComboBox.selectionForeground", TEXTO);
        UIManager.put("TextField.selectionBackground", SELECAO);
        UIManager.put("PasswordField.selectionBackground", SELECAO);
        UIManager.put("TextArea.selectionBackground", SELECAO);
        UIManager.put("CheckBox.focus", AZUL_PLACA);
        UIManager.put("RadioButton.focus", AZUL_PLACA);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ToolTip.background", ASFALTO);
        UIManager.put("ToolTip.foreground", MENU_TEXTO);
    }

    // ---------------- textos ----------------
    public static JLabel texto(String texto, Font fonte, Color cor) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(fonte);
        rotulo.setForeground(cor);
        return rotulo;
    }

    // ---------------- campos ----------------
    public static JTextField campo() {
        JTextField campo = new JTextField();
        estilizar(campo);
        return campo;
    }

    public static JPasswordField campoSenha() {
        JPasswordField campo = new JPasswordField();
        estilizar(campo);
        return campo;
    }

    /** Campo preenchido pelo sistema: não recebe foco nem digitação. */
    public static JTextField campoSomenteLeitura() {
        JTextField campo = campo();
        campo.setEditable(false);
        campo.setFocusable(false);
        campo.setBackground(SOMENTE_LEITURA);
        return campo;
    }

    public static JTextArea areaDeTexto(int linhas) {
        JTextArea area = new JTextArea(linhas, 10);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // Em caixas de texto, a tecla Tab deve ir para o próximo campo
        area.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        area.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        estilizar(area);
        return area;
    }

    private static void estilizar(JTextComponent campo) {
        campo.setFont(TEXTO_NORMAL);
        campo.setForeground(TEXTO);
        campo.setBackground(SUPERFICIE);
        campo.setCaretColor(TEXTO);
        campo.setBorder(new BordaCampo());
        repintarAoMudarFoco(campo);
    }

    public static <T> JComboBox<T> combo() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(TEXTO_NORMAL);
        combo.setForeground(TEXTO);
        combo.setBackground(SUPERFICIE);
        combo.setMaximumRowCount(10);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return new BotaoSeta();
            }
        });
        combo.setBorder(new BordaCampo());
        repintarAoMudarFoco(combo);
        return combo;
    }

    public static JCheckBox caixa(String texto) {
        JCheckBox caixa = new JCheckBox(texto);
        caixa.setOpaque(false);
        caixa.setFont(TEXTO_NORMAL);
        caixa.setForeground(TEXTO);
        return caixa;
    }

    public static JRadioButton opcao(String texto) {
        JRadioButton opcao = new JRadioButton(texto);
        opcao.setOpaque(false);
        opcao.setFont(TEXTO_NORMAL);
        opcao.setForeground(TEXTO);
        return opcao;
    }

    private static void repintarAoMudarFoco(JComponent componente) {
        componente.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evento) {
                componente.repaint();
            }

            @Override
            public void focusLost(FocusEvent evento) {
                componente.repaint();
            }
        });
    }

    /** Marca ou desmarca um campo como "com erro" (contorno vermelho). */
    public static void marcarErro(JComponent campo, boolean erro) {
        campo.putClientProperty("erro", erro);
        campo.repaint();
    }

    public static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    // ------------------------------------------------------------------
    /**
     * Contorno dos campos: cinza no estado normal, azul e mais grosso quando
     * o campo está em foco e vermelho quando há erro de preenchimento.
     */
    public static class BordaCampo extends AbstractBorder {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int largura, int altura) {
            Graphics2D g2 = suavizar(g.create());
            boolean erro = c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("erro"));
            boolean foco = c.isFocusOwner();
            boolean leitura = !c.isEnabled() || (c instanceof JTextComponent && !((JTextComponent) c).isEditable());
            float espessura = (erro || foco) ? 2f : 1f;
            float meio = espessura / 2f;
            RoundRectangle2D contorno = new RoundRectangle2D.Float(x + meio, y + meio,
                    largura - espessura, altura - espessura, 8, 8);

            // Cobre os cantos com a cor do painel de trás, para o campo ficar arredondado
            Component fundo = c.getParent();
            while (fundo != null && !fundo.isOpaque() && !(fundo instanceof Painel)) {
                fundo = fundo.getParent();
            }
            if (fundo != null) {
                Area cantos = new Area(new Rectangle2D.Float(x, y, largura, altura));
                cantos.subtract(new Area(contorno));
                g2.setColor(fundo.getBackground());
                g2.fill(cantos);
            }
            g2.setColor(erro ? VERMELHO : foco ? AZUL_PLACA : leitura ? LINHA : BORDA);
            g2.setStroke(new BasicStroke(espessura));
            g2.draw(contorno);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(9, 10, 9, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(9, 10, 9, 10);
            return insets;
        }
    }

    /** Seta simples da lista de opções. */
    private static class BotaoSeta extends JButton {

        BotaoSeta() {
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            setFocusable(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g.create());
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.setColor(isEnabled() ? TEXTO_APOIO : LINHA);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{cx - 5, cx, cx + 5}, new int[]{cy - 2, cy + 3, cy - 2}, 3);
            g2.dispose();
        }
    }

    /**
     * Painel branco com cantos arredondados e contorno fino, usado nos
     * formulários e nos quadros de informação.
     */
    public static class Painel extends JPanel {

        public Painel() {
            setOpaque(false);
            setBackground(SUPERFICIE);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g.create());
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
            g2.setColor(LINHA);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
