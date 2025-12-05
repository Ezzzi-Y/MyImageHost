package me.ezzi.myimagehostbackend.service.impl;

import cn.hutool.core.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.constant.MessageConstant;
import me.ezzi.myimagehostbackend.common.constant.RedisConstant;
import me.ezzi.myimagehostbackend.exception.BaseException;
import me.ezzi.myimagehostbackend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final Duration VERIFICATION_EXPIRE = Duration.ofMinutes(30);
    private static final Duration RATE_LIMIT_EXPIRE = Duration.ofMinutes(2);

    @Override
    public void sendVerificationCode(String email) {
        Assert.hasText(email, MessageConstant.LACK_PARAM);

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        
        // 1. 频率限制检查 (120s)
        String rateLimitKey = RedisConstant.EMAIL_RATE_LIMIT_KEY + email;
        if (ops.get(rateLimitKey) != null) {
            throw new BaseException(MessageConstant.TOO_QUICKLY);
        }

        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 渲染邮件内容
        Context context = new Context();
        context.setVariable("code", code);
        String htmlContent = templateEngine.process("verification", context);

        // 4. 发送邮件
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("邮箱验证码");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("验证码已发送: {} -> {}", email, code);

            // 5. 存入 Redis (仅在发送成功后)
            // 保存验证码，有效期 30 分钟
            ops.set(RedisConstant.EMAIL_CODE_KEY + email, code, VERIFICATION_EXPIRE);
            // 设置频率限制，有效期 2 分钟
            ops.set(rateLimitKey, "1", RATE_LIMIT_EXPIRE);

        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", email, e);
            throw new BaseException(MessageConstant.EMAIL_SEND_FAILED+e.getMessage());
        }
    }

    @Override
    public boolean validVerificationCode(String email, String verificationCode) {
        Assert.hasText(email, MessageConstant.LACK_PARAM);
        Assert.hasText(verificationCode, MessageConstant.LACK_PARAM);

        String key = RedisConstant.EMAIL_CODE_KEY + email;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            log.warn("验证码不存在或已过期: {}", email);
            return false;
        }

        boolean isValid = value.toString().equals(verificationCode);
        if (!isValid) {
            log.debug("验证码错误: {}, 收到的验证码：{}, 正确的验证码：{}", email, verificationCode, value);
        } else {
            // 验证成功后删除验证码，防止重复使用
            redisTemplate.delete(key);
        }
        return isValid;
    }
}
