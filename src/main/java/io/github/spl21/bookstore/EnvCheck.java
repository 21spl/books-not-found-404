package io.github.spl21.bookstore;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
public class EnvCheck implements CommandLineRunner{


    public final Environment env;

    public EnvCheck(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DB_URL = " + env.getProperty("DB_URL"));
        System.out.println("DB_USERNAME = " + env.getProperty("DB_USERNAME"));
        System.out.println("DB_PASSWORD= " + env.getProperty("DB_PASSWORD"));
    }

    
}
