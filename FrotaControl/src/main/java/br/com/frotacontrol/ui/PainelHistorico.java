package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.Motorista;
import br.com.frotacontrol.model.RegistroUso;
import br.com.frotacontrol.model.Veiculo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Histórico de utilização (RF07): todas as saídas e devoluções, com filtros
 * por período, veículo e motorista, e os totais do período.
 */
public class PainelHistorico extends Tela {

    private static final String TODOS = "Todos";
    private static final int DIAS_DO_PERIODO_PADRAO = 30;

    private final JTextField de = Tema.campo();
    private final JTextField ate = Tema.campo();
    private final JComboBox<Object> veiculo = Tema.combo();
    private final JComboBox<Object> motorista = Tema.combo();
    private final CampoForm campoDe = new CampoForm("De", de);
    private final CampoForm campoAte = new CampoForm("Até", ate);
    private final Tabela tabela = new Tabela("Nº", "Placa", "Motorista", "Saída", "Devolução", "Km rodados",
            "Destino", "Registrado por");
    private final JLabel quantidade = Tema.texto(" ", Tema.TEXTO_TABELA, Tema.TEXTO);
    private final JLabel totalRodado = Tema.texto(" ", Tema.TEXTO_FORTE.deriveFont(13.5f), Tema.TEXTO);
    private boolean carregando;

    public PainelHistorico(JanelaPrincipal janela) {
        super(janela, "Histórico", "Todas as saídas e devoluções registradas");

        DefaultListCellRenderer nomes = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean selecionado, boolean foco) {
                String texto = String.valueOf(valor);
                if (valor instanceof Veiculo) {
                    texto = ((Veiculo) valor).getPlaca() + "   " + ((Veiculo) valor).getDescricao();
                } else if (valor instanceof Motorista) {
                    texto = ((Motorista) valor).getNome();
                }
                super.getListCellRendererComponent(lista, texto, indice, selecionado, foco);
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                return this;
            }
        };
        veiculo.setRenderer(nomes);
        motorista.setRenderer(nomes);

        Botao filtrar = new Botao("Filtrar", Botao.Tipo.PRINCIPAL);
        filtrar.addActionListener(evento -> filtrar());
        Botao limpar = new Botao("Limpar filtros", Botao.Tipo.SECUNDARIO);
        limpar.addActionListener(evento -> limparFiltros());
        de.addActionListener(evento -> filtrar());     // Enter nos campos de data aplica o filtro
        ate.addActionListener(evento -> filtrar());

        JPanel filtros = new JPanel();
        filtros.setOpaque(false);
        filtros.setLayout(new BoxLayout(filtros, BoxLayout.X_AXIS));
        filtros.add(largura(campoDe, 150));
        filtros.add(Box.createHorizontalStrut(14));
        filtros.add(largura(campoAte, 150));
        filtros.add(Box.createHorizontalStrut(14));
        filtros.add(largura(new CampoForm("Veículo", veiculo), 250));
        filtros.add(Box.createHorizontalStrut(14));
        filtros.add(largura(new CampoForm("Motorista", motorista), 220));
        filtros.add(Box.createHorizontalStrut(10));
        filtros.add(largura(new CampoForm(" ", filtrar), 96));
        filtros.add(Box.createHorizontalStrut(12));
        filtros.add(largura(new CampoForm(" ", limpar), 150));
        filtros.add(Box.createHorizontalGlue());

        tabela.larguras(50, 108, 150, 150, 160, 110, 190, 130).colunaPlaca(1).colunaSituacao(4).colunaNumero(5);

        JPanel totais = new JPanel(new BorderLayout());
        totais.setOpaque(false);
        totais.add(quantidade, BorderLayout.WEST);
        totais.add(totalRodado, BorderLayout.EAST);

        conteudo.add(filtros, BorderLayout.NORTH);
        conteudo.add(tabela.comRolagem(), BorderLayout.CENTER);
        conteudo.add(totais, BorderLayout.SOUTH);
        periodoPadrao();
    }

    private static JPanel largura(JPanel campo, int largura) {
        Dimension tamanho = new Dimension(largura, campo.getPreferredSize().height + 18);
        campo.setPreferredSize(tamanho);
        campo.setMaximumSize(tamanho);
        campo.setAlignmentY(Component.TOP_ALIGNMENT);
        return campo;
    }

    private void periodoPadrao() {
        de.setText(LocalDate.now().minusDays(DIAS_DO_PERIODO_PADRAO).format(Formatos.DATA));
        ate.setText(LocalDate.now().format(Formatos.DATA));
    }

    @Override
    public void atualizar() {
        // Recarrega as listas dos filtros, mantendo o que estava escolhido
        carregando = true;
        Object veiculoAtual = veiculo.getSelectedItem();
        Object motoristaAtual = motorista.getSelectedItem();
        veiculo.removeAllItems();
        veiculo.addItem(TODOS);
        for (Veiculo item : controle.listarVeiculos()) {
            veiculo.addItem(item);
        }
        motorista.removeAllItems();
        motorista.addItem(TODOS);
        for (Motorista item : controle.listarMotoristas()) {
            motorista.addItem(item);
        }
        if (veiculoAtual != null) {
            veiculo.setSelectedItem(veiculoAtual);
        }
        if (motoristaAtual != null) {
            motorista.setSelectedItem(motoristaAtual);
        }
        carregando = false;
        filtrar();
    }

    private void limparFiltros() {
        periodoPadrao();
        veiculo.setSelectedItem(TODOS);
        motorista.setSelectedItem(TODOS);
        filtrar();
    }

    private void filtrar() {
        if (carregando) {
            return;
        }
        campoDe.limparErro();
        campoAte.limparErro();
        LocalDate inicio = lerData(campoDe, de);
        LocalDate fim = lerData(campoAte, ate);
        if (inicio == null || fim == null) {
            return;
        }
        if (fim.isBefore(inicio)) {
            campoAte.setErro("Deve ser depois da data inicial.");
            return;
        }

        // "Todos" equivale a não filtrar: o filtro é aplicado na própria consulta ao banco
        Object veiculoEscolhido = veiculo.getSelectedItem();
        Object motoristaEscolhido = motorista.getSelectedItem();
        List<RegistroUso> registros = controle.consultarHistorico(inicio, fim,
                veiculoEscolhido instanceof Veiculo ? (Veiculo) veiculoEscolhido : null,
                motoristaEscolhido instanceof Motorista ? (Motorista) motoristaEscolhido : null);
        tabela.limpar();
        int quantos = 0;
        int totalKm = 0;
        for (RegistroUso registro : registros) {
            Object devolucao = registro.isEmAberto()
                    ? new Tabela.Situacao("Em uso", Selo.Tipo.USO)
                    : registro.getDataDevolucao().format(Formatos.DATA_HORA);
            String rodados = registro.isEmAberto() ? "" : Formatos.km(registro.getKmRodados());
            String responsavel = registro.getResponsavel() == null ? ""
                    : registro.getResponsavel().getNome().split(" ")[0];
            tabela.linha(String.valueOf(registro.getId()), registro.getVeiculo().getPlaca(),
                    registro.getMotorista().getNome(), registro.getDataSaida().format(Formatos.DATA_HORA),
                    devolucao, rodados, registro.getDestino(), responsavel);
            quantos++;
            totalKm += registro.getKmRodados();
        }
        quantidade.setText(quantos == 0 ? "Nenhum registro encontrado com esses filtros."
                : quantos + (quantos == 1 ? " registro no período." : " registros no período."));
        totalRodado.setText("Total rodado no período: " + Formatos.km(totalKm) + " km");
    }

    private LocalDate lerData(CampoForm campo, JTextField texto) {
        String valor = texto.getText().trim();
        try {
            return LocalDate.parse(valor, Formatos.DATA);
        } catch (DateTimeParseException erro) {
            campo.setErro(valor.matches("\\d{2}/\\d{2}/\\d{4}") ? "Essa data não existe." : "Use dd/mm/aaaa.");
            texto.requestFocusInWindow();
            return null;
        }
    }

    /** Esc volta os filtros para o padrão. */
    @Override
    public void cancelar() {
        limparFiltros();
    }
}
