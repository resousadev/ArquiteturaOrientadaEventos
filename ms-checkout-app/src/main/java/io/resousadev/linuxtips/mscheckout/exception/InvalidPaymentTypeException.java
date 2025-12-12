package io.resousadev.linuxtips.mscheckout.exception;

/**
 * Exceção lançada quando um tipo de pagamento inválido é fornecido.
 */
public class InvalidPaymentTypeException extends RuntimeException {

    /**
     * Cria exceção com mensagem.
     *
     * @param message mensagem de erro
     */
    public InvalidPaymentTypeException(final String message) {
        super(message);
    }

}
