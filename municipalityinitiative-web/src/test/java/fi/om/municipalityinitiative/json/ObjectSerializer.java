package fi.om.municipalityinitiative.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fi.om.municipalityinitiative.util.Maybe;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.IOException;

public class ObjectSerializer {
    public static String objectToString(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        mapper.registerModule(new MaybeModule());
        try {
            return mapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class MaybeModule extends SimpleModule {
        public MaybeModule() {
            addSerializer(Maybe.class, new MaybeSerializer());
            addSerializer(LocalDate.class, new LocalDateJsonSerializer());
            addSerializer(DateTime.class, new DateTimeJsonSerializer());
        }
    }
}