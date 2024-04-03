package br.com.reverson.picpaydesafiobackend.wallet;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("WALLETS")
public record Wallet(
        @Id
        Long id,
        String fullName,
        String cpf,

        String email,
        String password,
        int type,
        BigDecimal balance,
        @Version Long version

) {
    public Wallet debit(BigDecimal value) {
        return new Wallet(id, fullName, cpf, email, password, type, balance.subtract(value), version);
    }

    public Wallet credit(BigDecimal value) {
        return new Wallet(id, fullName, cpf, email, password, type, balance.add(value), version);
    }
}
