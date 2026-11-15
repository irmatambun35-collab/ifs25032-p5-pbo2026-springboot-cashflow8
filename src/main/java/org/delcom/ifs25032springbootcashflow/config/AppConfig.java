package org.delcom.ifs25032springbootcashflow.config;

import org.delcom.ifs25032springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25032springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25032springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25032springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25032springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25032springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25032springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25032springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25032springbootcashflow.usecase.UserUseCase;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {
    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource, IUserRepository users) {
        return new JdbcTransactionRepository(dataSource); // dependensi users memastikan skema dibuat lebih dulu
    }

    @Bean public AuthUseCase authUseCase(IUserRepository users) { return new AuthUseCase(users); }
    @Bean public UserUseCase userUseCase(IUserRepository users) { return new UserUseCase(users); }
    @Bean public FinanceUseCase financeUseCase(ITransactionRepository repo) { return new FinanceUseCase(repo); }
    @Bean public FinancePresenter financePresenter() { return new FinancePresenter(); }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(AuthUseCase authUseCase,
                                           @Value("${app.root-username}") String rootUsername,
                                           @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    @Bean
    public WebMvcConfigurer uploadsMapping(FileStorage storage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**").addResourceLocations(storage.root().toUri().toString());
            }
        };
    }
}
