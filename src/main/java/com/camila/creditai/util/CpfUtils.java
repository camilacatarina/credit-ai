package com.camila.creditai.util;

public class CpfUtils {

    private CpfUtils() {}

    /**
     * Mascara o CPF para exibição segura.
     * Exemplo: 12345678901 → 123.***.***-01
     */
    public static String mask(String cpf) {
        if (cpf == null || cpf.length() != 11) return "***.***.***-**";
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    public static boolean isValid(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }
}