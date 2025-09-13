package com.team6.bsep.backend.init;

import com.team6.bsep.backend.service.RootCaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RootCaInitRunner implements CommandLineRunner {

    private final RootCaService rootCaService;
    @Value("${app.ca.root.auto-create:true}") boolean autoCreate;

    @Override
    public void run(String... args) throws Exception {
        if (autoCreate) rootCaService.createRootIfMissing();
    }
}
