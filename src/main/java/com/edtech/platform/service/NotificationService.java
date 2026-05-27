package com.edtech.platform.service;

import com.edtech.platform.entity.*;
import com.edtech.platform.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${resend.api.key:}") private String resendApiKey;
    @Value("${resend.from.email:noreply@educonnect.in}") private String fromEmail;
    @Value("${resend.from.name:EduConnect}") private String fromName;

    private final NotificationRepository notifRepo;
    private final RestTemplate rest = new RestTemplate();

    public NotificationService(NotificationRepository notifRepo) {
        this.notifRepo = notifRepo;
    }

    // ── Public trigger methods ────────────────────────────────────────────────

    @Async
    public void sendDemoApproved(String toEmail, String studentName,
                                 String teacherName, String subject, String meetingLink) {
        String subj = "✅ " + teacherName + " Accepted Your Demo Request";
        String body = wrap("""
            <h2 style="color:#1A1535">%s Accepted Your Request! ✅</h2>
            <p style="color:#4A4470">Hi <strong>%s</strong>,</p>
            <p style="color:#4A4470"><strong>%s</strong> has accepted your demo class request for <strong>%s</strong>.</p>
            %s
            <p style="color:#9B97B8;font-size:0.85rem;margin-top:16px">Check your dashboard for full details.</p>
            """.formatted(teacherName, studentName, teacherName, subject,
                meetingLink != null && !meetingLink.isBlank()
                        ? "<a href=\"" + meetingLink + "\" style=\"display:inline-block;background:linear-gradient(135deg,#6C63FF,#A084FF);color:#fff;padding:12px 24px;border-radius:50px;text-decoration:none;font-weight:700;margin:12px 0\">Join Class →</a>"
                        : "<p style='color:#F59E0B'>⏳ The teacher will share the meeting link shortly.</p>"));
        send(toEmail, subj, body, Notification.NotificationType.DEMO_APPROVED);
    }

    @Async
    public void sendTeacherApproved(String toEmail, String teacherName) {
        String subj = "🎉 Your EduConnect Profile is Now Live!";
        String body = wrap("""
            <h2 style="color:#00B894">Congratulations, %s! Your Profile is Live 🎉</h2>
            <p style="color:#4A4470">Your EduConnect teacher profile has been <strong>approved</strong> and is now visible to thousands of students across India.</p>
            <div style="background:#D5F7EF;border-radius:12px;padding:16px;margin:16px 0">
              <p style="color:#065F46;margin:5px 0">✅ Profile is live and discoverable</p>
              <p style="color:#065F46;margin:5px 0">✅ Students can now send you demo requests</p>
              <p style="color:#065F46;margin:5px 0">✅ Complete your profile to get more leads</p>
            </div>
            <a href="https://educonnect.in/teacher" style="display:inline-block;background:linear-gradient(135deg,#6C63FF,#A084FF);color:#fff;padding:12px 24px;border-radius:50px;text-decoration:none;font-weight:700">Go to Dashboard →</a>
            """.formatted(teacherName));
        send(toEmail, subj, body, Notification.NotificationType.TEACHER_APPROVED);
    }

    @Async
    public void sendTeacherRejected(String toEmail, String teacherName, String reason) {
        String subj = "EduConnect Profile Review Update";
        String body = wrap("""
            <h2 style="color:#1A1535">Profile Review Update</h2>
            <p style="color:#4A4470">Hi <strong>%s</strong>,</p>
            <p style="color:#4A4470">Thank you for registering. We need a few more details before approving your profile.</p>
            %s
            <p style="color:#4A4470">Please update your profile/documents and we'll re-review within 24 hours.</p>
            <a href="https://educonnect.in/teacher/docs" style="display:inline-block;background:#6C63FF;color:#fff;padding:12px 24px;border-radius:50px;text-decoration:none;font-weight:700;margin:12px 0">Update Documents →</a>
            """.formatted(teacherName,
                reason != null && !reason.isBlank()
                        ? "<div style='background:#FEF2F2;border-radius:12px;padding:14px;margin:14px 0'><strong style='color:#C0392B'>Reason:</strong> <span style='color:#7B241C'>" + reason + "</span></div>"
                        : ""));
        send(toEmail, subj, body, Notification.NotificationType.TEACHER_REJECTED);
    }

    @Async
    public void sendWelcomeStudent(String toEmail, String name) {
        String subj = "Welcome to EduConnect, " + name.split(" ")[0] + "! 🚀";
        String body = wrap("""
            <h2 style="color:#1A1535">Welcome to EduConnect! 🚀</h2>
            <p style="color:#4A4470">Hi <strong>%s</strong>, you've joined India's future skills platform.</p>
            <div style="background:#F3F2FF;border-radius:12px;padding:20px;margin:16px 0">
              <p style="color:#1A1535;margin:7px 0">🔍 <strong>Find Teachers</strong> — Browse verified teachers by subject and city</p>
              <p style="color:#1A1535;margin:7px 0">🧪 <strong>Free Skill Tests</strong> — Know your level instantly</p>
              <p style="color:#1A1535;margin:7px 0">🎯 <strong>Career Guidance</strong> — Get matched with the right mentor</p>
            </div>
            <a href="https://educonnect.in/skill-tests" style="display:inline-block;background:linear-gradient(135deg,#6C63FF,#A084FF);color:#fff;padding:12px 24px;border-radius:50px;text-decoration:none;font-weight:700">Take Free Skill Test →</a>
            """.formatted(name));
        send(toEmail, subj, body, Notification.NotificationType.WELCOME_STUDENT);
    }

    @Async
    public void sendWelcomeTeacher(String toEmail, String name) {
        String subj = "Welcome to EduConnect — Start Your Teaching Journey!";
        String body = wrap("""
            <h2 style="color:#1A1535">Welcome, %s! 🎓</h2>
            <p style="color:#4A4470">Thank you for joining EduConnect as a teacher. Here's how to get started:</p>
            <div style="background:#F3F2FF;border-radius:12px;padding:20px;margin:16px 0">
              <p style="color:#1A1535;margin:7px 0">📋 <strong>Step 1:</strong> Complete your profile</p>
              <p style="color:#1A1535;margin:7px 0">📄 <strong>Step 2:</strong> Upload your documents</p>
              <p style="color:#1A1535;margin:7px 0">✅ <strong>Step 3:</strong> Get approved (24–48 hrs)</p>
              <p style="color:#1A1535;margin:7px 0">🚀 <strong>Step 4:</strong> Start receiving student leads!</p>
            </div>
            <a href="https://educonnect.in/teacher/profile" style="display:inline-block;background:linear-gradient(135deg,#6C63FF,#A084FF);color:#fff;padding:12px 24px;border-radius:50px;text-decoration:none;font-weight:700">Complete Profile →</a>
            """.formatted(name));
        send(toEmail, subj, body, Notification.NotificationType.WELCOME_TEACHER);
    }

    @Async
    public void sendTestResult(String toEmail, String name, String testTitle,
                               int score, int passing, boolean passed) {
        String subj = (passed ? "🎉 " : "📊 ") + "Your Result: " + testTitle;
        String body = wrap("""
            <h2 style="color:%s">%s</h2>
            <p style="color:#4A4470">Hi <strong>%s</strong>, your results for <strong>%s</strong>:</p>
            <div style="background:%s;border-radius:12px;padding:28px;text-align:center;margin:20px 0">
              <div style="font-size:3rem;font-weight:900;color:%s">%d%%</div>
              <div style="color:%s;font-weight:700;font-size:1.1rem;margin-top:4px">%s</div>
              <div style="color:#9B97B8;margin-top:6px;font-size:0.85rem">Passing score: %d%%</div>
            </div>
            <a href="https://educonnect.in/skill-tests" style="display:inline-block;background:linear-gradient(135deg,#6C63FF,#A084FF);color:#fff;padding:12px 24px;border-radius:50px;text-decoration:none;font-weight:700">%s</a>
            """.formatted(
                passed?"#00B894":"#F59E0B",
                passed?"Congratulations, you passed! 🎉":"Keep going — you're improving! 💪",
                name, testTitle,
                passed?"#D5F7EF":"#FFF8E1",
                passed?"#00796B":"#9A6700",
                score,
                passed?"#00796B":"#9A6700",
                passed?"✅ PASSED":"Keep Practicing",
                passing,
                passed?"Explore More Tests →":"Try Again →"));
        send(toEmail, subj, body, Notification.NotificationType.TEST_COMPLETED);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void send(String toEmail, String subject, String htmlBody,
                      Notification.NotificationType type) {
        Notification n = notifRepo.save(Notification.builder()
                .email(toEmail).subject(subject).body(htmlBody)
                .type(type).channel(Notification.Channel.EMAIL)
                .status(Notification.Status.PENDING).build());

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("RESEND_API_KEY not set — email skipped for {}", toEmail);
            n.setStatus(Notification.Status.FAILED);
            n.setErrorMessage("API key not configured");
            notifRepo.save(n); return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);
            Map<String,Object> payload = Map.of(
                    "from",    fromName + " <" + fromEmail + ">",
                    "to",      new String[]{toEmail},
                    "subject", subject,
                    "html",    htmlBody);
            ResponseEntity<String> resp = rest.postForEntity(
                    "https://api.resend.com/emails",
                    new HttpEntity<>(payload, headers), String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                n.setStatus(Notification.Status.SENT);
                n.setSentAt(LocalDateTime.now());
                log.info("Email sent ✓ → {}", toEmail);
            } else {
                n.setStatus(Notification.Status.FAILED);
                n.setErrorMessage("HTTP " + resp.getStatusCode());
            }
        } catch (Exception e) {
            n.setStatus(Notification.Status.FAILED);
            n.setErrorMessage(e.getMessage());
            log.error("Email failed for {}: {}", toEmail, e.getMessage());
        }
        notifRepo.save(n);
    }

    private String wrap(String content) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#fff;border-radius:16px;overflow:hidden;border:1px solid #E8E6FF">
              <div style="background:linear-gradient(135deg,#6C63FF,#764ba2);padding:24px 32px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:1.3rem;font-weight:800">✦ EduConnect</h1>
                <p style="color:rgba(255,255,255,0.8);margin:4px 0 0;font-size:0.8rem">India's Future Skills Platform</p>
              </div>
              <div style="padding:28px 32px">""" + content + """
              </div>
              <div style="background:#F8F7FF;padding:14px 32px;text-align:center;font-size:0.72rem;color:#9B97B8">
                EduConnect · Bhopal, MP · <a href="https://educonnect.in" style="color:#6C63FF">educonnect.in</a>
              </div>
            </div>""";
    }
}