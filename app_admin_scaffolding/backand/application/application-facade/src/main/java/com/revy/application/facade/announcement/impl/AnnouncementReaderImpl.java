package com.revy.application.facade.announcement.impl;

import com.revy.application.facade.announcement.AnnouncementReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnnouncementReaderImpl implements AnnouncementReader {

}
