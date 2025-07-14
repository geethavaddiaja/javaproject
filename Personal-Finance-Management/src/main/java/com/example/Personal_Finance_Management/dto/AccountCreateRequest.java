package com.example.Personal_Finance_Management.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    private String accountNumber;

    private String account_holder_name;

    private String accountType;

    private BigDecimal initialBalance;

}
