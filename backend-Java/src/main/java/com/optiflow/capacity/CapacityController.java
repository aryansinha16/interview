package com.optiflow.capacity;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capacity")
public class CapacityController {

    private final CapacityPlannerService service;

    CapacityController(CapacityPlannerService service) {
        this.service = service;
    }

    @PostMapping("/simulate")
    @ResponseStatus(HttpStatus.CREATED)
    public CapacitySimulationResponse simulate(@Valid @RequestBody CapacitySimulationRequest request) {
        return service.simulate(request);
    }

    @GetMapping("/{customerId}/simulations")
    public List<CapacitySimulationResponse> history(@PathVariable String customerId) {
        return service.history(customerId);
    }
}
