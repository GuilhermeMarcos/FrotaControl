package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.Motorista;
import br.com.frotacontrol.model.RegistroUso;
import br.com.frotacontrol.model.StatusVeiculo;
import br.com.frotacontrol.model.Veiculo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Tela inicial: mostra onde está cada veículo neste momento (quadro de chaves),
 * o resumo da frota e os avisos que pedem alguma ação.
 */
public class PainelInicio extends Tela {

    private static final int DIAS_DE_AVISO_CNH = 30;

    private final JLabel total = Tema.texto("", Tema.TITULO_PAINEL, Tema.TEXTO);
    private final JPanel selos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    private final BarraFrota barra = new BarraFrota();
    private final Tabela tabela = new Tabela("Placa", "Veículo", "Situação", "Com quem está", "Desde");
    private final JPanel avisos = new JPanel();
    private List<Veiculo> veiculos;

    public PainelInicio(JanelaPrincipal janela) {
        super(janela, "Início", "Onde está cada veículo neste momento");

        Botao saida = new Botao("Registrar saída (F5)", Botao.Tipo.PRINCIPAL);
        saida.addActionListener(evento -> janela.abrirSaida(null));
        Botao devolucao = new Botao("Registrar devolução (F6)", Botao.Tipo.SECUNDARIO);
        devolucao.addActionListener(evento -> janela.abrirDevolucao(null));
        acoes.add(saida);
        acoes.add(devolucao);

        // Resumo da frota: contagem em texto, selos e barra proporcional
        selos.setOpaque(false);
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        esquerda.setOpaque(false);
        total.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));
        esquerda.add(total);
        esquerda.add(selos);
        Tema.Painel resumo = new Tema.Painel();
        resumo.setLayout(new BorderLayout(16, 0));
        resumo.add(esquerda, BorderLayout.WEST);
        resumo.add(barra, BorderLayout.EAST);

        // Quadro de chaves
        tabela.larguras(108, 190, 170, 160, 130).colunaPlaca(0).colunaSituacao(2);
        tabela.setRowHeight(44);
        tabela.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "abrir");
        tabela.getActionMap().put("abrir", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                abrirSelecionado();
            }
        });
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    abrirSelecionado();
                }
            }
        });

        JPanel quadro = new JPanel(new BorderLayout(0, 8));
        quadro.setOpaque(false);
        quadro.add(Tema.texto("Quadro de chaves", Tema.TITULO_PAINEL, Tema.TEXTO), BorderLayout.NORTH);
        quadro.add(tabela.comRolagem(), BorderLayout.CENTER);
        quadro.add(Tema.texto("Selecione um veículo e pressione Enter para registrar a saída ou a devolução.",
                Tema.APOIO, Tema.TEXTO_APOIO), BorderLayout.SOUTH);

        // Avisos
        avisos.setOpaque(false);
        avisos.setLayout(new BoxLayout(avisos, BoxLayout.Y_AXIS));
        Tema.Painel painelAvisos = new Tema.Painel();
        painelAvisos.setLayout(new BorderLayout(0, 12));
        painelAvisos.setPreferredSize(new Dimension(296, 10));
        painelAvisos.add(Tema.texto("Avisos", Tema.TITULO_PAINEL, Tema.TEXTO), BorderLayout.NORTH);
        painelAvisos.add(avisos, BorderLayout.CENTER);
        JPanel colunaAvisos = new JPanel(new BorderLayout());
        colunaAvisos.setOpaque(false);
        colunaAvisos.setBorder(BorderFactory.createEmptyBorder(29, 0, 25, 0));
        colunaAvisos.add(painelAvisos);

        conteudo.add(resumo, BorderLayout.NORTH);
        conteudo.add(quadro, BorderLayout.CENTER);
        conteudo.add(colunaAvisos, BorderLayout.EAST);
    }

    @Override
    public void atualizar() {
        veiculos = controle.listarVeiculosAtivos();
        // Uma única consulta traz todos os veículos que estão na rua
        Map<Integer, RegistroUso> emUsoPorVeiculo = new HashMap<>();
        for (RegistroUso registro : controle.listarRegistrosEmAberto()) {
            emUsoPorVeiculo.put(registro.getVeiculo().getId(), registro);
        }
        int disponiveis = 0;
        int emUso = 0;
        int emManutencao = 0;

        tabela.limpar();
        for (Veiculo veiculo : veiculos) {
            String comQuem = "";
            String desde = "";
            if (veiculo.getStatus() == StatusVeiculo.EM_USO) {
                emUso++;
                RegistroUso registro = emUsoPorVeiculo.get(veiculo.getId());
                if (registro != null) {
                    comQuem = registro.getMotorista().getNome();
                    desde = Formatos.quando(registro.getDataSaida());
                }
            } else if (veiculo.getStatus() == StatusVeiculo.EM_MANUTENCAO) {
                emManutencao++;
                comQuem = "Oficina";
                desde = veiculo.getInicioManutencao() == null ? "" : veiculo.getInicioManutencao().format(Formatos.DATA);
            } else {
                disponiveis++;
            }
            tabela.linha(veiculo.getPlaca(), veiculo.getDescricao(), Tabela.Situacao.de(veiculo), comQuem, desde);
        }

        total.setText(veiculos.size() + (veiculos.size() == 1 ? " veículo na frota" : " veículos na frota"));
        selos.removeAll();
        selos.add(new Selo(disponiveis + (disponiveis == 1 ? " disponível" : " disponíveis"), Selo.Tipo.OK));
        selos.add(new Selo(emUso + " em uso", Selo.Tipo.USO));
        selos.add(new Selo(emManutencao + " em manutenção", Selo.Tipo.MANUTENCAO));
        barra.definir(disponiveis, emUso, emManutencao);

        montarAvisos();
        revalidate();
        repaint();
    }

    private void montarAvisos() {
        avisos.removeAll();
        LocalDate hoje = LocalDate.now();
        // Primeiro o que já venceu, depois o que está perto de vencer
        for (Motorista motorista : controle.listarMotoristas()) {
            LocalDate validade = motorista.getValidadeCnh();
            if (motorista.isAtivo() && validade.isBefore(hoje)) {
                aviso(Selo.Tipo.ERRO, "CNH vencida", motorista.getNome(), "Venceu em " + validade.format(Formatos.DATA));
            }
        }
        for (Motorista motorista : controle.listarMotoristas()) {
            LocalDate validade = motorista.getValidadeCnh();
            if (motorista.isAtivo() && !validade.isBefore(hoje) && !validade.isAfter(hoje.plusDays(DIAS_DE_AVISO_CNH))) {
                aviso(Selo.Tipo.MANUTENCAO, "CNH perto de vencer", motorista.getNome(),
                        "Vence em " + validade.format(Formatos.DATA));
            }
        }
        for (Veiculo veiculo : veiculos) {
            if (veiculo.getStatus() == StatusVeiculo.EM_MANUTENCAO) {
                String desde = veiculo.getInicioManutencao() == null ? ""
                        : "Desde " + veiculo.getInicioManutencao().format(Formatos.DATA);
                aviso(Selo.Tipo.MANUTENCAO, "Manutenção em andamento",
                        veiculo.getPlaca() + ", " + veiculo.getDescricao(), desde);
            }
        }
        if (avisos.getComponentCount() == 0) {
            avisos.add(Tema.texto("Nenhum aviso no momento.", Tema.APOIO, Tema.TEXTO_APOIO));
        }
    }

    private void aviso(Selo.Tipo tipo, String titulo, String linha1, String linha2) {
        JLabel cabeca = Tema.texto(titulo, Tema.TEXTO_FORTE, tipo.getCor());
        cabeca.setIcon(tipo.icone(14));
        cabeca.setIconTextGap(9);
        JLabel primeira = Tema.texto(linha1, Tema.APOIO, Tema.TEXTO);
        JLabel segunda = Tema.texto(linha2, Tema.APOIO, Tema.TEXTO_APOIO);
        primeira.setBorder(BorderFactory.createEmptyBorder(3, 23, 0, 0));
        segunda.setBorder(BorderFactory.createEmptyBorder(1, 23, 12, 0));

        JPanel bloco = new JPanel();
        bloco.setOpaque(false);
        bloco.setLayout(new BoxLayout(bloco, BoxLayout.Y_AXIS));
        bloco.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.LINHA));
        bloco.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloco.add(cabeca);
        bloco.add(primeira);
        bloco.add(segunda);
        bloco.setMaximumSize(new Dimension(Integer.MAX_VALUE, bloco.getPreferredSize().height));
        avisos.add(bloco);
        avisos.add(Box.createVerticalStrut(12));
    }

    /** Enter ou duplo clique: leva para a saída ou para a devolução, conforme a situação. */
    private void abrirSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        Veiculo veiculo = veiculos.get(linha);
        if (veiculo.getStatus() == StatusVeiculo.EM_USO) {
            janela.abrirDevolucao(controle.buscarRegistroAbertoDoVeiculo(veiculo));
        } else if (veiculo.getStatus() == StatusVeiculo.EM_MANUTENCAO) {
            Dialogos.erro(this, "Veículo em manutenção",
                    "O veículo " + veiculo.getPlaca() + " está em manutenção.",
                    "Para liberá-lo, conclua a manutenção no cadastro de veículos.");
        } else {
            janela.abrirSaida(veiculo);
        }
    }

    /** Barra proporcional: quantos veículos estão disponíveis, em uso e em manutenção. */
    private static class BarraFrota extends JComponent {

        private int[] partes = {0, 0, 0};
        private final Color[] cores = {Tema.VERDE, Tema.AZUL_PLACA, Tema.AMBAR};

        void definir(int disponiveis, int emUso, int emManutencao) {
            partes = new int[]{disponiveis, emUso, emManutencao};
            setToolTipText(disponiveis + " disponíveis, " + emUso + " em uso, " + emManutencao + " em manutenção");
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(280, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            int soma = partes[0] + partes[1] + partes[2];
            int x = 0;
            int y = (getHeight() - 12) / 2;
            for (int i = 0; i < 3 && soma > 0; i++) {
                int largura = getWidth() * partes[i] / soma;
                if (largura > 2) {
                    g2.setColor(cores[i]);
                    g2.fillRoundRect(x, y, largura - 2, 12, 4, 4);
                }
                x += largura;
            }
            g2.dispose();
        }
    }
}
