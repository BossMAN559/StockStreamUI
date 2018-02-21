package stockstream.tv.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import stockstream.tv.application.spring.AppContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Application {

    public static AnnotationConfigWebApplicationContext initApplicationContext(final int port) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppContext.class);
        context.refresh();

        final SparkServer sparkServer = context.getBean(SparkServer.class);
        sparkServer.startServer(port);
        return context;
    }

    public static void main(final String[] args) throws IOException {
        log.info("WebTV initialized");

        final String adminsStr = System.getenv().getOrDefault("TV_ADMINS", "");
        final List<String> admins = new ArrayList<>(Arrays.asList(adminsStr.split(" ,")));
        admins.stream().filter(admin -> !StringUtils.isEmpty(admin)).forEach(admin -> Config.TV_ADMINS.add(admin));


        Config.SUBSCRIBERS_ONLY = Boolean.valueOf(System.getenv("SUBSCRIBERS_ONLY"));
        Config.stage = Stage.valueOf(System.getenv("STAGE"));
        int port = Integer.valueOf(System.getenv("PORT"));

        initApplicationContext(port);
    }

}
