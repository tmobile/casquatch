package ${package}.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tmobile.opensource.casquatch.CassandraDriver;
import com.tmobile.opensource.casquatch.dao.AbstractCassandraDAO;
import ${package}.${className};

@RestController
@RequestMapping(value="/v1/${keyspace}/${table}/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ${className}DAO extends AbstractCassandraDAO<${className}> {
    @Autowired
    public ${className}DAO(CassandraDriver db) {
        this.setClazz(${className}.class);
        this.setDB(db);
    }
}
