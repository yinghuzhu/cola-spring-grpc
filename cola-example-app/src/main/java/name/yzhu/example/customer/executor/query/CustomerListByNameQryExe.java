package name.yzhu.example.customer.executor.query;

import com.alibaba.cola.dto.MultiResponse;
import name.yzhu.example.api.CustomerServiceI;
import name.yzhu.example.dto.CustomerListByNameQry;
import name.yzhu.example.dto.data.CustomerDTO;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CustomerListByNameQryExe{
    public MultiResponse<CustomerDTO> execute(CustomerListByNameQry cmd) {
        List<CustomerDTO> customerDTOList = new ArrayList<>();
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setCustomerName("Frank");
        customerDTOList.add(customerDTO);
        return MultiResponse.of(customerDTOList);
    }
}
