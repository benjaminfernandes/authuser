package com.ead.authuser.dtos;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JwtDto {

    @NonNull //indica que esta variável será utilizada no construtor com a anotação @RequiredArgsConstructor
    private String token;

    private String type = "Bearer";
}
