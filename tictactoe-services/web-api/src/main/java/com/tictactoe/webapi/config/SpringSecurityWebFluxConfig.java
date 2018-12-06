package com.tictactoe.webapi.config;

import com.tictactoe.authmodule.auth.JwtAuthSuccessHandler;
import com.tictactoe.authmodule.auth.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableReactiveMethodSecurity
public class SpringSecurityWebFluxConfig {

  private static final String[] WHITELISTED_AUTH_URLS = {
      "/login",
      "/",
      "/public/**",
  };

  private final ReactiveUserDetailsService userDetailsRepositoryInMemory;

  public SpringSecurityWebFluxConfig(
      @Qualifier("userDetailsRepositoryInMemory") ReactiveUserDetailsService userDetailsRepositoryInMemory
  ) {
    this.userDetailsRepositoryInMemory = userDetailsRepositoryInMemory;
  }

  @Bean
  public ReactiveAuthenticationManager reactiveAuthenticationManager() {
    return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsRepositoryInMemory);
  }

  /**
   * The test defined in SampleApplicationTests class will only get executed
   * if you change the authentication mechanism to basic (from form mechanism)
   * in SpringSecurityWebFluxConfig file
   *
   * @param http
   * @return
   * @throws Exception
   */
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtService jwtService) {

    AuthenticationWebFilter authenticationJWT = new AuthenticationWebFilter(new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsRepositoryInMemory));
    authenticationJWT.setAuthenticationSuccessHandler(new JwtAuthSuccessHandler(jwtService));

    http.csrf().disable();

    http
        .authorizeExchange()
        .pathMatchers(WHITELISTED_AUTH_URLS)
        .permitAll()
        .and()
        .addFilterAt(authenticationJWT, SecurityWebFiltersOrder.FIRST)
        .exceptionHandling()
        .and()
        .authorizeExchange()
        .pathMatchers(HttpMethod.POST, "/auth/token").authenticated()
        .pathMatchers("/actuator/**").hasRole("SYSTEM")
        .pathMatchers(HttpMethod.GET, "/url-protected/games/**").hasRole("USER")
        .pathMatchers(HttpMethod.POST, "/url-protected/game/**").hasRole("ADMIN")
        .anyExchange()
        .authenticated()
        .and()
        .addFilterAt(new WebApiJwtAuthWebFilter(jwtService), SecurityWebFiltersOrder.HTTP_BASIC)
        .exceptionHandling();

    return http.build();
  }

}