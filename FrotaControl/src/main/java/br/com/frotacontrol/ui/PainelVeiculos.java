package br.com.frotacontrol.ui;

import br.com.frotacontrol.model.StatusVeiculo;
import br.com.frotacontrol.model.Veiculo;
import br.com.frotacontrol.service.RegraNegocioException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Cadastro de veículos (RF03) e controle de manutenção (RF08).
 */
public class PainelVeiculos extends TelaCadastro {

    private final JTextField busca = Tema.campo();
    private final JComboBox<String> filtroSituacao = Tema.combo();

    private final JTextField placa = Tema.campo();
    private final JTextField marca = Tema.campo();
    private final JTextField modelo = Tema.campo();
    private final JTextField ano = Tema.campo();
    private final JTextField km = Tema.campo();
    private final CampoForm campoPlaca = new CampoForm("Placa", placa, true, "Formato ABC1234 ou ABC1D23");
    private final CampoForm campoMarca = new CampoForm("Marca", marca, true, null);
    private final CampoForm campoModelo = new CampoForm("Modelo", modelo, true, null);
    private final CampoForm campoAno = new CampoForm("Ano", ano, true, null);
    private final CampoForm campoKm = new CampoForm("Quilometragem atual", km, true, null);
    private final Selo selo = new Selo("Disponível", Selo.Tipo.OK);
    private final Botao botaoManutencao = new Botao("Enviar para manutenção", Botao.Tipo.SECUNDARIO);
    private final Botao botaoInativar = new Botao("Inativar veículo", Botao.Tipo.PERIGO);

    private final List<Veiculo> linhas = new ArrayList<>();
    private Veiculo selecionado;

    public PainelVeiculos(JanelaPrincipal janela) {
        super(janela, "Veículos", "Cadastro e situação dos veículos da frota", "Novo veículo",
                "Placa", "Marca", "Modelo", "Ano", "Km", "Situação");
        tabela.larguras(108, 120, 110, 70, 90, 160).colunaPlaca(0).colunaNumero(4).colunaSituacao(5);

        for (String opcao : new String[]{"Todas", "Disponível", "Em uso", "Em manutenção", "Inativo"}) {
            filtroSituacao.addItem(opcao);
        }
        CampoForm campoSituacao = new CampoForm("Situação", filtroSituacao);
        campoSituacao.setPreferredSize(new Dimension(228, campoSituacao.getPreferredSize().height));
        campoSituacao.setMaximumSize(campoSituacao.getPreferredSize());
        filtros.add(new CampoForm("Buscar por placa, marca ou modelo", busca));
        filtros.add(Box.createHorizontalStrut(16));
        filtros.add(campoSituacao);
        aoDigitar(busca, () -> recarregar(false));
        filtroSituacao.addActionListener(evento -> recarregar(false));

        adicionar(campoPlaca, 10);
        adicionar(campoMarca, 10);
        adicionar(campoModelo, 10);
        adicionar(ladoALado(campoAno, campoKm), 12);

        JPanel situacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        situacao.setOpaque(false);
        situacao.add(Tema.texto("Situação   ", Tema.ROTULO, Tema.TEXTO));
        situacao.add(selo);
        JPanel linhaSituacao = new JPanel(new BorderLayout());
        linhaSituacao.setOpaque(false);
        linhaSituacao.add(situacao, BorderLayout.WEST);
        linhaSituacao.add(Tema.texto("* Campo obrigatório", Tema.LEGENDA, Tema.TEXTO_APOIO), BorderLayout.EAST);
        linhaSituacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        adicionar(linhaSituacao, 14);

        adicionar(botoesSalvarCancelar(), 12);
        botaoManutencao.addActionListener(evento -> alternarManutencao());
        adicionar(larguraTotal(botaoManutencao), 12);
        botaoInativar.addActionListener(evento -> alternarAtivo());
        adicionar(larguraTotal(botaoInativar), 0);
    }

    @Override
    public void atualizar() {
        recarregar(true);
    }

    /** Recarrega a lista aplicando a busca e o filtro, mantendo a seleção quando possível. */
    private void recarregar(boolean recarregarFormulario) {
        int idAntes = selecionado == null ? 0 : selecionado.getId();
        carregando = true;
        String termo = busca.getText().trim().toLowerCase();
        String situacaoEscolhida = (String) filtroSituacao.getSelectedItem();
        linhas.clear();
        tabela.limpar();
        int linhaSelecionada = -1;
        for (Veiculo veiculo : controle.listarVeiculos()) {
            Tabela.Situacao situacao = Tabela.Situacao.de(veiculo);
            String texto = (veiculo.getPlaca() + " " + veiculo.getMarca() + " " + veiculo.getModelo()).toLowerCase();
            boolean passaNaBusca = termo.isEmpty() || texto.contains(termo);
            boolean passaNoFiltro = "Todas".equals(situacaoEscolhida) || situacao.toString().equals(situacaoEscolhida);
            if (passaNaBusca && passaNoFiltro) {
                if (selecionado != null && veiculo.getId() == selecionado.getId()) {
                    linhaSelecionada = linhas.size();
                    selecionado = veiculo;      // usa os dados recém-lidos do banco
                }
                linhas.add(veiculo);
                tabela.linha(veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(),
                        String.valueOf(veiculo.getAno()), Formatos.km(veiculo.getQuilometragem()), situacao);
            }
        }
        tabela.selecionar(linhaSelecionada);
        if (linhaSelecionada < 0) {
            selecionado = null;
        }
        carregando = false;
        contagem.setText(linhas.size() + (linhas.size() == 1 ? " veículo" : " veículos"));
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
            tituloFormulario.setText("Novo veículo");
            placa.setText("");
            marca.setText("");
            modelo.setText("");
            ano.setText("");
            km.setText("");
            selo.definir("Disponível", Selo.Tipo.OK);
            botaoManutencao.setText("Enviar para manutenção");
            botaoManutencao.setEnabled(false);
            botaoInativar.setText("Inativar veículo");
            botaoInativar.setTipo(Botao.Tipo.PERIGO);
            botaoInativar.setEnabled(false);
            return;
        }
        tituloFormulario.setText("Dados do veículo");
        placa.setText(selecionado.getPlaca());
        marca.setText(selecionado.getMarca());
        modelo.setText(selecionado.getModelo());
        ano.setText(String.valueOf(selecionado.getAno()));
        km.setText(Formatos.km(selecionado.getQuilometragem()));
        Tabela.Situacao situacao = Tabela.Situacao.de(selecionado);
        selo.definir(situacao.toString(), situacao.getTipo());

        boolean emUso = selecionado.getStatus() == StatusVeiculo.EM_USO;
        boolean emManutencao = selecionado.getStatus() == StatusVeiculo.EM_MANUTENCAO;
        botaoManutencao.setText(emManutencao ? "Concluir manutenção" : "Enviar para manutenção");
        botaoManutencao.setEnabled(selecionado.isAtivo() && !emUso);
        botaoInativar.setText(selecionado.isAtivo() ? "Inativar veículo" : "Reativar veículo");
        botaoInativar.setTipo(selecionado.isAtivo() ? Botao.Tipo.PERIGO : Botao.Tipo.SECUNDARIO);
        botaoInativar.setEnabled(!emUso);
    }

    private void limparErros() {
        campoPlaca.limparErro();
        campoMarca.limparErro();
        campoModelo.limparErro();
        campoAno.limparErro();
        campoKm.limparErro();
    }

    @Override
    public void novo() {
        selecionado = null;
        carregando = true;
        tabela.clearSelection();
        carregando = false;
        preencherFormulario();
        placa.requestFocusInWindow();
    }

    @Override
    public void salvar() {
        limparErros();
        boolean valido = preenchido(campoPlaca, placa, "Informe a placa.");
        valido &= preenchido(campoMarca, marca, "Informe a marca.");
        valido &= preenchido(campoModelo, modelo, "Informe o modelo.");
        int anoInformado = 0;
        int kmInformado = 0;
        try {
            anoInformado = Formatos.inteiro(ano.getText());
        } catch (NumberFormatException erro) {
            campoAno.setErro("Digite o ano com 4 números.");
            valido = false;
        }
        try {
            kmInformado = Formatos.inteiro(km.getText());
        } catch (NumberFormatException erro) {
            campoKm.setErro("Digite apenas números.");
            valido = false;
        }
        if (!valido) {
            return;
        }

        Veiculo dados = new Veiculo(placa.getText(), marca.getText().trim(), modelo.getText().trim(),
                anoInformado, kmInformado);
        if (selecionado != null) {
            dados.setId(selecionado.getId());
        }
        try {
            selecionado = controle.salvarVeiculo(dados);
            recarregar(true);
            janela.informar("Veículo " + selecionado.getPlaca() + " salvo.");
        } catch (RegraNegocioException erro) {
            // Mostra o erro junto ao campo a que ele se refere
            String mensagem = erro.getMessage();
            if (mensagem.contains("laca")) {
                campoPlaca.setErro(mensagem);
                placa.requestFocusInWindow();
            } else if (mensagem.startsWith("Ano")) {
                campoAno.setErro("Informe um ano entre 1980 e o próximo ano.");
                ano.requestFocusInWindow();
            } else if (mensagem.contains("quilometragem")) {
                campoKm.setErro(mensagem);
                km.requestFocusInWindow();
            } else {
                Dialogos.erro(this, "Não foi possível salvar", mensagem, null);
            }
        }
    }

    @Override
    public void cancelar() {
        preencherFormulario();
    }

    private void alternarManutencao() {
        try {
            if (selecionado.getStatus() == StatusVeiculo.EM_MANUTENCAO) {
                controle.concluirManutencao(selecionado);
                janela.informar("Manutenção concluída: " + selecionado.getPlaca() + " está disponível.");
            } else {
                controle.enviarParaManutencao(selecionado);
                janela.informar("Veículo " + selecionado.getPlaca() + " enviado para manutenção.");
            }
            recarregar(true);
        } catch (RegraNegocioException erro) {
            Dialogos.erro(this, "Operação não realizada", erro.getMessage(), null);
        }
    }

    private void alternarAtivo() {
        if (!selecionado.isAtivo()) {
            controle.reativarVeiculo(selecionado);
            janela.informar("Veículo " + selecionado.getPlaca() + " reativado.");
            recarregar(true);
            return;
        }
        boolean confirmou = Dialogos.confirmar(this, "Inativar veículo",
                "Inativar o veículo " + selecionado.getPlaca() + "?",
                "O " + selecionado.getDescricao() + " deixa de aparecer nas saídas. O histórico dele continua "
                + "guardado e você pode reativá-lo depois nesta tela.", "Inativar veículo");
        if (!confirmou) {
            return;
        }
        try {
            controle.inativarVeiculo(selecionado);
            janela.informar("Veículo " + selecionado.getPlaca() + " inativado.");
            recarregar(true);
        } catch (RegraNegocioException erro) {
            Dialogos.erro(this, "Operação não realizada", erro.getMessage(), null);
        }
    }
}
