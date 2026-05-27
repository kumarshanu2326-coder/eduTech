package com.edtech.platform.service;

import com.edtech.platform.dto.BannerDto;
import com.edtech.platform.entity.Banner;
import com.edtech.platform.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public List<BannerDto.Response> getActive() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BannerDto.Response> getAll() {
        return bannerRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BannerDto.Response create(BannerDto.CreateRequest req) {
        Banner.BannerType type = Banner.BannerType.GENERAL;
        if (req.getType() != null) {
            try { type = Banner.BannerType.valueOf(req.getType().toUpperCase()); }
            catch (Exception ignored) {}
        }

        Banner banner = Banner.builder()
                .title(req.getTitle())
                .subtitle(req.getSubtitle())
                .ctaText(req.getCtaText())
                .ctaLink(req.getCtaLink())
                .backgroundColor(req.getBackgroundColor())
                .badgeText(req.getBadgeText())
                .type(type)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0)
                .startsAt(req.getStartsAt())
                .endsAt(req.getEndsAt())
                .build();
        return toResponse(bannerRepository.save(banner));
    }

    @Transactional
    public BannerDto.Response update(Long id, BannerDto.CreateRequest req) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found: " + id));
        if (req.getTitle()           != null) b.setTitle(req.getTitle());
        if (req.getSubtitle()        != null) b.setSubtitle(req.getSubtitle());
        if (req.getCtaText()         != null) b.setCtaText(req.getCtaText());
        if (req.getCtaLink()         != null) b.setCtaLink(req.getCtaLink());
        if (req.getBackgroundColor() != null) b.setBackgroundColor(req.getBackgroundColor());
        if (req.getBadgeText()       != null) b.setBadgeText(req.getBadgeText());
        if (req.getIsActive()        != null) b.setIsActive(req.getIsActive());
        if (req.getDisplayOrder()    != null) b.setDisplayOrder(req.getDisplayOrder());
        if (req.getStartsAt()        != null) b.setStartsAt(req.getStartsAt());
        if (req.getEndsAt()          != null) b.setEndsAt(req.getEndsAt());
        return toResponse(bannerRepository.save(b));
    }

    public void delete(Long id) {
        bannerRepository.deleteById(id);
    }

    private BannerDto.Response toResponse(Banner b) {
        return BannerDto.Response.builder()
                .id(b.getId())
                .title(b.getTitle())
                .subtitle(b.getSubtitle())
                .ctaText(b.getCtaText())
                .ctaLink(b.getCtaLink())
                .imagePath(b.getImagePath())
                .backgroundColor(b.getBackgroundColor())
                .badgeText(b.getBadgeText())
                .type(b.getType() != null ? b.getType().name() : "GENERAL")
                .isActive(b.getIsActive())
                .displayOrder(b.getDisplayOrder())
                .startsAt(b.getStartsAt())
                .endsAt(b.getEndsAt())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
