package org.cc.fileserver.controller;

import org.cc.common.component.FlowTrack;
import org.cc.common.exception.GlobalException;
import org.cc.common.model.Page;
import org.cc.common.model.Pageable;
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

    @GetMapping("/api/video-query-all")
    public RspResult<Page<Video>> getAllVideo(Pageable pageable) {
        Page<Video> files = fileService.queryAllVideo(pageable);
        return RspResult.ok(files);
    }

    @GetMapping("/pr/cache-cover")
    public RspResult<Void> cacheCover() {
        fileService.cacheCover(0);
        return RspResult.ok(null);
    }

    @GetMapping("/pr/test-lock")
    @FlowTrack(value = "测试LOCK", isLogInput = true, isLogOutput = true)
    public RspResult<Integer> testLock(@RequestParam Integer time, @RequestParam Integer inArgs) {
        if (time == 2000) {
            throw new GlobalException(501, "测试异常");
        }
        fileService.testLock(time);
        return RspResult.ok(1);
    }
}
