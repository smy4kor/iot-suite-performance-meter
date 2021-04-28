package io.bosch.measurement.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
            @RequestParam(value = "delay", required = false) final int delay,
            @RequestBody(required = false) final Map desiredResponseHeaders) {
        final Request request = Request.builder().id(RequestUtil.generateId()).count(count).delay(delay)
                .responseHeaders(RequestUtil.enrichHeaders(desiredResponseHeaders)).build();
        service.measureUsingEvents(request);
        return request;
    }

    @RequestMapping(value = "using-feature/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Request measureUsingFeature(@PathVariable(name = "count", required = true) final int count,
            @RequestParam(value = "delay", required = false) final int delay) {
        final Request request = Request.builder().id(RequestUtil.generateId()).count(count).delay(delay).build();
        service.measureUsingFeature(request);
        return request;
    }

    @RequestMapping(value = "using-rest/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Request measureUsingRest(@PathVariable(name = "count", required = true) final int count,
            @RequestParam(value = "delay", required = false) final int delay,
            @RequestParam(value = "response-url", required = true) final String responseUrl) {
        final Request request = Request.builder().id(RequestUtil.generateId()).count(count).delay(delay)
                .responseUrl(responseUrl).build();
        service.measureUsingRest(request);
        return request;
    }

    @RequestMapping(value = "status", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Map getStatus() {
        return service.getStatus();
    }

}
