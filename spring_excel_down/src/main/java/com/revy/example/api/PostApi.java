package com.revy.example.api;

import com.revy.example.api.dto.PostCreateRequest;
import com.revy.example.api.dto.PostResponse;
import com.revy.example.api.dto.PostUpdateRequest;
import com.revy.example.api.service.ExcelService;
import com.revy.example.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "게시글 API")
public class PostApi {
    private final PostService postService;
    private final ExcelService excelService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostCreateRequest req) {
        PostResponse res = postService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "게시글 단건 조회", description = "id로 게시글을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(postService.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.list(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest req) {
        return ResponseEntity.ok(postService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/excel/large-download/v1")
    public void downloadLargeExcelV1(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=post_v1.xlsx");
        excelService.writeLargeExcelV1(response.getOutputStream());
    }

    @GetMapping("/excel/large-download/v2")
    public void downloadLargeExcelV2(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=post_v2.xlsx");
        excelService.writeLargeExcelV2(response.getOutputStream());
    }

    @GetMapping("/excel/large-download/v3")
    public void downloadLargeExcelV3(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=post_v3.xlsx");
        excelService.writeLargeExcelV3(response.getOutputStream());
    }
}
