package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.RegistroUso;
import br.com.frotacontrol.model.Usuario;
import br.com.frotacontrol.model.Veiculo;
import br.com.frotacontrol.service.ControleFrota;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Janela principal: menu lateral fixo, área central onde as telas são
 * trocadas (CardLayout) e barra de status na parte de baixo.
 */
public class JanelaPrincipal extends JFrame {

    public static final String INICIO = "inicio";
    public static final String VEICULOS = "veiculos";
    public static final String MOTORISTAS = "motoristas";
    public static final String SAIDA = "saida";
    public static final String HISTORICO = "historico";
    public static final String USUARIOS = "usuarios";

    private final ControleFrota controle;
    private final Usuario usuario;
    private final CardLayout cartoes = new CardLayout();
    private final JPanel area = new JPanel(cartoes);
    private final Map<String, Tela> telas = new LinkedHashMap<>();
    private final Map<String, ItemMenu> itens = new LinkedHashMap<>();
    private final ButtonGroup grupoMenu = new ButtonGroup();
    private final JLabel mensagem = new JLabel();
    private final JLabel relogio = Tema.texto("", Tema.LEGENDA, Tema.TEXTO_APOIO);
    private final PainelSaidaDevolucao telaSaida;
    private Tela telaAtual;

    public JanelaPrincipal(ControleFrota controle, Usuario usuario, Runnable aoSair) {
        super("FrotaControl");
        this.controle = controle;
        this.usuario = usuario;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(iconeDaJanela());
        setSize(1280, 760);
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);

        JPanel menu = new JPanel();
        menu.setBackground(Tema.ASFALTO);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(224, 100));
        JLabel marca = Tema.texto("FrotaControl", Tema.TITULO_PAINEL.deriveFont(19f), Tema.MENU_TEXTO);
        marca.setBorder(BorderFactory.createEmptyBorder(22, 20, 0, 0));
        JLabel lema = Tema.texto("Controle de frota", Tema.LEGENDA, Tema.MENU_TEXTO_APOIO);
        lema.setBorder(BorderFactory.createEmptyBorder(2, 20, 22, 0));
        menu.add(marca);
        menu.add(lema);

        telaSaida = new PainelSaidaDevolucao(this);
        registrar(menu, INICIO, "Início", KeyEvent.VK_I, new PainelInicio(this));
        registrar(menu, VEICULOS, "Veículos", KeyEvent.VK_V, new PainelVeiculos(this));
        registrar(menu, MOTORISTAS, "Motoristas", KeyEvent.VK_M, new PainelMotoristas(this));
        registrar(menu, SAIDA, "Saída e devolução", KeyEvent.VK_S, telaSaida);
        registrar(menu, HISTORICO, "Histórico", KeyEvent.VK_H, new PainelHistorico(this));
        if (usuario.isAdministrador()) {
            // Regra RN09: só o administrador vê e acessa o cadastro de usuários
            registrar(menu, USUARIOS, "Usuários", KeyEvent.VK_U, new PainelUsuarios(this));
        }
        menu.add(Box.createVerticalGlue());
        JPanel divisor = new JPanel();
        divisor.setBackground(new Color(0x4B5563));
        divisor.setPreferredSize(new Dimension(224, 1));
        divisor.setMaximumSize(new Dimension(224, 1));
        divisor.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(divisor);
        ItemMenu sair = new ItemMenu("Sair do sistema", "");
        sair.addActionListener(evento -> {
            dispose();
            aoSair.run();
        });
        menu.add(sair);
        menu.add(Box.createVerticalStrut(8));

        JPanel status = new JPanel(new BorderLayout(16, 0));
        status.setBackground(Tema.SUPERFICIE);
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.LINHA),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        status.add(Tema.texto("Usuário: " + usuario.getNome() + " (" + usuario.getPerfil().getDescricao() + ")",
                Tema.LEGENDA, Tema.TEXTO_APOIO), BorderLayout.WEST);
        mensagem.setFont(Tema.LEGENDA_FORTE);
        mensagem.setForeground(Tema.TEXTO);
        mensagem.setHorizontalAlignment(SwingConstants.CENTER);
        status.add(mensagem, BorderLayout.CENTER);
        status.add(relogio, BorderLayout.EAST);

        add(menu, BorderLayout.WEST);
        add(area, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        configurarAtalhos();
        atualizarRelogio();
        new Timer(15000, evento -> atualizarRelogio()).start();
        mostrar(INICIO);
    }

    private void registrar(JPanel menu, String nome, String rotulo, int tecla, Tela tela) {
        ItemMenu item = new ItemMenu(rotulo, "Alt+" + KeyEvent.getKeyText(tecla));
        item.setMnemonic(tecla);          // Alt + letra abre a tela
        item.addActionListener(evento -> mostrar(nome));
        grupoMenu.add(item);
        menu.add(item);
        itens.put(nome, item);
        telas.put(nome, tela);
        area.add(tela, nome);
    }

    /** Teclas de atalho que valem em qualquer tela. */
    private void configurarAtalhos() {
        atalho("F5", () -> abrirSaida(null));
        atalho("F6", () -> abrirDevolucao(null));
        atalho("control N", () -> telaAtual.novo());
        atalho("control S", () -> telaAtual.salvar());
        atalho("ESCAPE", () -> telaAtual.cancelar());
    }

    private void atalho(String tecla, Runnable acao) {
        JComponent raiz = getRootPane();
        raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), tecla);
        raiz.getActionMap().put(tecla, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                acao.run();
            }
        });
    }

    private void atualizarRelogio() {
        relogio.setText(Formatos.agoraPorExtenso());
    }

    public final void mostrar(String nome) {
        telaAtual = telas.get(nome);
        telaAtual.atualizar();
        itens.get(nome).setSelected(true);
        cartoes.show(area, nome);
    }

    /** Abre a aba "Registrar saída", já com o veículo escolhido (se houver). */
    public void abrirSaida(Veiculo veiculo) {
        mostrar(SAIDA);
        telaSaida.abrirSaida(veiculo);
    }

    /** Abre a aba "Registrar devolução", já com o registro escolhido (se houver). */
    public void abrirDevolucao(RegistroUso registro) {
        mostrar(SAIDA);
        telaSaida.abrirDevolucao(registro);
    }

    /** Mostra a confirmação da última ação na barra de status, sem interromper o trabalho. */
    public void informar(String texto) {
        mensagem.setIcon(Selo.Tipo.OK.icone(14));
        mensagem.setText(texto);
    }

    public ControleFrota getControle() {
        return controle;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    static BufferedImage iconeDaJanela() {
        BufferedImage imagem = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Tema.suavizar(imagem.createGraphics());
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(2, 6, 28, 20, 5, 5);
        g2.setColor(Tema.AZUL_PLACA);
        g2.fillRoundRect(3, 7, 26, 6, 3, 3);
        g2.setColor(Tema.TEXTO);
        g2.drawRoundRect(2, 6, 28, 20, 5, 5);
        g2.dispose();
        return imagem;
    }

    // ------------------------------------------------------------------
    /** Item do menu lateral: o item da tela atual ganha fundo mais claro, barra amarela e negrito. */
    private static class ItemMenu extends JToggleButton {

        private final String atalho;

        ItemMenu(String texto, String atalho) {
            super(texto);
            this.atalho = atalho;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(224, 40));
            setPreferredSize(new Dimension(224, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            boolean ativo = isSelected();
            g2.setColor(ativo || getModel().isRollover() ? Tema.ASFALTO_ATIVO : Tema.ASFALTO);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (ativo) {
                g2.setColor(Tema.AMARELO_FAIXA);
                g2.fillRect(0, 0, 5, getHeight());
            }
            if (isFocusOwner()) {
                g2.setColor(Tema.AMARELO_FAIXA);
                g2.drawRect(7, 3, getWidth() - 12, getHeight() - 7);
            }
            g2.setFont(ativo ? Tema.TEXTO_FORTE : Tema.TEXTO_NORMAL);
            g2.setColor(Tema.MENU_TEXTO);
            FontMetrics medidas = g2.getFontMetrics();
            int base = (getHeight() + medidas.getAscent() - medidas.getDescent()) / 2;
            g2.drawString(getText(), 22, base);
            g2.setFont(Tema.LEGENDA.deriveFont(11.5f));
            g2.setColor(Tema.MENU_TEXTO_APOIO);
            g2.drawString(atalho, getWidth() - 14 - g2.getFontMetrics().stringWidth(atalho), base);
            g2.dispose();
        }
    }
}
