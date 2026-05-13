package com.revy.example.admin.api.common;

import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j(access = AccessLevel.PROTECTED)
public abstract class AbstractCrudApi<ID, CREQ, UREQ, SREQ, RES> {

    @PostMapping
    public ResponseEntity<ApiResponse<RES>> create(
            @Valid @RequestBody CREQ request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(doCreate(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RES>> get(
            @PathVariable ID id
    ) {
        return ok(doGet(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<RES>>> search(
            Pageable pageable,
            @Valid @ModelAttribute SREQ searchRequest
    ) {
        return ok(getPage(pageable, searchRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RES>> update(
            @PathVariable ID id,
            @Valid @RequestBody UREQ request
    ) {
        return ok(doUpdate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable ID id
    ) {
        doDelete(id);
        return ok();
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    private ResponseEntity<ApiResponse<Void>> ok() {
        return ResponseEntity.ok(ApiResponse.ok());
    }

    protected abstract ApiPageResponse<RES> getPage(Pageable pageable, SREQ searchRequest);

    protected abstract RES doCreate(CREQ request);

    protected abstract RES doGet(ID id);

    protected abstract RES doUpdate(ID id, UREQ request);

    protected abstract void doDelete(ID id);
}
