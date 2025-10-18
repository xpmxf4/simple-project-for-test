package support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@Import(TestTransactionConfig.class)
@ContextConfiguration(initializers = JpaBeanInitializer.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractJpaTest extends AbstractTest {

    @PersistenceContext
    protected EntityManager em;

    @Autowired
    protected TestTransactionSupport testTransactionSupport;

    protected final void flushAndClear() {
        em.flush();
        em.clear();
    }
}
