// package com.payplatform.security.config;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// @ConditionalOnWebApplication(type = Type.SERVLET)
// public class CommonWebSecurityConfig {

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         return http
//                 .csrf(csrf -> csrf.disable())
//                 .httpBasic(Customizer.withDefaults())
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/actuator/health/**").permitAll()
//                         .anyRequest().authenticated())
//                 .build();
//     }
// }