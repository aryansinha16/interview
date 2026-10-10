package com.optiflow.configdiff;

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
@RequestMapping("/api/v1/configs")
public class ConfigDiffController {

    private final ConfigDiffService service;

    ConfigDiffController(ConfigDiffService service) {
        this.service = service;
    }

    @PostMapping("/diff")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfigDiffResponse diff(@Valid @RequestBody ConfigDiffRequest request) {
        return service.diff(request);
    }

    @GetMapping("/{configId}/diffs")
    public List<ConfigDiffResponse> history(@PathVariable String configId) {
        return service.history(configId);
    }
}
