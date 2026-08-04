package com.camila.creditai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "A senha deve ter no mínimo 8 caracteres, conter pelo menos uma letra maiúscula, um número e um caractere especial")
    private String password;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;
}