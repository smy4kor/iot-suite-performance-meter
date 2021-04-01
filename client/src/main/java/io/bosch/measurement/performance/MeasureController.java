package io.bosch.measurement.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.bosch.measurement.ditto.SubscriptionInfo;

@RestController
@RequestMapping("api/v1/measure")
public class MeasureController {

    @Autowired
    MeasureService service;

    @RequestMapping(value = "using-events/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public String measureUsingEvents(@RequestHeader(value = "x-clientId", required = true) final String clientId,
            @RequestHeader(value = "x-secret", required = true) final String secret,
            @RequestHeader(value = "x-serviceInstanceId", required = true) final String serviceInstanceId,
            @RequestHeader(value = "x-thingId", required = true) final String thingId,
            @PathVariable(name = "count", required = true) final Long count) {
        final SubscriptionInfo subscriptionInfo = new SubscriptionInfo(clientId, secret, serviceInstanceId, thingId);
        return service.measureUsingEvents(subscriptionInfo, count);
    }

    @RequestMapping(value = "using-feature/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public String measureUsingFeature(@RequestHeader(value = "x-clientId", required = true) final String clientId,
            @RequestHeader(value = "x-secret", required = true) final String secret,
            @RequestHeader(value = "x-serviceInstanceId", required = true) final String serviceInstanceId,
            @RequestHeader(value = "x-thingId", required = true) final String thingId,
            @PathVariable(name = "count", required = true) final Long count) {
        final SubscriptionInfo subscriptionInfo = new SubscriptionInfo(clientId, secret, serviceInstanceId, thingId);
        return service.measureUsingFeature(subscriptionInfo, count);
    }

    @RequestMapping(value = "status/{id}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String getStatus(@PathVariable(name = "id", required = true) final String id) {
        return service.getStatus(id);
    }
}
