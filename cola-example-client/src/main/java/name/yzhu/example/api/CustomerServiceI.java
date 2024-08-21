package name.yzhu.example.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import name.yzhu.example.dto.CustomerAddCmd;
import name.yzhu.example.dto.CustomerListByNameQry;
import name.yzhu.example.dto.data.CustomerDTO;

public interface CustomerServiceI {

    Response addCustomer(CustomerAddCmd customerAddCmd);

    MultiResponse<CustomerDTO> listByName(CustomerListByNameQry customerListByNameQry);

    Response getCustomer(CustomerAddCmd customerAddCmd);
}
