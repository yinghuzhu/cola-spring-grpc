package name.yzhu.example.web;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import name.yzhu.example.api.CustomerServiceI;
import name.yzhu.example.database.mapper.auto.UserMapper;
import name.yzhu.example.database.model.User;
import name.yzhu.example.dto.CustomerAddCmd;
import name.yzhu.example.dto.CustomerListByNameQry;
import name.yzhu.example.dto.data.CustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerController {

    @Autowired
    private CustomerServiceI customerService;

    @GetMapping(value = "/helloworld")
    public String helloWorld(){
        return "Hello, welcome to COLA world!";
    }

    @GetMapping(value = "/customer")
    public MultiResponse<CustomerDTO> listCustomerByName(@RequestParam(required = false) String name){
        CustomerListByNameQry customerListByNameQry = new CustomerListByNameQry();
        customerListByNameQry.setName(name);
        return customerService.listByName(customerListByNameQry);
    }

    @PostMapping(value = "/customer")
    public Response addCustomer(@RequestBody CustomerAddCmd customerAddCmd){
        return customerService.addCustomer(customerAddCmd);
    }

    @Autowired
    private UserMapper userMapper;
    @GetMapping(value = "/get")
    public Response getCustomer(@RequestParam(name = "id", required = false) Long id){
        User user = userMapper.selectByPrimaryKey(id);
        return SingleResponse.of(user);
    }
}
