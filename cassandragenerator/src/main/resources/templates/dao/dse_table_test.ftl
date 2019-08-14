package ${package};

import com.tmobile.opensource.casquatch.junit.AbstractCassandraDAOTests;
import ${package}.dao.CassandraDAO;
import ${package}.dao.${className}DAO;
import ${package}.${className};
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= CassandraDAO.class)
@AutoConfigureMockMvc
public class CassandraGenerator${className}DAOTests extends AbstractCassandraDAOTests<${className},${className}DAO> {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    @Spy
    protected ${className}DAO service;

    @Override
    protected ${className}DAO getService() {
        return this.service;
    }

    @Override
    protected Class<${className}DAO> getServiceClass() {
        return ${className}DAO.class;
    }

    @Override
    protected Class<${className}> getTableClass() {
        return ${className}.class;
    }

    @Override
    protected MockMvc getMockMvc() {
        return this.mvc;
    }

}
