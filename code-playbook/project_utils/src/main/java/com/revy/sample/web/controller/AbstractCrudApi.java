package com.revy.sample.web.controller;

import com.revy.sample.web.common.ApiPageResponse;
import com.revy.sample.web.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class AbstractCrudApi<CREQ, UREQ, RES> {

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RES>> create(@Valid @RequestBody CREQ req) {
        return ResponseEntity.ok(ApiResponse.ok(doCreate(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RES>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(doGet(id)));
    }

    @GetMapping({"/search"})
    public ResponseEntity<ApiResponse<ApiPageResponse<RES>>> search(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size,
                                                                    @RequestParam(required = false) String sortBy,
                                                                    @RequestParam(defaultValue = "DESC") String sortDirection,
                                                                    @RequestParam(required = false) String paramQuery) {
        return ResponseEntity.ok(ApiResponse.ok(getPage(page, size, sortBy, sortDirection, paramQuery)));
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiResponse<RES>> update(@PathVariable UUID id, @Valid @RequestBody UREQ req) {
        return ResponseEntity.ok(ApiResponse.ok(doUpdate(id, req)));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        doDelete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    protected abstract ApiPageResponse<RES> getPage(int page, int size, String sortBy, String sortDirection,
                                                    String paramQuery);

    protected abstract RES doCreate(CREQ req);

    protected abstract RES doGet(UUID id);


    protected abstract RES doUpdate(UUID id, UREQ req);

    protected abstract void doDelete(UUID id);
}