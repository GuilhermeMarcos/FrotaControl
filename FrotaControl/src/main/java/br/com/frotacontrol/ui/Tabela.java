package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.Motorista;
import br.com.frotacontrol.model.Usuario;
import br.com.frotacontrol.model.Veiculo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Tabela no padrão visual do sistema: linhas altas, sem linhas verticais,
 * linha selecionada com fundo azul claro e barra lateral, placa desenhada
 * como uma placa em miniatura e situação exibida como selo.
 */
public class Tabela extends JTable {

    /** Valor de uma célula de situação: texto + tipo do selo. */
    public static class Situacao {

        final String texto;
        final Selo.Tipo tipo;

        public Situacao(String texto, Selo.Tipo tipo) {
            this.texto = texto;
            this.tipo = tipo;
        }

        public static Situacao de(Veiculo veiculo) {
            if (!veiculo.isAtivo()) {
                return new Situacao("Inativo", Selo.Tipo.INATIVO);
            }
            switch (veiculo.getStatus()) {
                case EM_USO:
                    return new Situacao("Em uso", Selo.Tipo.USO);
                case EM_MANUTENCAO:
                    return new Situacao("Em manutenção", Selo.Tipo.MANUTENCAO);
                default:
                    return new Situacao("Disponível", Selo.Tipo.OK);
            }
        }

        public static Situacao de(Motorista motorista) {
            if (!motorista.isAtivo()) {
                return new Situacao("Inativo", Selo.Tipo.INATIVO);
            }
            if (!motorista.isCnhValida()) {
                return new Situacao("CNH vencida", Selo.Tipo.ERRO);
            }
            return new Situacao("Apto", Selo.Tipo.OK);
        }

        public static Situacao de(Usuario usuario) {
            return usuario.isAtivo() ? new Situacao("Ativo", Selo.Tipo.OK) : new Situacao("Inativo", Selo.Tipo.INATIVO);
        }

        public Selo.Tipo getTipo() {
            return tipo;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    private final DefaultTableModel modelo;

    public Tabela(String... colunas) {
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        setModel(modelo);
        setFont(Tema.TEXTO_TABELA);
        setForeground(Tema.TEXTO);
        setBackground(Tema.SUPERFICIE);
        setRowHeight(40);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(Tema.LINHA);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(Tema.SELECAO);
        setSelectionForeground(Tema.TEXTO);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setDefaultRenderer(Object.class, new CelulaTexto(SwingConstants.LEFT));
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setPreferredSize(new Dimension(10, 36));
        getTableHeader().setDefaultRenderer(new CelulaCabecalho(SwingConstants.LEFT));
    }

    public void limpar() {
        modelo.setRowCount(0);
    }

    public void linha(Object... valores) {
        modelo.addRow(valores);
    }

    public Tabela larguras(int... larguras) {
        for (int i = 0; i < larguras.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        return this;
    }

    public Tabela colunaPlaca(int coluna) {
        getColumnModel().getColumn(coluna).setCellRenderer(new CelulaPlaca());
        getColumnModel().getColumn(coluna).setMinWidth(108);
        return this;
    }

    public Tabela colunaSituacao(int coluna) {
        getColumnModel().getColumn(coluna).setCellRenderer(new CelulaSituacao());
        getColumnModel().getColumn(coluna).setMinWidth(160);
        return this;
    }

    public Tabela colunaNumero(int coluna) {
        getColumnModel().getColumn(coluna).setCellRenderer(new CelulaTexto(SwingConstants.RIGHT));
        getColumnModel().getColumn(coluna).setHeaderRenderer(new CelulaCabecalho(SwingConstants.RIGHT));
        return this;
    }

    /** Seleciona a linha indicada e garante que ela esteja visível. */
    public void selecionar(int linha) {
        if (linha >= 0 && linha < getRowCount()) {
            setRowSelectionInterval(linha, linha);
            scrollRectToVisible(getCellRect(linha, 0, true));
        } else {
            clearSelection();
        }
    }

    public JScrollPane comRolagem() {
        JScrollPane rolagem = new JScrollPane(this);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.LINHA));
        rolagem.getViewport().setBackground(Tema.SUPERFICIE);
        return rolagem;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Barra lateral da linha selecionada: a seleção não depende só da cor de fundo
        int linha = getSelectedRow();
        if (linha >= 0) {
            Rectangle area = getCellRect(linha, 0, true);
            g.setColor(Tema.AZUL_PLACA);
            g.fillRect(0, area.y, 4, area.height);
        }
    }

    // ------------------------------------------------------------------
    private static class CelulaCabecalho extends DefaultTableCellRenderer {

        CelulaCabecalho(int alinhamento) {
            setHorizontalAlignment(alinhamento);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            setFont(Tema.ROTULO);
            setForeground(Tema.TEXTO_APOIO);
            setBackground(Tema.CABECALHO_TABELA);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.LINHA),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class CelulaTexto extends DefaultTableCellRenderer {

        CelulaTexto(int alinhamento) {
            setHorizontalAlignment(alinhamento);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            // A primeira coluna de texto da linha selecionada fica em negrito
            setFont(selecionada && coluna <= 1 ? Tema.TEXTO_FORTE.deriveFont(13.5f) : Tema.TEXTO_TABELA);
            return this;
        }
    }

    /** Desenha a placa do veículo como uma placa Mercosul em miniatura. */
    static class CelulaPlaca extends JComponent implements TableCellRenderer {

        private String placa = "";
        private Color fundo = Tema.SUPERFICIE;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            placa = valor == null ? "" : valor.toString();
            fundo = selecionada ? tabela.getSelectionBackground() : tabela.getBackground();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(fundo);
            g.fillRect(0, 0, getWidth(), getHeight());
            desenharPlaca((Graphics2D) g, placa, 12, (getHeight() - 26) / 2);
        }
    }

    public static void desenharPlaca(Graphics2D g, String placa, int x, int y) {
        Graphics2D g2 = Tema.suavizar(g.create());
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(x, y, 84, 26, 6, 6));
        g2.setColor(Tema.AZUL_PLACA);
        g2.fill(new RoundRectangle2D.Float(x + 1.5f, y + 1.5f, 81, 5.5f, 3, 3));
        g2.setColor(Tema.TEXTO);
        g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, 83, 25, 6, 6));
        g2.setFont(Tema.PLACA);
        FontMetrics medidas = g2.getFontMetrics();
        g2.drawString(placa, x + (84 - medidas.stringWidth(placa)) / 2, y + 21);
        g2.dispose();
    }

    private static class CelulaSituacao extends JComponent implements TableCellRenderer {

        private final Selo selo = new Selo("", Selo.Tipo.OK);
        private final JLabel texto = new JLabel();
        private Color fundo = Tema.SUPERFICIE;
        private boolean comSelo;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean foco, int linha, int coluna) {
            fundo = selecionada ? tabela.getSelectionBackground() : tabela.getBackground();
            comSelo = valor instanceof Situacao;
            if (comSelo) {
                Situacao situacao = (Situacao) valor;
                selo.definir(situacao.texto, situacao.tipo);
            } else {
                texto.setText(valor == null ? "" : valor.toString());
                texto.setFont(Tema.TEXTO_TABELA);
                texto.setForeground(Tema.TEXTO);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(fundo);
            g.fillRect(0, 0, getWidth(), getHeight());
            Graphics g2 = g.create();
            if (comSelo) {
                Dimension tamanho = selo.getPreferredSize();
                selo.setSize(tamanho);
                g2.translate(12, (getHeight() - tamanho.height) / 2);
                selo.paint(g2);
            } else {
                texto.setSize(Math.max(getWidth() - 24, 0), getHeight());
                g2.translate(12, 0);
                texto.paint(g2);
            }
            g2.dispose();
        }
    }
}
