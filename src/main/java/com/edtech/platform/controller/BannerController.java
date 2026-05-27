package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.BannerDto;
import com.edtech.platform.service.BannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;
    public BannerController(BannerService bannerService) { this.bannerService = bannerService; }

    /** Public endpoint — homepage fetches active banners */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerDto.Response>>> active() {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.getActive()));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BannerDto.Response>>> all() {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BannerDto.Response>> create(@RequestBody BannerDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Banner created", bannerService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BannerDto.Response>> update(
            @PathVariable Long id, @RequestBody BannerDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Banner updated", bannerService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Banner deleted", null));
    }
}
