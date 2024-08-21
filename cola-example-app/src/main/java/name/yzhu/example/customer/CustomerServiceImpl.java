package name.yzhu.example.customer;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.catchlog.CatchAndLog;
import jakarta.annotation.Resource;
import name.yzhu.example.api.CustomerServiceI;
import name.yzhu.example.dto.CustomerAddCmd;
import name.yzhu.example.dto.CustomerListByNameQry;
import name.yzhu.example.dto.data.CustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import name.yzhu.example.customer.executor.CustomerAddCmdExe;
import name.yzhu.example.customer.executor.query.CustomerListByNameQryExe;


@Service
@CatchAndLog
public class CustomerServiceImpl implements CustomerServiceI {

    @Resource
    private CustomerAddCmdExe customerAddCmdExe;

    @Resource
    private CustomerListByNameQryExe customerListByNameQryExe;

    public Response addCustomer(CustomerAddCmd customerAddCmd) {
        return customerAddCmdExe.execute(customerAddCmd);
    }

    @Override
    public MultiResponse<CustomerDTO> listByName(CustomerListByNameQry customerListByNameQry) {
        return customerListByNameQryExe.execute(customerListByNameQry);
    }

    @Override
    public Response getCustomer(CustomerAddCmd customerAddCmd) {

        return null;
    }

}