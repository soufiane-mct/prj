package com.aliboo.book.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async //hna bch usern mytsnash 9bl miytsiftsh l msg bsh tkhdm biha sir l application o dir EnableAsync

    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String accontActivation) throws MessagingException {
        String templateName;
        if(emailTemplate == null) {
            templateName = "confirm-email"; //lmkntsh tl3 lia hdi
        } else {
            templateName = emailTemplate.name();  //knt jib lia name mn enum emailTemplate o templateName khdmna biha ltht likat confirmi l mail
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage(); //hna kn configiw mailsender
        MimeMessageHelper helper = new MimeMessageHelper(
          mimeMessage,
          MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name() //hdi d incoding
        );
        //hna anpassiw prameters l email template lihia html template
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        //hd context mn thymleaf
        Context context = new Context();
        context.setVariables(properties); //haka bsh katsift properties l context (html)

        //hna andiro properties l email
        helper.setFrom("contact@gmail.com");//hna fin atsift
        helper.setTo(to); //hna lmn bghit nsift
        helper.setSubject("Account Activation - Please Confirm");//hna wst a9wass kn a subject 2:40:07 t9d tbdl err

        String template = templateEngine.process(templateName, context); //hna kn prosesiw template o templateName katkonfirmi lina mail var drnaha fo9
        helper.setText(template, true); //hna katgol if kn html template wl la

        mailSender.send(mimeMessage); //hna sift lia l mimmsg li 9adina fo9




    }
}
