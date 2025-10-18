package support;

import org.junit.jupiter.api.extension.*;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.ScopedMock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.session.MockitoSessionLoggerAdapter;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class AutoMockExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private final static ExtensionContext.Namespace MOCKITO = create("org.mockito");

    private final static String SESSION = "session", MOCKS = "mocks";

    private final Strictness strictness;

    // This constructor is invoked by JUnit Jupiter via reflection or ServiceLoader
    @SuppressWarnings("unused")
    public AutoMockExtension() {
        this(Strictness.STRICT_STUBS);
    }

    public AutoMockExtension(Strictness strictness) {
        this.strictness = strictness;
    }

    private Optional<MockitoSettings> retrieveAnnotationFromTestClasses(final ExtensionContext context) {
        ExtensionContext currentContext = context;
        Optional<MockitoSettings> annotation;

        do {
            annotation = findAnnotation(currentContext.getElement(), MockitoSettings.class);

            if (currentContext.getParent().isEmpty()) {
                break;
            }

            currentContext = currentContext.getParent().get();
        } while (annotation.isEmpty() && currentContext != context.getRoot());

        return annotation;
    }

    @Override
    public void afterAll(ExtensionContext context) {
        context.getStore(MOCKITO).remove(MOCKS, Set.class);
        context.getStore(MOCKITO).remove(SESSION, MockitoSession.class)
                .finishMocking(context.getExecutionException().orElse(null));
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        var testInstance = context.getRequiredTestInstance();

        Strictness actualStrictness = this.retrieveAnnotationFromTestClasses(context)
                .map(MockitoSettings::strictness)
                .orElse(strictness);

        MockitoSession session = Mockito.mockitoSession()
                .initMocks(testInstance)
                .strictness(actualStrictness)
                .logger(new MockitoSessionLoggerAdapter(Plugins.getMockitoLogger()))
                .startMocking();

        var mockSet = new HashSet<>();

        var fields = testInstance.getClass().getDeclaredFields();

        for (var field : fields) {
            try {
                field.setAccessible(true);
                var extractField = field.get(testInstance);

                if (Mockito.mockingDetails(extractField).isMock() || Mockito.mockingDetails(extractField).isSpy()) {
                    mockSet.add(extractField);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        context.getStore(MOCKITO).put(MOCKS, mockSet);
        context.getStore(MOCKITO).put(SESSION, session);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        var mocks = context.getStore(MOCKITO).get(MOCKS, Set.class);
        Mockito.reset(mocks.toArray());
    }
}
