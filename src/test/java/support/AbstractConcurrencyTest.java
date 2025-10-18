package support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(TestTransactionConfig.class)
public class AbstractConcurrencyTest extends AbstractIntegrationServiceTest {

    @Autowired
    protected TestTransactionSupport testTransactionSupport;
}
