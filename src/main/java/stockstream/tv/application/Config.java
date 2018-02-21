package stockstream.tv.application;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static stockstream.tv.application.Stage.TEST;

public class Config {

    public static final Integer ELECTION_LENGTH_SECONDS = Integer.parseInt(System.getenv().getOrDefault("ELECTION_LENGTH_SECONDS", "30"));

    public static final String CUSTOM_PRESENTATION_URL = System.getenv().getOrDefault("CUSTOM_PRESENTATION_URL", "");

    public static Stage stage = TEST;

    public static boolean SUBSCRIBERS_ONLY = false;

    public static Set<String> TV_ADMINS = ConcurrentHashMap.newKeySet();

}
