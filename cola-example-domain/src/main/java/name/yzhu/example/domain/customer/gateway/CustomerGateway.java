package name.yzhu.example.domain.customer.gateway;

import name.yzhu.example.domain.customer.Customer;

public interface CustomerGateway {
    Customer getByById(String customerId);
}
