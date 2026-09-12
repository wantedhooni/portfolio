package com.revy.example.admin.api.admin;

import com.revy.example.admin.api.common.AbstractCrudApi;
import com.revy.example.core.common.ApiPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController extends AbstractCrudApi {


    @Override
    protected ApiPageResponse getPage(int page, int size, String sortBy, String sortDirection, String paramQuery) {
        return null;
    }

    @Override
    protected Object doCreate(Object req) {
        return null;
    }

    @Override
    protected Object doGet(UUID id) {
        return null;
    }

    @Override
    protected Object doUpdate(UUID id, Object req) {
        return null;
    }

    @Override
    protected void doDelete(UUID id) {

    }
}
