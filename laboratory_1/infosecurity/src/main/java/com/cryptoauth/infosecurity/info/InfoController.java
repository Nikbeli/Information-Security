package com.cryptoauth.infosecurity.info;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/info")
public class InfoController {

    @GetMapping("/about")
    public ResponseEntity<InfoResponse> about() {
        return ResponseEntity.ok(new InfoResponse(
                "Белянин Никита Николаевич",
                "ПИбд-41",
                "Лабораторная работа №1",
                "Разграничение полномочий пользователя с криптографической защитой",
                "DES (режим CBC), SHA",
                "Отсутствие повторяющихся символов в пароле"));
    }
}
