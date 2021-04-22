package org.cc.fileserver.controller;

import org.cc.common.model.RspResult;
import org.cc.fileserver.Server.FileService;
import org.cc.fileserver.entity.Video;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}
