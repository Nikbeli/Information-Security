package com.cryptoauth.infosecurity.user.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long totalElements, 
    int TotalPages, boolean last) {}
