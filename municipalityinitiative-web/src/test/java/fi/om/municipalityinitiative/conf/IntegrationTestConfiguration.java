package fi.om.municipalityinitiative.conf;

import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.service.AccessDeniedException;
import fi.om.municipalityinitiative.service.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.File;

@Configuration
@Import(AppConfiguration.class)
@PropertySource({"classpath:default.properties", "classpath:test.properties"})
public class IntegrationTestConfiguration {

    @Bean
    public TestHelper testHelper() {
        return new TestHelper();
    }

    @Bean
    public FakeUserService userService() {
        return new FakeUserService();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        File file = new File(System.getProperty("user.dir"), "src/main/webapp/WEB-INF/messages");
        messageSource.setBasenames(file.toURI().toString());
        return messageSource;
    }

    public static class FakeUserService extends UserService {


        private boolean isOmUser;

        public void setOmUser(boolean omUser) {
            isOmUser = omUser;
        }

        @Override
        public void requireOmUser() {
            if (!isOmUser) {
                throw new AccessDeniedException();
            }
        }
    }

}

