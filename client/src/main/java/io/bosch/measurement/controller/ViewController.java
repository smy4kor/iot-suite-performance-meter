package io.bosch.measurement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.bosch.measurement.performance.MeasureService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final MeasureService service;

    @GetMapping("/ui")
    public String index(final Model model) {
        // loads 1 and display 1, stream data, data driven mode.
        // final IReactiveDataDriverContextVariable reactiveDataDrivenMode = new
        // ReactiveDataDriverContextVariable(
        // this.service.getStatusStream(), 1);

        model.addAttribute("allstatus", this.service.getStatus());
        return "index";
    }

}