package io.bosch.measurement.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.bosch.measurement.performance.MeasureService;
import io.bosch.measurement.performance.Request;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/measure")
@RequiredArgsConstructor
public class MeasureController {

    private final MeasureService service;

    @RequestMapping(value = "using-events/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Request measureUsingEvents(@PathVariable(name = "count", required = true) final int count,
            @RequestBody(required = false) final Map desiredResponseHeaders) {
        final Request request = new Request(generateId(), count, enrich(desiredResponseHeaders));
        service.measureUsingEvents(request);
        return request;
    }

    @RequestMapping(value = "using-feature/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Request measureUsingFeature(@PathVariable(name = "count", required = true) final int count) {
        final Request request = new Request(generateId(), count, null);
        service.measureUsingFeature(request);
        return request;
    }

    @RequestMapping(value = "using-rest/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Request measureUsingRest(@PathVariable(name = "count", required = true) final int count) {
        final Request request = new Request(generateId(), count, null);
        service.measureUsingRest(request);
        return request;
    }

    @RequestMapping(value = "status", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Map getStatus() {
        return service.getStatus();
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Map enrich(Map desiredResponseHeaders) {
        if (desiredResponseHeaders != null) {
            return desiredResponseHeaders;
        }

        desiredResponseHeaders = new HashMap<>();
        desiredResponseHeaders.put("response-required", false);
        desiredResponseHeaders.put("content-type", "application/json");
        desiredResponseHeaders.put("correlation-id", "dont-care");
        return desiredResponseHeaders;
    }
}
