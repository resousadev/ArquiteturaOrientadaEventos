package io.resousadev.linuxtips.mscheckout.dto;

import io.resousadev.linuxtips.mscheckout.strategy.enumeration.PaymentTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request para processamento de pagamento.
 *
 * @param origem origem do pagamento
 * @param valor valor do pagamento (formato decimal com até 2 casas)
 * @param paymentType tipo de pagamento
 */
public record PaymentRequest(
        @NotBlank(message = "Origem não pode ser vazia")
        String origem,

        @NotBlank(message = "Valor não pode ser vazio")
        @Pattern(regexp = "\\d+(\\.\\d{1,2})?", message = "Valor deve ser numérico com até 2 casas decimais")
        String valor,

        @NotNull(message = "Tipo de pagamento é obrigatório")
        PaymentTypeEnum paymentType
) {}
