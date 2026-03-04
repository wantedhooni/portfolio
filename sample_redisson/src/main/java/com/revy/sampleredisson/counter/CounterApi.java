package com.revy.sampleredisson.counter;

import com.revy.sampleredisson.counter.payload.CounterResponse;
import com.revy.sampleredisson.counter.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counter")
@RequiredArgsConstructor
public class CounterApi {

    private final CounterService service;

    @GetMapping("/{name}")
    public CounterResponse get(@PathVariable String name) {
        return service.get(name);
    }

    @PostMapping("/{name}/increment")
    public CounterResponse increment(@PathVariable String name, @RequestParam(defaultValue = "1") long delta) {
        return service.increment(name, delta);
    }


}
