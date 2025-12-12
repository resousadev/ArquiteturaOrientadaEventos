package io.resousadev.linuxtips.mscheckout.strategy.enumeration;

/**
 * Tipos de pagamento suportados pelo sistema de checkout.
 */
public enum PaymentTypeEnum {
    /** Pagamento com cartão de crédito. */
    CREDIT_CARD,
    
    /** Pagamento com cartão de débito. */
    DEBIT_CARD,
    
    /** Pagamento PIX (sistema de pagamento instantâneo brasileiro). */
    PIX,
    
    /** Pagamento via boleto bancário. */
    BOLETO
}
