package br.com.frotacontrol.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gera o hash das senhas. O banco de dados guarda apenas o hash (SHA-256),
 * nunca a senha digitada. No login, o sistema calcula o hash do que foi
 * digitado e compara com o que está gravado.
 */
public final class Senhas {

    private Senhas() {
    }

    /**
     * @return o hash SHA-256 da senha, em 64 caracteres hexadecimais
     *         (o mesmo resultado da função SHA2(senha, 256) do MySQL)
     */
    public static String gerarHash(String senha) {
        try {
            MessageDigest algoritmo = MessageDigest.getInstance("SHA-256");
            byte[] bytes = algoritmo.digest(senha.getBytes(StandardCharsets.UTF_8));
            StringBuilder texto = new StringBuilder();
            for (byte b : bytes) {
                texto.append(String.format("%02x", b));
            }
            return texto.toString();
        } catch (NoSuchAlgorithmException erro) {
            // SHA-256 faz parte de toda instalação do Java
            throw new IllegalStateException("Algoritmo SHA-256 indisponível.", erro);
        }
    }
}
