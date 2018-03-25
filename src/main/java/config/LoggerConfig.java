package config;

import org.pmw.tinylog.Configurator;

public class LoggerConfig {
    static {
        configRefined();
    }

    private static void configRefined(){
        Configurator.currentConfig()
                .formatPattern("{date:HH:mm:ss:SSS} {class}.{method}(): {message}")
                .activate();
    }

    private static void configDetailed(){
        Configurator.currentConfig()
                .formatPattern("{date:HH:mm:ss:SSS} [{level}] {thread}-{class}.{method}(): {message}")
                .activate();
    }
}
