package name.yzhu.example.config;

import jakarta.annotation.PostConstruct;
import name.yzhu.example.Interceptor.DataMaskingInterceptor;
import name.yzhu.example.Interceptor.FieldMaskInterceptor;
import name.yzhu.example.Interceptor.ResultSetMaskInterceptor;
import name.yzhu.example.Interceptor.SqlInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MyBatisConfig {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void addInterceptors() {
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
//        configuration.addInterceptor(new DataMaskingInterceptor(Arrays.asList("password")));
        configuration.addInterceptor(new FieldMaskInterceptor());
//        configuration.addInterceptor(new ResultSetMaskInterceptor());

//        configuration.addInterceptor(new RecordFilterInterceptor());
    }
}