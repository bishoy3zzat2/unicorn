package com.loyalixa.backend.staff.dto;
import jakarta.validation.constraints.Size;
public record FinancialAccountUpdateRequest(
    @Size(max = 100) String bankName,
    @Size(max = 50) String bankAccountNumber,
    @Size(max = 50) String bankIban,
    @Size(max = 20) String bankSwiftCode,
    @Size(max = 20) String walletType,
    @Size(max = 20) String walletNumber,
    @Size(max = 30) String cardType,
    @Size(max = 50) String cardNumber,
    @Size(max = 100) String cardHolderName,
    @Size(max = 100) String cardCountry,
    @Size(max = 100) String cardBankName,
    @Size(max = 10) String cardExpiryDate,
    String notes
) {}
