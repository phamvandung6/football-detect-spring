package com.loopy.footballvideoprocessor.video.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopy.footballvideoprocessor.common.dto.ApiResponse;
import com.loopy.footballvideoprocessor.common.dto.PagedResponse;
import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.dto.VideoUploadRequest;
import com.loopy.footballvideoprocessor.video.dto.YoutubeVideoRequest;
import com.loopy.footballvideoprocessor.video.model.VideoStatus;
import com.loopy.footballvideoprocessor.video.model.VideoType;
import com.loopy.footballvideoprocessor.video.service.R2StorageService;
import com.loopy.footballvideoprocessor.video.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "API quản lý video")
@Slf4j
public class VideoController {

    private final VideoService videoService;
    private final R2StorageService r2StorageService;

    @Operation(summary = "Lấy danh sách tất cả video của người dùng")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<VideoDto>> getAllVideos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        return ResponseEntity.ok(videoService.getAllVideos(page, size));
    }

    @Operation(summary = "Lấy danh sách video được tải lên của người dùng")
    @Parameters({
            @Parameter(name = "page", description = "Số trang, bắt đầu từ 0", example = "0"),
            @Parameter(name = "size", description = "Kích thước trang", example = "10")
    })
    @GetMapping("/uploaded")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<VideoDto>> getUploadedVideos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        return ResponseEntity.ok(videoService.getVideosByType(VideoType.UPLOADED, page, size));
    }

    @Operation(summary = "Lấy danh sách video YouTube của người dùng")
    @GetMapping("/youtube")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<VideoDto>> getYoutubeVideos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        return ResponseEntity.ok(videoService.getVideosByType(VideoType.YOUTUBE, page, size));
    }

    @Operation(summary = "Lấy thông tin video theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VideoDto>> getVideo(@PathVariable UUID id) {
        // Để GlobalExceptionHandler xử lý ResourceNotFoundException
        VideoDto video = videoService.getVideo(id);
        return ResponseEntity.ok(ApiResponse.success(video));
    }

    @Operation(summary = "Tải lên video mới")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VideoDto>> uploadVideo(
            @Valid @ModelAttribute VideoUploadRequest videoUploadRequest) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        VideoDto uploadedVideo = videoService.uploadVideo(videoUploadRequest);
        return new ResponseEntity<>(ApiResponse.success("Video đã được tải lên thành công", uploadedVideo),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Thêm video YouTube")
    @PostMapping("/youtube")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VideoDto>> addYoutubeVideo(
            @Valid @RequestBody YoutubeVideoRequest youtubeVideoRequest) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        VideoDto youtubeVideo = videoService.addYoutubeVideo(youtubeVideoRequest);
        return new ResponseEntity<>(ApiResponse.success("Video YouTube đã được thêm thành công", youtubeVideo),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin video")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VideoDto>> updateVideo(
            @PathVariable UUID id,
            @Valid @RequestBody VideoDto videoDto) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        VideoDto updatedVideo = videoService.updateVideo(id, videoDto);
        return ResponseEntity.ok(ApiResponse.success("Video đã được cập nhật thành công", updatedVideo));
    }

    @Operation(summary = "Xóa video")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable UUID id) {
        // Để GlobalExceptionHandler xử lý các ngoại lệ
        videoService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.success("Video đã được xóa thành công", null));
    }

    @Operation(summary = "Tạo URL tạm thời để truy cập thumbnail của video")
    @GetMapping("/{id}/thumbnail-url")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> generateThumbnailPresignedUrl(
            @PathVariable UUID id,
            @RequestParam(name = "expirationInMinutes", defaultValue = "15") int expirationInMinutes) {

        // Lấy thông tin video từ cơ sở dữ liệu
        VideoDto video = videoService.getVideo(id);

        // Kiểm tra xem video có phải loại YOUTUBE không
        if (video.getVideoType() == VideoType.YOUTUBE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            "Không thể tạo URL thumbnail cho video YouTube. Hãy sử dụng thumbnail YouTube trực tiếp."));
        }

        // Lấy đường dẫn của thumbnail
        String thumbnailPath = video.getThumbnailPath();
        if (thumbnailPath == null || thumbnailPath.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Video chưa có thumbnail"));
        }

        // Tạo presigned URL cho thumbnail
        String presignedUrl = r2StorageService.generatePresignedUrl(thumbnailPath, expirationInMinutes);

        return ResponseEntity
                .ok(ApiResponse.success("URL tạm thời cho thumbnail đã được tạo thành công", presignedUrl));
    }

    @Operation(summary = "Lấy thông tin trạng thái xử lý video")
    @GetMapping("/{id}/processing-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> getVideoProcessingStatus(@PathVariable UUID id) {
        log.debug("Lấy thông tin trạng thái xử lý video: {}", id);

        // Lấy thông tin video
        VideoDto video = videoService.getVideo(id);

        if (video.getStatus() == VideoStatus.COMPLETED) {
            return ResponseEntity.ok(ApiResponse.success("Video đã được xử lý hoàn tất",
                    Map.of(
                            "status", video.getStatus(),
                            "progress", 100,
                            "message", "Hoàn thành")));
        } else if (video.getStatus() == VideoStatus.ERROR) {
            return ResponseEntity.ok(ApiResponse.success("Video xử lý bị lỗi",
                    Map.of(
                            "status", video.getStatus(),
                            "progress", video.getProgress() != null ? video.getProgress() : 0,
                            "message", "Lỗi khi xử lý video")));
        }

        // Lấy thông tin trạng thái xử lý gần nhất
        Map<String, Object> processingStatus = videoService.getLatestProcessingStatus(id);

        return ResponseEntity.ok(ApiResponse.success("Thông tin trạng thái xử lý video", processingStatus));
    }
}
