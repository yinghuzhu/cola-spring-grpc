package name.yzhu.example.dto;

import name.yzhu.example.dto.data.CustomerDTO;
import lombok.Data;

@Data
public class CustomerAddCmd{

    private CustomerDTO customerDTO;

}
