package com.movieticket.service;

import com.movieticket.dto.ScreenDto;
import com.movieticket.dto.TheaterDto;
import com.movieticket.repository.TheaterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public TheaterService(TheaterRepository theaterRepository) {
        this.theaterRepository = theaterRepository;
    }

    public List<TheaterDto> getAllTheaters() {
        return theaterRepository.findAll().stream()
                .map(t -> new TheaterDto(
                        t.getId(),
                        t.getName(),
                        t.getScreens().stream()
                                .map(s -> new ScreenDto(s.getId(), s.getLabel()))
                                .toList()
                ))
                .toList();
    }
}
