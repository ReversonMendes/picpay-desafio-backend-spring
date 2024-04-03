package br.com.reverson.picpaydesafiobackend.transaction;

import br.com.reverson.picpaydesafiobackend.authorization.AuthorizerService;
import br.com.reverson.picpaydesafiobackend.notification.NotificationService;
import br.com.reverson.picpaydesafiobackend.wallet.Wallet;
import br.com.reverson.picpaydesafiobackend.wallet.WalletRepository;
import br.com.reverson.picpaydesafiobackend.wallet.WalletType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizerService authorizeService;
    private final NotificationService notificationService;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository,
            AuthorizerService authorizeService,
            NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizeService = authorizeService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        //1 - validar
        validate(transaction);

        //2 - Criar a transação
        var newTransaction = transactionRepository.save(transaction);

        //3 - Opera Transação
        var walletPayer = walletRepository.findById(transaction.payer()).get();
        var walletPayee = walletRepository.findById(transaction.payee()).get();
        //debitar da carteira
        walletRepository.save(walletPayer.debit(transaction.value()));
        //Creditar no Lojista
        walletRepository.save(walletPayee.credit(transaction.value()));

        //4- Chamar serviços externos
        //authorize transaction
        //Deu problema de certificado
        //authorizeService.authorize(transaction);

        //notificação



        return newTransaction;
    }

    /*
     *  - the payer has a common wallet
     *  - the  payer has enough balance
     *  - the payer is not the payee
     * */
    private void validate(Transaction transaction) {

        walletRepository.findById(transaction.payee())
                .map(payee -> walletRepository.findById(transaction.payer())
                        .map(payer -> isTransactionValid(transaction, payer) ? transaction : null)
                        .orElseThrow(() -> new InvalidTransactionException("Invalid transaction - %s".formatted(transaction))))
                .orElseThrow(() -> new InvalidTransactionException("Invalid transaction - %s".formatted(transaction)));
    }

    private boolean isTransactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());
    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}
