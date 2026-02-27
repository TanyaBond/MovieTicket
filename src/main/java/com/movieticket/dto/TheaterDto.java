package com.movieticket.dto;

import java.util.List;
import java.util.UUID;

public record TheaterDto(UUID id, String name, List<ScreenDto> screens) {
}
