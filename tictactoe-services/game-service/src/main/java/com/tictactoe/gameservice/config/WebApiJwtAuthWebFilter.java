package com.tictactoe.gameservice.config;

import com.tictactoe.authmodule.auth.JwtAuthWebFilter;
import com.tictactoe.authmodule.auth.JwtService;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.util.ArrayList;
import java.util.List;

/**
 * User: aleksey
 * Date: 2018-12-03
 * Time: 09:29
 */
public class WebApiJwtAuthWebFilter extends JwtAuthWebFilter {

  public WebApiJwtAuthWebFilter(ReactiveAuthenticationManager reactiveAuthManager, JwtService jwtService) {
    super(jwtService);
  }

  @Override
  protected ServerWebExchangeMatcher getAuthMatcher() {
    List<ServerWebExchangeMatcher> matchers = new ArrayList<>();
    matchers.add(new PathPatternParserServerWebExchangeMatcher("/v1/games/**", HttpMethod.GET));
    matchers.add(new PathPatternParserServerWebExchangeMatcher("/v1/games/**", HttpMethod.POST));
    return ServerWebExchangeMatchers.matchers(new OrServerWebExchangeMatcher(matchers));
  }
}