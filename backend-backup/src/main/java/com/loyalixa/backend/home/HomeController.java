package com.loyalixa.backend.home;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.loyalixa.backend.content.dto.HomeResponse;
@RestController
@RequestMapping("/api/v1/home")
public class HomeController {
    private final HomeService homeService;
    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }
    @GetMapping
    public ResponseEntity<HomeResponse> getHomeData() {
        HomeResponse response = homeService.getHomePageData();
        return ResponseEntity.ok(response);
    }
}