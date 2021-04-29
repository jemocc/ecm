package org.cc.fileserver.controller;

import org.cc.common.model.RspResult;
import org.cc.fileserver.Server.FileService;
import org.cc.fileserver.entity.Video;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FileCtrl {
    private final FileService fileService;

    public FileCtrl(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/pr/save-videos")
    public RspResult<Integer> saveRemoteVideo(@RequestBody List<Video> videos) {
        int i = fileService.saveRemoteVideo(videos);
        return RspResult.ok(i);
    }

    @GetMapping("/pr/cache-video/{id}")
    public RspResult<Integer> saveRemoteVideo(@PathVariable("id") Integer id) {
        int i = fileService.cacheVideo(id);
        return RspResult.ok(i);
    }

    @GetMapping("/pr/cache-cover")
    public RspResult<Void> cacheCover() {
        fileService.cacheCover();
        return RspResult.ok(null);
    }

    @GetMapping("/pr/test-lock")
    public RspResult<Void> testLock() {
        fileService.testLock();
        return RspResult.ok(null);
    }
}
