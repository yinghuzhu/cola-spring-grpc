package name.yzhu.example.domain.customer.gateway;

import name.yzhu.example.domain.customer.Credit;

//Assume that the credit info is in another distributed Service
public interface CreditGateway {
    Credit getCredit(String customerId);
}
