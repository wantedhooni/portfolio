package com.revy.executor.api;

import com.revy.executor.service.QuartzService;
import com.revy.executor.service.dto.QuartzDtos;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api/quartz")
public class QuartzController {

    private final QuartzService quartzService;

    public QuartzController(QuartzService quartzService) {
        this.quartzService = quartzService;
    }

    @GetMapping("/jobs")
    public List<QuartzDtos.JobKeyDto> listJobs() throws SchedulerException {
        return quartzService.listAllJobs().stream()
                            .map(k -> new QuartzDtos.JobKeyDto(k.getName(), k.getGroup()))
                            .toList();
    }

    @PostMapping("/jobs/trigger")
    public void triggerNow(@RequestBody QuartzDtos.JobKeyDto jobKey) throws SchedulerException {
        quartzService.triggerNow(new JobKey(jobKey.name(), jobKey.group()));
    }

    @PostMapping("/jobs/pause")
    public void pause(@RequestBody QuartzDtos.JobKeyDto jobKey) throws SchedulerException {
        quartzService.pauseJob(new JobKey(jobKey.name(), jobKey.group()));
    }

    @PostMapping("/jobs/resume")
    public void resume(@RequestBody QuartzDtos.JobKeyDto jobKey) throws SchedulerException {
        quartzService.resumeJob(new JobKey(jobKey.name(), jobKey.group()));
    }

    @PostMapping("/jobs/delete")
    public void delete(@RequestBody QuartzDtos.JobKeyDto jobKey) throws SchedulerException {
        quartzService.deleteJob(new JobKey(jobKey.name(), jobKey.group()));
    }
}
