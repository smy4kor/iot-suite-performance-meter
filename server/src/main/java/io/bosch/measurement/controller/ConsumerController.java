package io.bosch.measurement.controller;

import io.bosch.measurement.consumers.RestConsumer;
import io.bosch.measurement.performance.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/consumer")
@Slf4j
public class ConsumerController {

    @Autowired
    RestConsumer restConsumer;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void receiveResponse(@RequestBody final Response response) {
        restConsumer.accept(response);
    }
}
