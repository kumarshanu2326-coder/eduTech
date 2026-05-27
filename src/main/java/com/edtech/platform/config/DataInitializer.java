package com.edtech.platform.config;

import com.edtech.platform.entity.Banner;
import com.edtech.platform.entity.Test;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.BannerRepository;
import com.edtech.platform.repository.TestRepository;
import com.edtech.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository   userRepository;
    private final BannerRepository bannerRepository;
    private final TestRepository   testRepository;

    public DataInitializer(UserRepository userRepository, BannerRepository bannerRepository,
                           TestRepository testRepository) {
        this.userRepository   = userRepository;
        this.bannerRepository = bannerRepository;
        this.testRepository   = testRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedBanners();
        seedFoundationTests();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@educonnect.in")) return;

        User admin = User.builder()
                .fullName("EduConnect Admin")
                .email("admin@educonnect.in")
                .password("Admin@2026")   // TODO: BCrypt encode when security enabled
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Admin user seeded: admin@educonnect.in / Admin@2026");
    }

    private void seedBanners() {
        if (bannerRepository.count() > 0) return;

        List<Banner> banners = List.of(
                Banner.builder()
                        .title("🚀 AI & Future Skills Bootcamp")
                        .subtitle("Learn Artificial Intelligence from industry experts. Limited seats — register now!")
                        .ctaText("Register Free")
                        .ctaLink("/register")
                        .backgroundColor("linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                        .badgeText("🔥 This Weekend")
                        .type(Banner.BannerType.AI_BOOTCAMP)
                        .isActive(true)
                        .displayOrder(1)
                        .build(),
                Banner.builder()
                        .title("💼 Career Counselling — Free Webinar")
                        .subtitle("Top HR professionals guide you on career choices, resume tips, and interview prep.")
                        .ctaText("Book Free Slot")
                        .ctaLink("/register")
                        .backgroundColor("linear-gradient(135deg, #f093fb 0%, #f5576c 100%)")
                        .badgeText("📅 Saturday 6 PM")
                        .type(Banner.BannerType.WEBINAR)
                        .isActive(true)
                        .displayOrder(2)
                        .build(),
                Banner.builder()
                        .title("🏢 IT Company Workshop Series")
                        .subtitle("Real workshops conducted by professionals from top tech companies. Get certified!")
                        .ctaText("Explore Workshops")
                        .ctaLink("/teachers")
                        .backgroundColor("linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)")
                        .badgeText("✅ Certified")
                        .type(Banner.BannerType.WORKSHOP)
                        .isActive(true)
                        .displayOrder(3)
                        .build(),
                Banner.builder()
                        .title("🎓 Internship Connect Program")
                        .subtitle("Get matched with real internship opportunities at startups and MNCs across India.")
                        .ctaText("Find Internships")
                        .ctaLink("/register")
                        .backgroundColor("linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)")
                        .badgeText("🌟 New Program")
                        .type(Banner.BannerType.INTERNSHIP)
                        .isActive(true)
                        .displayOrder(4)
                        .build()
        );
        bannerRepository.saveAll(banners);
        log.info("Seeded {} sample banners", banners.size());
    }

    private void seedFoundationTests() {
        if (testRepository.count() > 0) return;

        java.util.List<Test> tests = java.util.List.of(
                Test.builder().title("AI Foundations — Know Your Skills").subject("Artificial Intelligence")
                        .category("Future Skills").description("Discover your baseline understanding of AI, machine learning, and what the future holds. Free for all students.")
                        .difficulty("BEGINNER").isFree(true).totalQuestions(20).durationMinutes(25).passingScore(50).displayOrder(1).build(),
                Test.builder().title("Career Readiness Assessment").subject("Career Counselling")
                        .category("Future Skills").description("Find out how well-prepared you are to make smart career decisions. Covers goal setting, industry awareness, and self-awareness.")
                        .difficulty("BEGINNER").isFree(true).totalQuestions(15).durationMinutes(20).passingScore(50).displayOrder(2).build(),
                Test.builder().title("Communication Skills Baseline").subject("Communication Skills")
                        .category("Future Skills").description("Evaluate your written and verbal communication strengths. Identify gaps before they hold you back in your career.")
                        .difficulty("BEGINNER").isFree(true).totalQuestions(20).durationMinutes(25).passingScore(50).displayOrder(3).build(),
                Test.builder().title("Soft Skills & Emotional Intelligence").subject("Soft Skills")
                        .category("Future Skills").description("Leadership, teamwork, conflict resolution — where do you really stand? Take this free self-assessment to find out.")
                        .difficulty("BEGINNER").isFree(true).totalQuestions(18).durationMinutes(22).passingScore(50).displayOrder(4).build(),
                Test.builder().title("Psychology & Mindset Check").subject("Psychology")
                        .category("Future Skills").description("Understand your mental models, biases, and emotional patterns. The first step to personal growth is honest self-assessment.")
                        .difficulty("BEGINNER").isFree(true).totalQuestions(15).durationMinutes(18).passingScore(50).displayOrder(5).build(),
                Test.builder().title("Modern Tech Awareness Test").subject("IT Company Knowledge")
                        .category("Future Skills").description("How aware are you of what leading IT companies do, what tech stacks they use, and what skills they hire for? Find out now.")
                        .difficulty("BEGINNER").isFree(true).totalQuestions(20).durationMinutes(25).passingScore(50).displayOrder(6).build(),
                // Paid advanced tests
                Test.builder().title("Advanced AI & Prompt Engineering").subject("Artificial Intelligence")
                        .category("Future Skills").description("Deep dive into generative AI, prompt engineering, and building AI-powered products. For those ready to go beyond basics.")
                        .difficulty("INTERMEDIATE").isFree(false).price(new java.math.BigDecimal("99")).totalQuestions(30).durationMinutes(45).passingScore(60).displayOrder(7).build(),
                Test.builder().title("Professional Communication Mastery").subject("Communication Skills")
                        .category("Future Skills").description("Advanced assessment covering business writing, presentations, and workplace communication at the professional level.")
                        .difficulty("INTERMEDIATE").isFree(false).price(new java.math.BigDecimal("99")).totalQuestions(25).durationMinutes(35).passingScore(60).displayOrder(8).build()
        );
        testRepository.saveAll(tests);
        log.info("Seeded {} foundation tests", tests.size());
    }
}
