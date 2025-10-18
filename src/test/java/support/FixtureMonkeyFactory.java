package support;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.*;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;

import java.util.List;

public abstract class FixtureMonkeyFactory {

    private static final FixtureMonkey INSTANCE = FixtureMonkey.builder()
            .objectIntrospector(new FailoverIntrospector(
                    List.of(
                            FieldReflectionArbitraryIntrospector.INSTANCE,
                            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                            BuilderArbitraryIntrospector.INSTANCE,
                            BeanArbitraryIntrospector.INSTANCE
                    )
            ))
            .defaultNotNull(true)
            .enableLoggingFail(false)
            .plugin(
                    new SimpleValueJqwikPlugin()
                            .minNumberValue(1)
                            .maxNumberValue(20_000_000)
                            .minStringLength(1)
                            .maxStringLength(300)
            )
            .build();

    public static FixtureMonkey getInstance() {
        return INSTANCE;
    }
}
