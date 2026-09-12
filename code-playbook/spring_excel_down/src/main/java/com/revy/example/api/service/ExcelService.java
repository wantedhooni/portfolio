package com.revy.example.api.service;

import java.io.IOException;
import java.io.OutputStream;

public interface ExcelService {
    void writeLargeExcelV1(OutputStream os) throws IOException;

    void writeLargeExcelV2(OutputStream os) throws IOException;
}
