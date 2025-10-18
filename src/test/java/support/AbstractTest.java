package support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTest {

    protected final FixtureMonkey fixture = FixtureMonkeyFactory.getInstance();
}
