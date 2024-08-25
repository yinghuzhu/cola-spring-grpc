package name.yzhu.example.customer;

import name.yzhu.example.database.mapper.auto.UserMapper;
import name.yzhu.example.database.model.User;
import name.yzhu.example.domain.customer.Customer;
import name.yzhu.example.domain.customer.gateway.CustomerGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerGatewayImpl implements CustomerGateway {
//    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private UserMapper userMapper;

    public Customer getByById(String customerId){
      CustomerDO customerDO = customerMapper.getById(customerId);
        User user = userMapper.selectByPrimaryKey(Long.valueOf(customerId));
        Customer customer = new Customer();
        customer.setCustomerId(String.valueOf(user.getId()));
      return null;
    }
}
