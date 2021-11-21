package com.score.filter;

import com.score.validator.UriValidator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;


public class RequestFilter extends Filter {

    private static final Logger LOGGER = Logger.getLogger(RequestFilter.class.getName());

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        if (UriValidator.isValidUri(exchange.getRequestURI().toString())) {
            chain.doFilter(exchange);
        } else {
            LOGGER.warning("Request not supported " + exchange.getRequestURI().toString());
            exchange.sendResponseHeaders(HTTP_BAD_REQUEST, -1);
            exchange.getResponseBody().close();
        }
    }

    @Override
    public String description() {
        return "Filter the requests";
    }
}
