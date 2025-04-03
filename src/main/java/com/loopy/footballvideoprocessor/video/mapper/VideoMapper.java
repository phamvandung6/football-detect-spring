package com.loopy.footballvideoprocessor.video.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.loopy.footballvideoprocessor.video.dto.VideoDto;
import com.loopy.footballvideoprocessor.video.model.Video;
import com.loopy.footballvideoprocessor.video.model.VideoProcessingStatusEntity;

@Component
public class VideoMapper {

    /**
     * Chuyển đổi từ entity Video sang DTO
     * 
     * @param video Entity Video từ cơ sở dữ liệu
     * @return VideoDto
     */
    public VideoDto toDto(Video video) {
        if (video == null) {
            return null;
        }

        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setUserId(video.getUser().getId());
        dto.setUsername(video.getUser().getUsername());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setVideoType(video.getVideoType());
        dto.setFilePath(video.getFilePath());
        dto.setFileSize(video.getFileSize());
        dto.setDuration(video.getDuration());
        dto.setThumbnailPath(video.getThumbnailPath());
        dto.setProcessedPath(video.getProcessedPath());
        dto.setYoutubeUrl(video.getYoutubeUrl());
        dto.setYoutubeVideoId(video.getYoutubeVideoId());
        dto.setIsDownloadable(video.getIsDownloadable());
        dto.setStatus(video.getStatus());
        
        // Lấy tiến độ xử lý từ trạng thái xử lý mới nhất
        if (video.getProcessingStatuses() != null && !video.getProcessingStatuses().isEmpty()) {
            Optional<VideoProcessingStatusEntity> latestStatus = video.getProcessingStatuses().stream()
                    .sorted((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()))
                    .findFirst();
            
            latestStatus.ifPresent(status -> dto.setProgress(status.getProgress()));
        } else {
            dto.setProgress(0);
        }
        
        dto.setCreatedAt(video.getCreatedAt());
        dto.setUpdatedAt(video.getUpdatedAt());
        
        return dto;
    }

    /**
     * Cập nhật entity Video từ DTO
     * 
     * @param video Entity Video cần cập nhật
     * @param dto VideoDto chứa dữ liệu cập nhật
     * @return Video đã cập nhật
     */
    public Video updateEntityFromDto(Video video, VideoDto dto) {
        if (video == null || dto == null) {
            return video;
        }
        
        if (dto.getTitle() != null) {
            video.setTitle(dto.getTitle());
        }
        
        if (dto.getDescription() != null) {
            video.setDescription(dto.getDescription());
        }
        
        if (dto.getIsDownloadable() != null) {
            video.setIsDownloadable(dto.getIsDownloadable());
        }
        
        return video;
    }
} 