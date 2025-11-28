package io.resousadev.linuxtips.mscheckout.model;

/**
 * Record representando um pagamento no sistema de checkout.
 *
 * @param origem origem do pagamento
 * @param valor valor do pagamento
 * @param status status atual do pagamento
 */
public record Payment(String origem, String valor, String status) {

}
