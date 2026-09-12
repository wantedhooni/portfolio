package com.revy.application.facade;

import java.util.Optional;
import java.util.UUID;

public interface ViewReader<PAGE_DTO, VIEW_DTO > {

    Optional<VIEW_DTO> getViewById(UUID id);
    PAGE_DTO getPage(int page, int size, String sortBy, String sortDirection, String paramQuery);
}
