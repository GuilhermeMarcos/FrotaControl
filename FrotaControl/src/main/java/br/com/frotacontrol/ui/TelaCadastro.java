package br.com.frotacontrol.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Estrutura comum das telas de cadastro: filtros e lista à esquerda,
 * formulário à direita. Selecionar uma linha carrega os dados no formulário,
 * sem abrir outra janela.
 */
public abstract class TelaCadastro extends Tela {

    protected final JPanel filtros = new JPanel();
    protected final Tabela tabela;
    protected final JLabel contagem = Tema.texto(" ", Tema.APOIO, Tema.TEXTO_APOIO);
    protected final JPanel rodapeLista = new JPanel();
    protected final JLabel tituloFormulario = Tema.texto(" ", Tema.TITULO_PAINEL, Tema.TEXTO);
    protected final JPanel formulario = new JPanel();
    /** Verdadeiro enquanto a lista está sendo recarregada, para ignorar eventos de seleção. */
    protected boolean carregando;

    protected TelaCadastro(JanelaPrincipal janela, String titulo, String subtitulo, String textoNovo,
            String... colunas) {
        super(janela, titulo, subtitulo);

        Botao novo = new Botao(textoNovo + " (Ctrl+N)", Botao.Tipo.PRINCIPAL);
        novo.addActionListener(evento -> novo());
        acoes.add(novo);

        tabela = new Tabela(colunas);
        tabela.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && !carregando) {
                aoSelecionar(tabela.getSelectedRow());
            }
        });

        filtros.setOpaque(false);
        filtros.setLayout(new BoxLayout(filtros, BoxLayout.X_AXIS));

        JPanel lista = new JPanel(new BorderLayout(0, 10));
        lista.setOpaque(false);
        lista.add(filtros, BorderLayout.NORTH);
        lista.add(tabela.comRolagem(), BorderLayout.CENTER);
        rodapeLista.setOpaque(false);
        rodapeLista.setLayout(new BoxLayout(rodapeLista, BoxLayout.Y_AXIS));
        contagem.setAlignmentX(Component.LEFT_ALIGNMENT);
        rodapeLista.add(contagem);
        lista.add(rodapeLista, BorderLayout.SOUTH);

        formulario.setOpaque(false);
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        tituloFormulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulario.add(tituloFormulario);
        formulario.add(Box.createVerticalStrut(14));

        Tema.Painel painel = new Tema.Painel();
        painel.setLayout(new BorderLayout());
        painel.setPreferredSize(new Dimension(360, 10));
        painel.add(formulario, BorderLayout.NORTH);

        conteudo.add(lista, BorderLayout.CENTER);
        conteudo.add(painel, BorderLayout.EAST);
    }

    /** Chamado quando a pessoa escolhe uma linha da lista (-1 quando nada está selecionado). */
    protected abstract void aoSelecionar(int linha);

    /** Acrescenta um componente ao formulário, seguido de um espaço. */
    protected void adicionar(JComponent componente, int espacoAbaixo) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulario.add(componente);
        formulario.add(Box.createVerticalStrut(espacoAbaixo));
    }

    /** Dois componentes lado a lado, com a mesma largura. */
    protected static JPanel ladoALado(JComponent esquerda, JComponent direita) {
        // A altura acompanha o conteúdo, para caber a mensagem de erro quando ela aparece
        JPanel linha = new JPanel(new GridLayout(1, 2, 12, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        linha.setOpaque(false);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.add(esquerda);
        linha.add(direita);
        return linha;
    }

    protected JPanel botoesSalvarCancelar() {
        Botao salvar = new Botao("Salvar (Ctrl+S)", Botao.Tipo.PRINCIPAL);
        salvar.addActionListener(evento -> salvar());
        Botao cancelar = new Botao("Cancelar (Esc)", Botao.Tipo.SECUNDARIO);
        cancelar.addActionListener(evento -> cancelar());
        return ladoALado(salvar, cancelar);
    }

    protected static JPanel larguraTotal(JComponent componente) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);
        linha.add(componente);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
        return linha;
    }

    /** Executa a ação sempre que o texto do campo mudar (usado na busca). */
    protected static void aoDigitar(JTextComponent campo, Runnable acao) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                acao.run();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                acao.run();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                acao.run();
            }
        });
    }

    /** Verifica se um campo obrigatório foi preenchido; se não, mostra o erro junto a ele. */
    protected static boolean preenchido(CampoForm campo, JTextComponent texto, String mensagem) {
        if (texto.getText().trim().isEmpty()) {
            campo.setErro(mensagem);
            return false;
        }
        return true;
    }

    /** Leva o foco para o primeiro campo com erro. */
    protected static void focar(JComponent campo) {
        campo.requestFocusInWindow();
    }
}
