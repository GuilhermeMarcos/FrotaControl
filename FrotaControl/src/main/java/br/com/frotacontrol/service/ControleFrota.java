package br.com.frotacontrol.service;

import br.com.frotacontrol.dao.MotoristaDAO;
import br.com.frotacontrol.dao.RegistroUsoDAO;
import br.com.frotacontrol.dao.UsuarioDAO;
import br.com.frotacontrol.dao.VeiculoDAO;
import br.com.frotacontrol.model.Motorista;
import br.com.frotacontrol.model.RegistroUso;
import br.com.frotacontrol.model.StatusVeiculo;
import br.com.frotacontrol.model.Usuario;
import br.com.frotacontrol.model.Veiculo;
import java.time.LocalDate;
import java.util.List;

/**
 * Concentra as regras de negócio do sistema de controle de frota.
 *
 * As telas conversam apenas com esta classe. Ela valida as regras e usa as
 * classes DAO para ler e gravar no banco de dados MySQL.
 */
public class ControleFrota {

    public static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();
    private final MotoristaDAO motoristaDAO = new MotoristaDAO();
    private final RegistroUsoDAO registroDAO = new RegistroUsoDAO();

    // ------------------------------------------------------------------
    // Usuários
    // ------------------------------------------------------------------

    /**
     * Inclui um usuário novo (id igual a zero) ou altera um existente.
     *
     * @param novaSenha senha digitada; null mantém a senha atual de um usuário existente
     */
    public Usuario salvarUsuario(Usuario dados, String novaSenha) throws RegraNegocioException {
        if (estaVazio(dados.getNome()) || estaVazio(dados.getLogin())) {
            throw new RegraNegocioException("Nome e usuário são obrigatórios.");
        }
        boolean novo = dados.getId() == 0;
        if ((novo || novaSenha != null) && (novaSenha == null || novaSenha.length() < TAMANHO_MINIMO_SENHA)) {
            throw new RegraNegocioException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (dados.getPerfil() == null) {
            throw new RegraNegocioException("Escolha o perfil do usuário.");
        }
        Usuario mesmoLogin = usuarioDAO.buscarPorLogin(dados.getLogin());
        if (mesmoLogin != null && mesmoLogin.getId() != dados.getId()) {
            throw new RegraNegocioException("Já existe um usuário com o login \"" + dados.getLogin() + "\".");
        }

        if (novo) {
            dados.setSenha(Senhas.gerarHash(novaSenha));
            usuarioDAO.inserir(dados);
            return dados;
        }

        Usuario atual = usuarioDAO.buscarPorId(dados.getId());
        boolean deixaDeSerAdministradorAtivo = atual.isAdministrador() && atual.isAtivo()
                && (!dados.isAdministrador() || !dados.isAtivo());
        if (deixaDeSerAdministradorAtivo && usuarioDAO.contarAdministradoresAtivos() == 1) {
            throw new RegraNegocioException("O sistema precisa ter pelo menos um administrador ativo.");
        }
        atual.setNome(dados.getNome());
        atual.setLogin(dados.getLogin());
        atual.setPerfil(dados.getPerfil());
        atual.setAtivo(dados.isAtivo());
        if (novaSenha != null) {
            atual.setSenha(Senhas.gerarHash(novaSenha));
        }
        usuarioDAO.atualizar(atual);
        return atual;
    }

    /**
     * Confere o login e a senha. A comparação é feita entre o hash gravado
     * no banco e o hash da senha digitada.
     *
     * @return o usuário autenticado ou null quando os dados não conferem
     */
    public Usuario autenticar(String login, String senha) {
        Usuario usuario = usuarioDAO.buscarPorLogin(login);
        if (usuario != null && usuario.isAtivo() && usuario.getSenha().equals(Senhas.gerarHash(senha))) {
            return usuario;
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listar();
    }

    // ------------------------------------------------------------------
    // Veículos
    // ------------------------------------------------------------------

    /**
     * Inclui um veículo novo (id igual a zero) ou altera um existente.
     */
    public Veiculo salvarVeiculo(Veiculo dados) throws RegraNegocioException {
        String placa = normalizarPlaca(dados.getPlaca());

        // Aceita o padrão antigo (ABC1234) e o padrão Mercosul (ABC1D23)
        if (!placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new RegraNegocioException("Placa inválida. Use o formato ABC1234 ou ABC1D23.");
        }
        Veiculo mesmaPlaca = veiculoDAO.buscarPorPlaca(placa);
        if (mesmaPlaca != null && mesmaPlaca.getId() != dados.getId()) {
            throw new RegraNegocioException("Já existe um veículo cadastrado com a placa " + placa + ".");
        }
        if (estaVazio(dados.getMarca()) || estaVazio(dados.getModelo())) {
            throw new RegraNegocioException("Marca e modelo são obrigatórios.");
        }
        int anoMaximo = LocalDate.now().getYear() + 1;
        if (dados.getAno() < 1980 || dados.getAno() > anoMaximo) {
            throw new RegraNegocioException("Ano inválido. Informe um ano entre 1980 e " + anoMaximo + ".");
        }
        if (dados.getQuilometragem() < 0) {
            throw new RegraNegocioException("A quilometragem não pode ser negativa.");
        }

        if (dados.getId() == 0) {
            dados.setPlaca(placa);
            veiculoDAO.inserir(dados);
            return dados;
        }

        // Na alteração, a situação e a indicação de ativo são mantidas como estão no banco
        Veiculo atual = veiculoDAO.buscarPorId(dados.getId());
        atual.setPlaca(placa);
        atual.setMarca(dados.getMarca());
        atual.setModelo(dados.getModelo());
        atual.setAno(dados.getAno());
        atual.setQuilometragem(dados.getQuilometragem());
        veiculoDAO.atualizar(atual);
        return atual;
    }

    public Veiculo buscarVeiculoPorPlaca(String placa) {
        return veiculoDAO.buscarPorPlaca(normalizarPlaca(placa));
    }

    public List<Veiculo> listarVeiculos() {
        return veiculoDAO.listar();
    }

    public List<Veiculo> listarVeiculosAtivos() {
        return veiculoDAO.listarAtivos();
    }

    public List<Veiculo> listarVeiculosDisponiveis() {
        return veiculoDAO.listarDisponiveis();
    }

    public void enviarParaManutencao(Veiculo veiculo) throws RegraNegocioException {
        Veiculo atual = veiculoDAO.buscarPorId(veiculo.getId());
        if (atual.getStatus() == StatusVeiculo.EM_USO) {
            throw new RegraNegocioException("O veículo está em uso. Registre a devolução antes de enviá-lo para manutenção.");
        }
        if (atual.getStatus() == StatusVeiculo.EM_MANUTENCAO) {
            throw new RegraNegocioException("O veículo já está em manutenção.");
        }
        atual.setStatus(StatusVeiculo.EM_MANUTENCAO);
        atual.setInicioManutencao(LocalDate.now());
        veiculoDAO.atualizar(atual);
    }

    public void concluirManutencao(Veiculo veiculo) throws RegraNegocioException {
        Veiculo atual = veiculoDAO.buscarPorId(veiculo.getId());
        if (atual.getStatus() != StatusVeiculo.EM_MANUTENCAO) {
            throw new RegraNegocioException("O veículo não está em manutenção.");
        }
        atual.setStatus(StatusVeiculo.DISPONIVEL);
        atual.setInicioManutencao(null);
        veiculoDAO.atualizar(atual);
    }

    /**
     * Veículos não são excluídos, para preservar o histórico: são inativados.
     */
    public void inativarVeiculo(Veiculo veiculo) throws RegraNegocioException {
        Veiculo atual = veiculoDAO.buscarPorId(veiculo.getId());
        if (atual.getStatus() == StatusVeiculo.EM_USO) {
            throw new RegraNegocioException("O veículo está em uso. Registre a devolução antes de inativá-lo.");
        }
        atual.setAtivo(false);
        veiculoDAO.atualizar(atual);
    }

    public void reativarVeiculo(Veiculo veiculo) {
        Veiculo atual = veiculoDAO.buscarPorId(veiculo.getId());
        atual.setAtivo(true);
        veiculoDAO.atualizar(atual);
    }

    // ------------------------------------------------------------------
    // Motoristas
    // ------------------------------------------------------------------

    /**
     * Inclui um motorista novo (id igual a zero) ou altera um existente.
     */
    public Motorista salvarMotorista(Motorista dados) throws RegraNegocioException {
        if (estaVazio(dados.getNome())) {
            throw new RegraNegocioException("O nome do motorista é obrigatório.");
        }
        String cpf = apenasDigitos(dados.getCpf());
        if (cpf.length() != 11) {
            throw new RegraNegocioException("CPF inválido. Informe os 11 dígitos.");
        }
        Motorista mesmoCpf = motoristaDAO.buscarPorCpf(cpf);
        if (mesmoCpf != null && mesmoCpf.getId() != dados.getId()) {
            throw new RegraNegocioException("Já existe um motorista cadastrado com este CPF.");
        }
        if (estaVazio(dados.getCnh())) {
            throw new RegraNegocioException("O número da CNH é obrigatório.");
        }
        if (dados.getValidadeCnh() == null) {
            throw new RegraNegocioException("A validade da CNH é obrigatória.");
        }
        dados.setCpf(cpf);

        if (dados.getId() == 0) {
            motoristaDAO.inserir(dados);
            return dados;
        }
        if (!dados.isAtivo() && registroDAO.buscarAbertoPorMotorista(dados.getId()) != null) {
            throw new RegraNegocioException("O motorista está com um veículo. Registre a devolução antes de inativá-lo.");
        }
        motoristaDAO.atualizar(dados);
        return dados;
    }

    public List<Motorista> listarMotoristas() {
        return motoristaDAO.listar();
    }

    /**
     * Motoristas que podem retirar um veículo agora: ativos, com a CNH
     * válida e sem outro veículo em seu nome.
     */
    public List<Motorista> listarMotoristasAptos() {
        return motoristaDAO.listarAptos();
    }

    // ------------------------------------------------------------------
    // Saída e devolução de veículos
    // ------------------------------------------------------------------

    /**
     * Registra a saída de um veículo, validando as regras de negócio com os
     * dados atuais do banco: o veículo precisa estar disponível, o motorista
     * precisa estar ativo e com a CNH válida, e não pode ter outro veículo.
     */
    public RegistroUso registrarSaida(Veiculo veiculo, Motorista motorista, Usuario responsavel, String destino)
            throws RegraNegocioException {

        Veiculo veiculoAtual = veiculoDAO.buscarPorId(veiculo.getId());
        if (veiculoAtual == null || !veiculoAtual.isDisponivel()) {
            throw new RegraNegocioException("O veículo " + veiculo.getPlaca() + " não está disponível.");
        }
        Motorista motoristaAtual = motoristaDAO.buscarPorId(motorista.getId());
        if (motoristaAtual == null || !motoristaAtual.isAtivo()) {
            throw new RegraNegocioException("O motorista " + motorista.getNome() + " está inativo.");
        }
        if (!motoristaAtual.isCnhValida()) {
            throw new RegraNegocioException("A CNH de " + motoristaAtual.getNome() + " está vencida.");
        }
        RegistroUso emAberto = registroDAO.buscarAbertoPorMotorista(motoristaAtual.getId());
        if (emAberto != null) {
            throw new RegraNegocioException(motoristaAtual.getNome() + " já está com o veículo "
                    + emAberto.getVeiculo().getPlaca() + ".");
        }
        if (estaVazio(destino)) {
            throw new RegraNegocioException("Informe o destino ou a finalidade da saída.");
        }

        RegistroUso registro = new RegistroUso(veiculoAtual, motoristaAtual, responsavel, destino.trim());
        if (!registroDAO.registrarSaida(registro)) {
            throw new RegraNegocioException("O veículo " + veiculo.getPlaca() + " não está mais disponível.");
        }
        veiculoAtual.setStatus(StatusVeiculo.EM_USO);
        return registro;
    }

    /**
     * Registra a devolução: fecha o registro, atualiza a quilometragem
     * do veículo e o deixa disponível novamente.
     */
    public void registrarDevolucao(RegistroUso registro, int kmFinal, String observacao)
            throws RegraNegocioException {

        if (!registro.isEmAberto()) {
            throw new RegraNegocioException("Este registro já foi finalizado.");
        }
        if (kmFinal < registro.getKmSaida()) {
            throw new RegraNegocioException("O km de devolução não pode ser menor que o km de saída ("
                    + registro.getKmSaida() + ").");
        }

        registro.registrarDevolucao(kmFinal, estaVazio(observacao) ? null : observacao.trim());
        if (!registroDAO.registrarDevolucao(registro)) {
            throw new RegraNegocioException("Este registro já foi finalizado.");
        }
        registro.getVeiculo().setQuilometragem(kmFinal);
        registro.getVeiculo().setStatus(StatusVeiculo.DISPONIVEL);
    }

    public RegistroUso buscarRegistroAbertoDoVeiculo(Veiculo veiculo) {
        return registroDAO.buscarAbertoPorVeiculo(veiculo.getId());
    }

    public List<RegistroUso> listarRegistrosEmAberto() {
        return registroDAO.listarEmAberto();
    }

    /**
     * Histórico de utilização no período, do mais recente para o mais antigo.
     *
     * @param veiculo   veículo desejado ou null para todos
     * @param motorista motorista desejado ou null para todos
     */
    public List<RegistroUso> consultarHistorico(LocalDate inicio, LocalDate fim, Veiculo veiculo, Motorista motorista) {
        return registroDAO.consultar(inicio, fim,
                veiculo == null ? null : veiculo.getId(),
                motorista == null ? null : motorista.getId());
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares
    // ------------------------------------------------------------------

    private boolean estaVazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private String apenasDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("[^0-9]", "");
    }

    private String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
