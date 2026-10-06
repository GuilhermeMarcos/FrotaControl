package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.Usuario;
import br.com.frotacontrol.service.ControleFrota;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Tela de login. Depois de três tentativas incorretas o sistema é encerrado (regra RN01).
 */
public class TelaLogin extends JFrame {

    private static final int MAXIMO_TENTATIVAS = 3;

    private final ControleFrota controle;
    private final Consumer<Usuario> aoEntrar;
    private final JTextField campoUsuario = Tema.campo();
    private final JPasswordField campoSenha = Tema.campoSenha();
    private final JPanel faixaErro = new JPanel(new BorderLayout(10, 0));
    private final JLabel textoErro = new JLabel();
    private int tentativas;

    public TelaLogin(ControleFrota controle, Consumer<Usuario> aoEntrar) {
        super("FrotaControl");
        this.controle = controle;
        this.aoEntrar = aoEntrar;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(JanelaPrincipal.iconeDaJanela());
        setResizable(false);

        JPanel painel = new JPanel();
        painel.setBackground(Tema.SUPERFICIE);
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(28, 40, 22, 40));

        painel.add(centro(new Marca()));
        painel.add(Box.createVerticalStrut(12));
        painel.add(centro(Tema.texto("FrotaControl", Tema.TITULO_TELA.deriveFont(24f), Tema.TEXTO)));
        painel.add(centro(Tema.texto("Entre com o seu usuário e a sua senha", Tema.TEXTO_NORMAL, Tema.TEXTO_APOIO)));
        painel.add(Box.createVerticalStrut(18));

        // Faixa de erro: aparece acima dos campos, com ícone e texto
        faixaErro.setBackground(Tema.VERMELHO_FUNDO);
        faixaErro.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel iconeErro = new JLabel(Selo.Tipo.ERRO.icone(16));
        iconeErro.setVerticalAlignment(JLabel.TOP);
        faixaErro.add(iconeErro, BorderLayout.WEST);
        textoErro.setForeground(Tema.VERMELHO);
        textoErro.setFont(Tema.LEGENDA);
        faixaErro.add(textoErro, BorderLayout.CENTER);
        faixaErro.setVisible(false);
        faixaErro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        painel.add(faixaErro);
        painel.add(Box.createVerticalStrut(12));

        painel.add(new CampoForm("Usuário", campoUsuario));
        painel.add(Box.createVerticalStrut(10));
        painel.add(new CampoForm("Senha", campoSenha));
        painel.add(Box.createVerticalStrut(8));

        char pontoDaSenha = campoSenha.getEchoChar();
        JCheckBox mostrar = Tema.caixa("Mostrar senha");
        mostrar.addActionListener(evento -> campoSenha.setEchoChar(mostrar.isSelected() ? (char) 0 : pontoDaSenha));
        JPanel linhaMostrar = new JPanel(new BorderLayout());
        linhaMostrar.setOpaque(false);
        linhaMostrar.add(mostrar, BorderLayout.WEST);
        linhaMostrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        painel.add(linhaMostrar);
        painel.add(Box.createVerticalStrut(14));

        Botao entrar = new Botao("Entrar", Botao.Tipo.PRINCIPAL);
        entrar.addActionListener(evento -> entrar());
        JPanel linhaBotao = new JPanel(new BorderLayout());
        linhaBotao.setOpaque(false);
        linhaBotao.add(entrar);
        linhaBotao.setPreferredSize(new Dimension(10, 40));
        linhaBotao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        painel.add(linhaBotao);
        getRootPane().setDefaultButton(entrar);   // a tecla Enter aciona "Entrar"

        painel.add(Box.createVerticalStrut(22));
        painel.add(centro(Tema.texto("Problemas para entrar? Procure o administrador da frota.",
                Tema.LEGENDA, Tema.TEXTO_APOIO)));

        // No BoxLayout vertical, todos os itens precisam do mesmo alinhamento
        for (Component item : painel.getComponents()) {
            ((JComponent) item).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        setContentPane(painel);
        setSize(440, 540);
        setLocationRelativeTo(null);
    }

    private void entrar() {
        Usuario usuario = controle.autenticar(campoUsuario.getText().trim(), new String(campoSenha.getPassword()));
        if (usuario != null) {
            dispose();
            aoEntrar.accept(usuario);
            return;
        }
        tentativas++;
        if (tentativas >= MAXIMO_TENTATIVAS) {
            Dialogos.erro(this, "Acesso bloqueado", "Número máximo de tentativas atingido.",
                    "O sistema será encerrado. Procure o administrador da frota.");
            System.exit(0);
        }
        textoErro.setText("<html><b>Usuário ou senha incorretos.</b><br>Tentativa " + tentativas + " de "
                + MAXIMO_TENTATIVAS + ". Verifique a tecla Caps Lock.</html>");
        faixaErro.setVisible(true);
        campoSenha.setText("");
        campoSenha.requestFocusInWindow();   // mantém o usuário digitado e volta para a senha
    }

    private static JComponent centro(JComponent componente) {
        componente.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel linha = new JPanel();
        linha.setOpaque(false);
        linha.setLayout(new BoxLayout(linha, BoxLayout.X_AXIS));
        linha.add(Box.createHorizontalGlue());
        linha.add(componente);
        linha.add(Box.createHorizontalGlue());
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
        return linha;
    }

    /** Marca do sistema: uma placa em miniatura com as letras FC. */
    private static class Marca extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(72, 44);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(1, 1, 70, 42, 10, 10));
            g2.setColor(Tema.AZUL_PLACA);
            g2.fill(new RoundRectangle2D.Float(3, 3, 66, 10, 6, 6));
            g2.setColor(Tema.TEXTO);
            g2.setStroke(new java.awt.BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Float(1, 1, 70, 42, 10, 10));
            g2.setFont(Tema.PLACA.deriveFont(20f));
            FontMetrics medidas = g2.getFontMetrics();
            g2.drawString("FC", (72 - medidas.stringWidth("FC")) / 2, 35);
            g2.dispose();
        }
    }
}
