package co.edu.unbosque.dto;

public class PagoResponse {

    private String paymentIntentId;
    private String clientSecret;
    private Long amount;
    private String currency;
    private String status;
    private Long idEntrada;

    public PagoResponse() {}

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Long idEntrada) { this.idEntrada = idEntrada; }
}
