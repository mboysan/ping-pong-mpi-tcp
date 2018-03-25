package config;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;

public class LoggerConfig {
    static {
        configRefined(Level.DEBUG);
    }

    private static void configRefined(Level level){
        Configurator.currentConfig()
                .formatPattern("{date:HH:mm:ss:SSS} {class}.{method}(): {message}")
                .level(level)
                .activate();
    }

    private static void configDetailed(Level level){
        Configurator.currentConfig()
                .formatPattern("{date:HH:mm:ss:SSS} [{level}] {thread}-{class}.{method}(): {message}")
                .level(level)
                .activate();
    }
}
