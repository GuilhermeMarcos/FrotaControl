package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.Motorista;
import br.com.frotacontrol.model.RegistroUso;
import br.com.frotacontrol.model.Veiculo;
import br.com.frotacontrol.service.RegraNegocioException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * Registro de saída (RF05) e de devolução (RF06) de veículos, em duas abas.
 * As listas só oferecem opções válidas, para evitar o erro antes que ele aconteça.
 */
public class PainelSaidaDevolucao extends Tela {

    private final CardLayout cartoes = new CardLayout();
    private final JPanel abas = new JPanel(cartoes);
    private final Aba abaSaida = new Aba("Registrar saída (F5)");
    private final Aba abaDevolucao = new Aba("Registrar devolução (F6)");
    private boolean carregando;

    // ---- aba de saída
    private final JComboBox<Veiculo> veiculo = Tema.combo();
    private final JComboBox<Motorista> motorista = Tema.combo();
    private final JTextField destino = Tema.campo();
    private final JTextField kmSaida = Tema.campoSomenteLeitura();
    private final JTextField dataHora = Tema.campoSomenteLeitura();
    private final CampoForm campoVeiculo = new CampoForm("Veículo", veiculo, true, " ");
    private final CampoForm campoMotorista = new CampoForm("Motorista", motorista, true, " ");
    private final CampoForm campoDestino = new CampoForm("Destino ou finalidade", destino, true, null);
    private final JPanel emUso = new JPanel();

    // ---- aba de devolução
    private final Tabela tabela = new Tabela("Placa", "Veículo", "Motorista", "Saída", "Km saída");
    private final List<RegistroUso> abertos = new ArrayList<>();
    private RegistroUso selecionado;
    private final CardLayout cartoesDetalhe = new CardLayout();
    private final JPanel detalhe = new JPanel(cartoesDetalhe);
    private final Placa placaResumo = new Placa("");
    private final JLabel resumo1 = Tema.texto(" ", Tema.TEXTO_TABELA, Tema.TEXTO);
    private final JLabel resumo2 = Tema.texto(" ", Tema.TEXTO_TABELA, Tema.TEXTO);
    private final JLabel resumo3 = Tema.texto(" ", Tema.TEXTO_TABELA, Tema.TEXTO_APOIO);
    private final JTextField kmDevolucao = Tema.campo();
    private final CampoForm campoKmDevolucao = new CampoForm("Km na devolução", kmDevolucao, true, null);
    private final JLabel kmRodados = Tema.texto("–", Tema.TEXTO_FORTE.deriveFont(15f), Tema.TEXTO);
    private final JTextArea observacao = Tema.areaDeTexto(3);

    public PainelSaidaDevolucao(JanelaPrincipal janela) {
        super(janela, "Saída e devolução", "Registro de retirada e de retorno dos veículos");

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(abaSaida);
        grupo.add(abaDevolucao);
        abaSaida.addActionListener(evento -> mostrarAba(true));
        abaDevolucao.addActionListener(evento -> mostrarAba(false));
        JPanel barraDeAbas = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barraDeAbas.setOpaque(false);
        barraDeAbas.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.LINHA));
        barraDeAbas.add(abaSaida);
        barraDeAbas.add(Box.createHorizontalStrut(8));
        barraDeAbas.add(abaDevolucao);

        abas.setOpaque(false);
        abas.add(montarAbaSaida(), "saida");
        abas.add(montarAbaDevolucao(), "devolucao");
        conteudo.add(barraDeAbas, BorderLayout.NORTH);
        conteudo.add(abas, BorderLayout.CENTER);
        mostrarAba(true);
    }

    // ------------------------------------------------------------------
    // Montagem das abas
    // ------------------------------------------------------------------

    private JPanel montarAbaSaida() {
        veiculo.setRenderer(renderizador(item -> ((Veiculo) item).getPlaca() + "   " + ((Veiculo) item).getDescricao()));
        motorista.setRenderer(renderizador(item -> ((Motorista) item).getNome()));
        veiculo.addActionListener(evento -> atualizarKmDeSaida());
        destino.addActionListener(evento -> registrarSaida());   // Enter no destino registra a saída
        // O erro some assim que a pessoa corrige o campo
        veiculo.addActionListener(evento -> limparErroSeEscolheu(campoVeiculo));
        motorista.addActionListener(evento -> limparErroSeEscolheu(campoMotorista));
        TelaCadastro.aoDigitar(destino, campoDestino::limparErro);

        JPanel formulario = coluna();
        formulario.add(esquerda(Tema.texto("Nova saída", Tema.TITULO_PAINEL, Tema.TEXTO)));
        formulario.add(Box.createVerticalStrut(14));
        formulario.add(campoVeiculo);
        formulario.add(Box.createVerticalStrut(8));
        formulario.add(campoMotorista);
        formulario.add(Box.createVerticalStrut(8));
        formulario.add(campoDestino);
        formulario.add(Box.createVerticalStrut(10));
        formulario.add(TelaCadastro.ladoALado(new CampoForm("Km de saída", kmSaida), new CampoForm("Data e hora", dataHora)));
        formulario.add(Box.createVerticalStrut(18));

        Botao registrar = new Botao("Registrar saída", Botao.Tipo.PRINCIPAL);
        registrar.addActionListener(evento -> registrarSaida());
        Botao limpar = new Botao("Limpar", Botao.Tipo.SECUNDARIO);
        limpar.addActionListener(evento -> limparSaida());
        formulario.add(botoes(registrar, limpar));

        emUso.setOpaque(false);
        emUso.setLayout(new BoxLayout(emUso, BoxLayout.Y_AXIS));
        JPanel lateral = coluna();
        lateral.add(esquerda(Tema.texto("Em uso agora", Tema.TITULO_PAINEL, Tema.TEXTO)));
        lateral.add(Box.createVerticalStrut(14));
        lateral.add(esquerda(emUso));

        JPanel aba = new JPanel(new BorderLayout(24, 0));
        aba.setOpaque(false);
        aba.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        aba.add(emPainel(formulario, 0), BorderLayout.CENTER);
        aba.add(emPainel(lateral, 424), BorderLayout.EAST);
        return aba;
    }

    private JPanel montarAbaDevolucao() {
        tabela.larguras(108, 150, 140, 100, 90).colunaPlaca(0).colunaNumero(4);
        tabela.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && !carregando) {
                int linha = tabela.getSelectedRow();
                selecionado = linha >= 0 ? abertos.get(linha) : null;
                preencherDevolucao();
            }
        });

        JPanel lista = new JPanel(new BorderLayout(0, 8));
        lista.setOpaque(false);
        lista.add(Tema.texto("Veículos em uso", Tema.TITULO_PAINEL, Tema.TEXTO), BorderLayout.NORTH);
        lista.add(tabela.comRolagem(), BorderLayout.CENTER);
        lista.add(Tema.texto("Selecione o veículo que está retornando.", Tema.APOIO, Tema.TEXTO_APOIO),
                BorderLayout.SOUTH);

        // Formulário da devolução, exibido quando há um veículo selecionado
        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titulo.setOpaque(false);
        titulo.add(Tema.texto("Devolução de  ", Tema.TITULO_PAINEL, Tema.TEXTO));
        titulo.add(placaResumo);

        JPanel rodados = coluna();
        rodados.setBorder(BorderFactory.createEmptyBorder(22, 16, 0, 0));
        rodados.add(Tema.texto("Km rodados", Tema.LEGENDA, Tema.TEXTO_APOIO));
        rodados.add(kmRodados);
        JPanel linhaKm = new JPanel(new BorderLayout());
        linhaKm.setOpaque(false);
        TelaCadastro.aoDigitar(kmDevolucao, this::atualizarKmRodados);

        JPanel formulario = coluna();
        formulario.add(esquerda(titulo));
        formulario.add(Box.createVerticalStrut(10));
        formulario.add(esquerda(resumo1));
        formulario.add(esquerda(resumo2));
        formulario.add(esquerda(resumo3));
        formulario.add(Box.createVerticalStrut(12));
        JPanel divisor = new JPanel();
        divisor.setBackground(Tema.LINHA);
        divisor.setPreferredSize(new Dimension(10, 1));
        divisor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        formulario.add(esquerda(divisor));
        formulario.add(Box.createVerticalStrut(12));
        linhaKm.add(campoKmDevolucao, BorderLayout.CENTER);
        linhaKm.add(rodados, BorderLayout.EAST);
        linhaKm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        formulario.add(esquerda(linhaKm));
        formulario.add(Box.createVerticalStrut(8));
        formulario.add(new CampoForm("Observação", observacao));
        formulario.add(Box.createVerticalStrut(18));

        Botao registrar = new Botao("Registrar devolução", Botao.Tipo.PRINCIPAL);
        registrar.addActionListener(evento -> registrarDevolucao());
        Botao cancelar = new Botao("Cancelar (Esc)", Botao.Tipo.SECUNDARIO);
        cancelar.addActionListener(evento -> cancelar());
        formulario.add(botoes(registrar, cancelar));

        JPanel vazio = coluna();
        vazio.add(Tema.texto("Nenhum veículo selecionado", Tema.TITULO_PAINEL, Tema.TEXTO));
        vazio.add(Box.createVerticalStrut(8));
        vazio.add(Tema.texto("Selecione um veículo em uso na lista ao lado.", Tema.TEXTO_NORMAL, Tema.TEXTO_APOIO));

        JPanel topoFormulario = new JPanel(new BorderLayout());
        topoFormulario.setOpaque(false);
        topoFormulario.add(formulario, BorderLayout.NORTH);
        JPanel topoVazio = new JPanel(new BorderLayout());
        topoVazio.setOpaque(false);
        topoVazio.add(vazio, BorderLayout.NORTH);
        detalhe.setOpaque(false);
        detalhe.add(topoVazio, "vazio");
        detalhe.add(topoFormulario, "formulario");

        Tema.Painel painel = new Tema.Painel();
        painel.setLayout(new BorderLayout());
        painel.setPreferredSize(new Dimension(424, 10));
        painel.add(detalhe);

        JPanel aba = new JPanel(new BorderLayout(24, 0));
        aba.setOpaque(false);
        aba.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        aba.add(lista, BorderLayout.CENTER);
        aba.add(painel, BorderLayout.EAST);
        return aba;
    }

    // ------------------------------------------------------------------
    // Carga dos dados
    // ------------------------------------------------------------------

    @Override
    public void atualizar() {
        carregando = true;

        Object veiculoAtual = veiculo.getSelectedItem();
        veiculo.removeAllItems();
        for (Veiculo item : controle.listarVeiculosDisponiveis()) {
            veiculo.addItem(item);
        }
        veiculo.setSelectedItem(null);
        if (veiculoAtual != null) {
            veiculo.setSelectedItem(veiculoAtual);     // continua vazio se ele não estiver mais disponível
        }
        veiculo.setEnabled(veiculo.getItemCount() > 0);
        campoVeiculo.limparErro();
        campoVeiculo.setAjuda(veiculo.getItemCount() > 0 ? "Só aparecem os veículos disponíveis."
                : "Nenhum veículo disponível no momento.");

        Object motoristaAtual = motorista.getSelectedItem();
        motorista.removeAllItems();
        for (Motorista item : controle.listarMotoristasAptos()) {
            motorista.addItem(item);
        }
        motorista.setSelectedItem(null);
        if (motoristaAtual != null) {
            motorista.setSelectedItem(motoristaAtual);
        }
        motorista.setEnabled(motorista.getItemCount() > 0);
        campoMotorista.limparErro();
        campoMotorista.setAjuda(motorista.getItemCount() > 0
                ? "Só aparecem motoristas ativos, com a CNH válida e sem outro veículo."
                : "Nenhum motorista apto no momento.");
        campoDestino.limparErro();

        abertos.clear();
        abertos.addAll(controle.listarRegistrosEmAberto());
        tabela.limpar();
        emUso.removeAll();
        int linhaSelecionada = -1;
        for (RegistroUso registro : abertos) {
            if (registro.equals(selecionado)) {
                linhaSelecionada = tabela.getRowCount();
                selecionado = registro;
            }
            tabela.linha(registro.getVeiculo().getPlaca(), registro.getVeiculo().getDescricao(),
                    registro.getMotorista().getNome(), Formatos.quando(registro.getDataSaida()),
                    Formatos.km(registro.getKmSaida()));
            emUso.add(itemEmUso(registro));
        }
        if (abertos.isEmpty()) {
            emUso.add(Tema.texto("Nenhum veículo em uso no momento.", Tema.APOIO, Tema.TEXTO_APOIO));
        } else {
            emUso.add(Tema.texto(Dialogos.html("Para devolver, use o botão ao lado do veículo ou pressione F6.", 280),
                    Tema.APOIO, Tema.TEXTO_APOIO));
        }
        tabela.selecionar(linhaSelecionada);
        if (linhaSelecionada < 0) {
            selecionado = null;
        }
        carregando = false;

        atualizarKmDeSaida();
        preencherDevolucao();
        revalidate();
        repaint();
    }

    private JPanel itemEmUso(RegistroUso registro) {
        JPanel identificacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        identificacao.setOpaque(false);
        identificacao.add(new Placa(registro.getVeiculo().getPlaca()));
        identificacao.add(Tema.texto("   " + registro.getVeiculo().getDescricao(), Tema.TEXTO_FORTE, Tema.TEXTO));

        Botao devolver = new Botao("Devolver", Botao.Tipo.SECUNDARIO);
        devolver.getAccessibleContext().setAccessibleName("Devolver " + registro.getVeiculo().getPlaca());
        devolver.addActionListener(evento -> abrirDevolucao(registro));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(identificacao, BorderLayout.WEST);
        topo.add(devolver, BorderLayout.EAST);

        JPanel textos = coluna();
        textos.add(Tema.texto(registro.getMotorista().getNome() + ", saiu " + Formatos.quando(registro.getDataSaida()),
                Tema.APOIO, Tema.TEXTO));
        textos.add(Tema.texto(registro.getDestino(), Tema.APOIO, Tema.TEXTO_APOIO));

        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.LINHA),
                BorderFactory.createEmptyBorder(0, 0, 12, 0)));
        item.add(topo, BorderLayout.NORTH);
        item.add(textos, BorderLayout.CENTER);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, item.getPreferredSize().height));

        JPanel comEspaco = new JPanel(new BorderLayout());
        comEspaco.setOpaque(false);
        comEspaco.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        comEspaco.add(item);
        comEspaco.setAlignmentX(Component.LEFT_ALIGNMENT);
        comEspaco.setMaximumSize(new Dimension(Integer.MAX_VALUE, comEspaco.getPreferredSize().height));
        return comEspaco;
    }

    private void limparErroSeEscolheu(CampoForm campo) {
        if (!carregando) {
            campo.limparErro();
        }
    }

    private void atualizarKmDeSaida() {
        if (carregando) {
            return;
        }
        Veiculo escolhido = (Veiculo) veiculo.getSelectedItem();
        kmSaida.setText(escolhido == null ? "" : Formatos.km(escolhido.getQuilometragem()));
        dataHora.setText(LocalDateTime.now().format(Formatos.DATA_HORA));
    }

    private void preencherDevolucao() {
        campoKmDevolucao.limparErro();
        kmDevolucao.setText("");
        observacao.setText("");
        kmRodados.setText("–");
        if (selecionado == null) {
            cartoesDetalhe.show(detalhe, "vazio");
            return;
        }
        placaResumo.definir(selecionado.getVeiculo().getPlaca());
        resumo1.setText(selecionado.getVeiculo().getDescricao() + ", com " + selecionado.getMotorista().getNome());
        resumo2.setText("Saída: " + Formatos.quando(selecionado.getDataSaida()) + ", com "
                + Formatos.km(selecionado.getKmSaida()) + " km");
        resumo3.setText("Destino: " + selecionado.getDestino());
        cartoesDetalhe.show(detalhe, "formulario");
    }

    /** Calcula os quilômetros rodados enquanto a pessoa digita (regra RN06). */
    private void atualizarKmRodados() {
        if (selecionado == null) {
            return;
        }
        campoKmDevolucao.limparErro();
        kmRodados.setText("–");
        String texto = kmDevolucao.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        try {
            int km = Formatos.inteiro(texto);
            if (km >= selecionado.getKmSaida()) {
                kmRodados.setText(Formatos.km(km - selecionado.getKmSaida()) + " km");
            } else if (texto.replace(".", "").length() >= String.valueOf(selecionado.getKmSaida()).length()) {
                campoKmDevolucao.setErro("Não pode ser menor que o km de saída ("
                        + Formatos.km(selecionado.getKmSaida()) + ").");
            }
        } catch (NumberFormatException erro) {
            campoKmDevolucao.setErro("Digite apenas números.");
        }
    }

    // ------------------------------------------------------------------
    // Ações
    // ------------------------------------------------------------------

    private void mostrarAba(boolean saida) {
        abaSaida.setSelected(saida);
        abaDevolucao.setSelected(!saida);
        cartoes.show(abas, saida ? "saida" : "devolucao");
    }

    public void abrirSaida(Veiculo escolhido) {
        mostrarAba(true);
        if (escolhido != null) {
            veiculo.setSelectedItem(escolhido);
            motorista.requestFocusInWindow();
        } else {
            veiculo.requestFocusInWindow();
        }
    }

    public void abrirDevolucao(RegistroUso registro) {
        mostrarAba(false);
        if (registro != null) {
            tabela.selecionar(abertos.indexOf(registro));
            kmDevolucao.requestFocusInWindow();
        } else {
            tabela.requestFocusInWindow();
        }
    }

    private void registrarSaida() {
        campoVeiculo.limparErro();
        campoMotorista.limparErro();
        campoDestino.limparErro();
        Veiculo veiculoEscolhido = (Veiculo) veiculo.getSelectedItem();
        Motorista motoristaEscolhido = (Motorista) motorista.getSelectedItem();
        boolean valido = true;
        if (veiculoEscolhido == null) {
            campoVeiculo.setErro("Escolha um veículo.");
            valido = false;
        }
        if (motoristaEscolhido == null) {
            campoMotorista.setErro("Escolha um motorista.");
            valido = false;
        }
        if (destino.getText().trim().isEmpty()) {
            campoDestino.setErro("Informe o destino ou a finalidade.");
            valido = false;
        }
        if (!valido) {
            return;
        }
        try {
            RegistroUso registro = controle.registrarSaida(veiculoEscolhido, motoristaEscolhido,
                    janela.getUsuario(), destino.getText());
            destino.setText("");
            veiculo.setSelectedItem(null);
            motorista.setSelectedItem(null);
            atualizar();
            janela.informar("Saída registrada: " + veiculoEscolhido.getPlaca() + " com "
                    + motoristaEscolhido.getNome() + ", às " + registro.getDataSaida().format(Formatos.HORA) + ".");
        } catch (RegraNegocioException erro) {
            Dialogos.erro(this, "Saída não autorizada", erro.getMessage(),
                    "Confira os dados ou escolha outro veículo ou motorista.");
            atualizar();
        }
    }

    private void limparSaida() {
        destino.setText("");
        veiculo.setSelectedItem(null);
        motorista.setSelectedItem(null);
        atualizar();
        veiculo.requestFocusInWindow();
    }

    private void registrarDevolucao() {
        if (selecionado == null) {
            return;
        }
        int km;
        try {
            km = Formatos.inteiro(kmDevolucao.getText());
        } catch (NumberFormatException erro) {
            campoKmDevolucao.setErro("Informe o km do painel do veículo.");
            kmDevolucao.requestFocusInWindow();
            return;
        }
        try {
            String placa = selecionado.getVeiculo().getPlaca();
            controle.registrarDevolucao(selecionado, km, observacao.getText().trim());
            String rodados = Formatos.km(selecionado.getKmRodados());
            selecionado = null;
            atualizar();
            janela.informar("Devolução registrada: " + placa + ", " + rodados + " km rodados.");
        } catch (RegraNegocioException erro) {
            campoKmDevolucao.setErro("Não pode ser menor que o km de saída ("
                    + Formatos.km(selecionado.getKmSaida()) + ").");
            kmDevolucao.requestFocusInWindow();
        }
    }

    /** Ctrl+S registra a operação da aba que está aberta. */
    @Override
    public void salvar() {
        if (abaSaida.isSelected()) {
            registrarSaida();
        } else {
            registrarDevolucao();
        }
    }

    /** Esc limpa o formulário da aba que está aberta. */
    @Override
    public void cancelar() {
        if (abaSaida.isSelected()) {
            limparSaida();
        } else {
            selecionado = null;
            atualizar();
        }
    }

    // ------------------------------------------------------------------
    // Pequenos auxiliares de layout
    // ------------------------------------------------------------------

    private static JPanel coluna() {
        JPanel painel = new JPanel();
        painel.setOpaque(false);
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        return painel;
    }

    private static <T extends JComponent> T esquerda(T componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        return componente;
    }

    private static JPanel botoes(Botao... botoes) {
        JPanel linha = new JPanel();
        linha.setOpaque(false);
        linha.setLayout(new BoxLayout(linha, BoxLayout.X_AXIS));
        for (Botao botao : botoes) {
            linha.add(botao);
            linha.add(Box.createHorizontalStrut(12));
        }
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return linha;
    }

    /** Coloca o conteúdo no alto de um painel branco arredondado. */
    private static Tema.Painel emPainel(JComponent conteudo, int largura) {
        Tema.Painel painel = new Tema.Painel();
        painel.setLayout(new BorderLayout());
        painel.add(conteudo, BorderLayout.NORTH);
        if (largura > 0) {
            painel.setPreferredSize(new Dimension(largura, 10));
        }
        return painel;
    }

    private static DefaultListCellRenderer renderizador(Function<Object, String> texto) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean selecionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor == null ? "" : texto.apply(valor), indice,
                        selecionado, foco);
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                return this;
            }
        };
    }

    /** Placa do veículo desenhada como uma placa em miniatura. */
    static class Placa extends JComponent {

        private String codigo;

        Placa(String codigo) {
            this.codigo = codigo;
        }

        void definir(String codigo) {
            this.codigo = codigo;
            repaint();
        }

        /** Permite que leitores de tela anunciem a placa. */
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
                        return "Placa " + codigo;
                    }
                };
            }
            return accessibleContext;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(84, 26);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Tabela.desenharPlaca((Graphics2D) g, codigo, 0, 0);
        }
    }

    /** Aba: a aba aberta fica em negrito, azul e com uma barra embaixo. */
    private static class Aba extends JToggleButton {

        Aba(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getFontMetrics(Tema.TEXTO_FORTE).stringWidth(getText()) + 32, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            g2.setFont(isSelected() ? Tema.TEXTO_FORTE : Tema.TEXTO_NORMAL);
            g2.setColor(isSelected() ? Tema.AZUL_PLACA : Tema.TEXTO_APOIO);
            FontMetrics medidas = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - medidas.stringWidth(getText())) / 2, 24);
            if (isSelected()) {
                g2.setColor(Tema.AZUL_PLACA);
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
            }
            if (isFocusOwner()) {
                g2.setColor(Tema.AZUL_PLACA);
                g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 13, 6, 6);
            }
            g2.dispose();
        }
    }
}
