package dev.pjc1991.commerce.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CriteriaBuilderFactoryConfig {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public CriteriaBuilderFactory criteriaBuilderFactory() {
        return Criteria.getDefault().createCriteriaBuilderFactory(entityManagerFactory);
    }
}
