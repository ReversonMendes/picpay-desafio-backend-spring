package br.com.reverson.picpaydesafiobackend.notification;

import br.com.reverson.picpaydesafiobackend.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void notify(Transaction transaction){
        System.out.println("Notificou");
    }
}
