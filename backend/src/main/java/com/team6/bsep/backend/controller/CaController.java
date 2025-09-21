package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.CaInfo;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ca")
@RequiredArgsConstructor
public class CaController {

    private final CertificateAuthorityRepository caRepo;

    @GetMapping("/list")
    public List<CaInfo> listCas() {
        return caRepo.findAll().stream()
                .map(ca -> new CaInfo(
                        ca.getId(),
                        ca.getSubjectDn(),
                        ca.getNotBefore(),
                        ca.getNotAfter()
                ))
                .toList();
    }

}

