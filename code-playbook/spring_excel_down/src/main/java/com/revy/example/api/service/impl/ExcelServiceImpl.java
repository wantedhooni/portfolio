package com.revy.example.api.service.impl;

import com.revy.example.api.service.ExcelService;
import com.revy.example.domain.Post;
import com.revy.example.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {
    private final int TOTAL_SIZE = 10000000, MAX_ROW = 1_048_576;
    private final PostService postService;

    @Override
    public void writeLargeExcelV1(OutputStream os) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); /* 핵심: 메모리 유지 행 수 */
        SXSSFWorkbook workbook = new SXSSFWorkbook(1000);
        Sheet sheet = workbook.createSheet("Posts");
        createHeader(sheet);
        int rowNum = 1; /* 우선 들고 오는 것조차 버겁다. */
        Page<Post> result = postService.find(0, TOTAL_SIZE);
        for (Post post : result) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(post.getId());
            row.createCell(1).setCellValue(post.getTitle());
            row.createCell(2).setCellValue(post.getContent());
            row.createCell(3).setCellValue(post.getCreatedDate());
            row.createCell(4).setCellValue(post.getLastModifiedDate());
        }
        workbook.write(os);
        workbook.dispose(); /* 임시파일 제거 (필수) */
        stopWatch.stop();
        log.info("V1 working time: {}ms", stopWatch.getTotalTimeMillis());
    }

    @Override
    public void writeLargeExcelV2(OutputStream os) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(); /* 핵심: 메모리 유지 행 수 */
        SXSSFWorkbook workbook = new SXSSFWorkbook(1000);
        Sheet sheet = workbook.createSheet("Posts");
        createHeader(sheet);
        int page = 0, chunk = 10000, rowNum = 1;
        while (true) {
            Page<Post> result = postService.find(page, chunk);
            for (Post post : result) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(post.getId());
                row.createCell(1).setCellValue(post.getTitle());
                row.createCell(2).setCellValue(post.getContent());
                row.createCell(3).setCellValue(post.getCreatedDate());
                row.createCell(4).setCellValue(post.getLastModifiedDate());
            }
            log.info("V2 page:{}, row:{} end", page, rowNum);
            if (!result.hasNext() || rowNum + chunk >= MAX_ROW) {
                log.info("V2 While Break;");
                break;
            }
            page++;
        }
        workbook.write(os);
        workbook.dispose(); /* 임시파일 제거 (필수) */
        stopWatch.stop();
        log.info("V2 working time: {}ms", stopWatch.getTotalTimeMillis());
        logMemory();
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("TITLE");
        header.createCell(2).setCellValue("Content");
        header.createCell(3).setCellValue("Create Date");
        header.createCell(4).setCellValue("List Modified Date");
    }

    private void logMemory() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory(), free = rt.freeMemory(), used = total - free, max = rt.maxMemory();
        log.info("used={}MB | free={}MB | total={}MB | max={}MB", toMb(used), toMb(free), toMb(total), toMb(max));
    }

    private long toMb(long bytes) {
        return bytes / 1024 / 1024;
    }
}
