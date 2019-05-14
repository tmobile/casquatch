
package ${package};

import ${package}.${className};
import com.tmobile.opensource.casquatch.junit.AbstractCassandraDriverTableTests;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraGenerator${className}Tests extends AbstractCassandraDriverTableTests<${className}> {

    @Override
    protected Class<${className}> getTableClass() {
        return ${className}.class;
    }
}
