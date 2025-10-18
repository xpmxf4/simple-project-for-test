package support;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import java.beans.Introspector;

public class JpaBeanInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        var beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        var beanScanner = new ClassPathScanningCandidateComponentProvider( false);
        beanScanner.addIncludeFilter(new AnnotationTypeFilter(Repository.class));

        var repositoryBeans = beanScanner.findCandidateComponents("com.concurrency");

        System.out.println("repositoryBeans = " + repositoryBeans);

        for (var definition : repositoryBeans) {
            var beanName = Introspector.decapitalize(definition.getBeanClassName());
            beanFactory.registerBeanDefinition(beanName, definition);
        }


//        applicationContext.refresh();
    }
}
