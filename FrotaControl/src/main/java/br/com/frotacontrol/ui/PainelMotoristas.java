package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.Motorista;
import br.com.frotacontrol.service.RegraNegocioException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Cadastro de motoristas (RF04). A situação (Apto, CNH vencida ou Inativo)
 * é calculada pelo sistema a partir da validade da CNH.
 */
public class PainelMotoristas extends TelaCadastro {

    private final JTextField busca = Tema.campo();
    private final JComboBox<String> filtroSituacao = Tema.combo();

    private final JTextField nome = Tema.campo();
    private final JTextField cpf = Tema.campo();
    private final JTextField cnh = Tema.campo();
    private final JTextField validade = Tema.campo();
    private final JTextField telefone = Tema.campo();
    private final JCheckBox ativo = Tema.caixa("Motorista ativo");
    private final CampoForm campoNome = new CampoForm("Nome completo", nome, true, null);
    private final CampoForm campoCpf = new CampoForm("CPF", cpf, true, null);
    private final CampoForm campoCnh = new CampoForm("Número da CNH", cnh, true, null);
    private final CampoForm campoValidade = new CampoForm("Validade da CNH", validade, true, "Formato dd/mm/aaaa");
    private final CampoForm campoTelefone = new CampoForm("Telefone", telefone);

    private final List<Motorista> linhas = new ArrayList<>();
    private Motorista selecionado;

    public PainelMotoristas(JanelaPrincipal janela) {
        super(janela, "Motoristas", "Pessoas autorizadas a retirar veículos", "Novo motorista",
                "Nome", "CPF", "Validade da CNH", "Situação");
        tabela.larguras(190, 140, 140, 160).colunaSituacao(3);

        for (String opcao : new String[]{"Todos", "Apto", "CNH vencida", "Inativo"}) {
            filtroSituacao.addItem(opcao);
        }
        CampoForm campoSituacao = new CampoForm("Situação", filtroSituacao);
        campoSituacao.setPreferredSize(new Dimension(228, campoSituacao.getPreferredSize().height));
        campoSituacao.setMaximumSize(campoSituacao.getPreferredSize());
        filtros.add(new CampoForm("Buscar por nome, CPF ou CNH", busca));
        filtros.add(Box.createHorizontalStrut(16));
        filtros.add(campoSituacao);
        aoDigitar(busca, () -> recarregar(false));
        filtroSituacao.addActionListener(evento -> recarregar(false));

        adicionar(campoNome, 10);
        adicionar(campoCpf, 10);
        adicionar(campoCnh, 10);
        adicionar(campoValidade, 10);
        adicionar(campoTelefone, 12);

        JPanel linhaAtivo = new JPanel(new BorderLayout());
        linhaAtivo.setOpaque(false);
        linhaAtivo.add(ativo, BorderLayout.WEST);
        linhaAtivo.add(Tema.texto("* Campo obrigatório", Tema.LEGENDA, Tema.TEXTO_APOIO), BorderLayout.EAST);
        linhaAtivo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        adicionar(linhaAtivo, 14);
        adicionar(botoesSalvarCancelar(), 0);
    }

    @Override
    public void atualizar() {
        recarregar(true);
    }

    private void recarregar(boolean recarregarFormulario) {
        int idAntes = selecionado == null ? 0 : selecionado.getId();
        carregando = true;
        String termo = busca.getText().trim().toLowerCase();
        String situacaoEscolhida = (String) filtroSituacao.getSelectedItem();
        linhas.clear();
        tabela.limpar();
        int linhaSelecionada = -1;
        for (Motorista motorista : controle.listarMotoristas()) {
            Tabela.Situacao situacao = Tabela.Situacao.de(motorista);
            String texto = (motorista.getNome() + " " + motorista.getCpf() + " " + Formatos.cpf(motorista.getCpf())
                    + " " + motorista.getCnh()).toLowerCase();
            boolean passaNaBusca = termo.isEmpty() || texto.contains(termo);
            boolean passaNoFiltro = "Todos".equals(situacaoEscolhida) || situacao.toString().equals(situacaoEscolhida);
            if (passaNaBusca && passaNoFiltro) {
                if (selecionado != null && motorista.getId() == selecionado.getId()) {
                    linhaSelecionada = linhas.size();
                    selecionado = motorista;      // usa os dados recém-lidos do banco
                }
                linhas.add(motorista);
                tabela.linha(motorista.getNome(), Formatos.cpf(motorista.getCpf()),
                        motorista.getValidadeCnh().format(Formatos.DATA), situacao);
            }
        }
        tabela.selecionar(linhaSelecionada);
        if (linhaSelecionada < 0) {
            selecionado = null;
        }
        carregando = false;
        contagem.setText(linhas.size() + (linhas.size() == 1 ? " motorista" : " motoristas"));
        int idDepois = selecionado == null ? 0 : selecionado.getId();
        if (recarregarFormulario || idAntes != idDepois) {
            preencherFormulario();
        }
    }

    @Override
    protected void aoSelecionar(int linha) {
        selecionado = linha >= 0 ? linhas.get(linha) : null;
        preencherFormulario();
    }

    private void preencherFormulario() {
        limparErros();
        if (selecionado == null) {
            tituloFormulario.setText("Novo motorista");
            nome.setText("");
            cpf.setText("");
            cnh.setText("");
            validade.setText("");
            telefone.setText("");
            ativo.setSelected(true);
            return;
        }
        tituloFormulario.setText("Dados do motorista");
        nome.setText(selecionado.getNome());
        cpf.setText(Formatos.cpf(selecionado.getCpf()));
        cnh.setText(selecionado.getCnh());
        validade.setText(selecionado.getValidadeCnh().format(Formatos.DATA));
        telefone.setText(selecionado.getTelefone());
        ativo.setSelected(selecionado.isAtivo());
    }

    private void limparErros() {
        campoNome.limparErro();
        campoCpf.limparErro();
        campoCnh.limparErro();
        campoValidade.limparErro();
        campoTelefone.limparErro();
    }

    @Override
    public void novo() {
        selecionado = null;
        carregando = true;
        tabela.clearSelection();
        carregando = false;
        preencherFormulario();
        nome.requestFocusInWindow();
    }

    @Override
    public void salvar() {
        limparErros();
        boolean valido = preenchido(campoNome, nome, "Informe o nome do motorista.");
        if (cpf.getText().replaceAll("[^0-9]", "").length() != 11) {
            campoCpf.setErro("Informe os 11 números do CPF.");
            valido = false;
        }
        valido &= preenchido(campoCnh, cnh, "Informe o número da CNH.");

        LocalDate dataValidade = null;
        String textoValidade = validade.getText().trim();
        if (textoValidade.isEmpty()) {
            campoValidade.setErro("Informe a validade da CNH.");
            valido = false;
        } else {
            try {
                dataValidade = LocalDate.parse(textoValidade, Formatos.DATA);
            } catch (DateTimeParseException erro) {
                // Diferencia o formato errado de uma data que não existe (31/02, por exemplo)
                campoValidade.setErro(textoValidade.matches("\\d{2}/\\d{2}/\\d{4}")
                        ? "Essa data não existe. Confira o dia e o mês."
                        : "Use o formato dd/mm/aaaa.");
                valido = false;
            }
        }
        if (!valido) {
            return;
        }

        Motorista dados = new Motorista(nome.getText().trim(), cpf.getText(), cnh.getText().trim(),
                dataValidade, telefone.getText().trim());
        dados.setAtivo(ativo.isSelected());
        if (selecionado != null) {
            dados.setId(selecionado.getId());
        }
        try {
            selecionado = controle.salvarMotorista(dados);
            recarregar(true);
            janela.informar("Motorista " + selecionado.getNome() + " salvo."
                    + (selecionado.isCnhValida() ? "" : " Atenção: a CNH está vencida."));
        } catch (RegraNegocioException erro) {
            if (erro.getMessage().contains("CPF")) {
                campoCpf.setErro(erro.getMessage());
                cpf.requestFocusInWindow();
            } else {
                Dialogos.erro(this, "Não foi possível salvar", erro.getMessage(), null);
            }
        }
    }

    @Override
    public void cancelar() {
        preencherFormulario();
    }
}
