package io.bosch.measurement.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/measure")
public class MeasureController {

    @Autowired
    MeasureService service;

    @RequestMapping(value = "using-events/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public String measureUsingEvents(@PathVariable(name = "count", required = true) final Long count) {
        return service.measureUsingEvents(count);
    }

    @RequestMapping(value = "using-feature/{count}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public String measureUsingFeature(@PathVariable(name = "count", required = true) final Long count) {
        return service.measureUsingFeature(count);
    }

    @RequestMapping(value = "status/{id}", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public String getStatus(@PathVariable(name = "id", required = true) final String id) {
        return service.getStatus(id);
    }
}
