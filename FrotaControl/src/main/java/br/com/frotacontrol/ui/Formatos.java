package br.com.frotacontrol.ui;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Formatação e leitura de datas e números usados nas telas.
 */
public final class Formatos {

    public static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DIA_DA_SEMANA =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy, HH:mm", BRASIL);

    private Formatos() {
    }

    /** 45200 vira "45.200". */
    public static String km(int valor) {
        return NumberFormat.getIntegerInstance(BRASIL).format(valor);
    }

    /** "11122233344" vira "111.222.333-44". */
    public static String cpf(String digitos) {
        if (digitos == null || digitos.length() != 11) {
            return digitos;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }

    /** "hoje, 07:15" para o dia atual; data e hora completas nos demais casos. */
    public static String quando(LocalDateTime momento) {
        if (momento.toLocalDate().equals(LocalDate.now())) {
            return "hoje, " + momento.format(HORA);
        }
        return momento.format(DATA_HORA);
    }

    public static String agoraPorExtenso() {
        return LocalDateTime.now().format(DIA_DA_SEMANA);
    }

    /** Lê um número inteiro aceitando pontos de milhar, como "45.200". */
    public static int inteiro(String texto) {
        return Integer.parseInt(texto.trim().replace(".", ""));
    }
}
