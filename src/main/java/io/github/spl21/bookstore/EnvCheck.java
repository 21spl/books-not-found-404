package io.github.spl21.bookstore;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class EnvCheck implements CommandLineRunner{


    public final Environment env;
    private final PasswordEncoder passwordEncoder;

    public EnvCheck(Environment env, PasswordEncoder passwordEncoder) {
        this.env = env;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DB_URL = " + env.getProperty("DB_URL"));
        System.out.println("DB_USERNAME = " + env.getProperty("DB_USERNAME"));
        System.out.println("DB_PASSWORD= " + env.getProperty("DB_PASSWORD"));

        System.out.println(passwordEncoder.encode("admin123"));
    }

    
}
