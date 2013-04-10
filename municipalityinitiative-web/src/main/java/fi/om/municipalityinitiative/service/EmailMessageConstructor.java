package fi.om.municipalityinitiative.service;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mysema.commons.lang.Assert;
import fi.om.municipalityinitiative.conf.EmailSettings;
import fi.om.municipalityinitiative.newdto.email.CollectableInitiativeEmailInfo;
import fi.om.municipalityinitiative.pdf.ParticipantToPdfExporter;
import fi.om.municipalityinitiative.util.Maybe;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class EmailMessageConstructor {

    private static final String FILE_NAME = "Kuntalaisaloite_{0}_{1}_osallistujat.pdf";

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private EmailSettings emailSettings;

    @Resource
    private FreeMarkerConfigurer freemarkerConfig;

    private static final Logger log = LoggerFactory.getLogger(EmailMessageConstructor.class);

    static void addAttachment(MimeMessageHelper multipart, CollectableInitiativeEmailInfo emailInfo) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ParticipantToPdfExporter.createPdf(emailInfo, outputStream);

            byte[] bytes = outputStream.toByteArray();
            DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");

            String fileName = MessageFormat.format(FILE_NAME, new LocalDate().toString("yyyy-MM-dd"), emailInfo.getId());

            multipart.addAttachment(fileName, dataSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MimeMessageHelper parseBasicEmailData(String sendTo, String subject, String templateName, Map<String, Object> dataMap) {

        String text = processTemplate(templateName + "-text", dataMap);
        String html = processTemplate(templateName + "-html", dataMap);

        text = stripTextRows(text, 2);

        if (emailSettings.getTestSendTo().isPresent()) {
            text = "TEST OPTION REPLACED THE EMAIL ADDRESS!\nThe original address was: " + sendTo + "\n\n\n-------------\n" + text;
            html = "TEST OPTION REPLACED THE EMAIL ADDRESS!\nThe original address was: " + sendTo + "<hr>" + html;
            sendTo = emailSettings.getTestSendTo().get();
        }

        Assert.notNull(sendTo, "sendTo");

        if (emailSettings.isTestConsoleOutput()) {
            System.out.println("----------------------------------------------------------");
            System.out.println("To: " + sendTo);
            System.out.println("Reply-to: " + emailSettings.getDefaultReplyTo());
            System.out.println("Subject: " + subject);
            System.out.println("---");
            System.out.println(text);
            System.out.println("----------------------------------------------------------");
            return null; //FIXME: May cause nullpointer exception
        }

        try {
            MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true, "UTF-8");
            helper.setTo(sendTo);
            helper.setFrom(emailSettings.getDefaultReplyTo());
            helper.setReplyTo(emailSettings.getDefaultReplyTo());
            helper.setSubject(subject);
            helper.setText(text, html);
            log.info("About to send email to " + sendTo + ": " + subject);
            return helper;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private static String stripTextRows(String text, int maxEmptyRows) {
        List<String> rows = Lists.newArrayList(Splitter.on('\n').trimResults().split(text));

        int emptyRows = maxEmptyRows;
        for (int i = rows.size()-1; i >= 0; i--) {
            if (Strings.isNullOrEmpty(rows.get(i))) {
                emptyRows++;
            } else {
                emptyRows = 0;
            }
            if (emptyRows > maxEmptyRows) {
                rows.remove(i);
            }
        }

        //remove remaining empty rows from the beginning
        while (rows.size() > 0 && Strings.isNullOrEmpty(rows.get(0))) {
            rows.remove(0);
        }

        return Joiner.on("\r\n").join(rows);
    }

    private String processTemplate(String templateName, Map<String, Object> dataMap) {
        final Configuration cfg = freemarkerConfig.getConfiguration();

        try {
            Template template;
            template = cfg.getTemplate("emails/" + templateName + ".ftl");
            Writer out = new StringWriter();
            template.process(dataMap, out);
            out.flush();
            return out.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public EmailMessageDraft fromTemplate(String templateName) {
        return new EmailMessageDraft(templateName);
    }

    public class EmailMessageDraft {
        private String sendTo;
        private String subject;
        private String templateName;
        private Map<String, Object> dataMap;
        private Maybe<CollectableInitiativeEmailInfo> attachmentEmailInfo = Maybe.absent();

        public EmailMessageDraft(String templateName) {
            this.templateName = templateName;
        }

        public EmailMessageDraft withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailMessageDraft withSendTo(String sendTo) {
            this.sendTo = sendTo;
            return this;
        }
        
        public EmailMessageDraft withSendToModerator() {
            this.sendTo = emailSettings.getModeratorEmail();
            return this;
        }

        public EmailMessageDraft withDataMap(Map<String, Object> dataMap) {
            this.dataMap = dataMap;
            return this;
        }

        public EmailMessageDraft withAttachment(CollectableInitiativeEmailInfo attachmentEmailInfo) {
            this.attachmentEmailInfo = Maybe.of(attachmentEmailInfo);
            return this;
        }

        public void send() {

            Assert.notNull(sendTo, "sendTo");
            Assert.notNull(subject, "subject");
            Assert.notNull(templateName, "templateName");
            Assert.notNull(dataMap, "dataMap");

            MimeMessageHelper mimeMessageHelper = parseBasicEmailData(sendTo, subject, templateName, dataMap);
            if (attachmentEmailInfo.isPresent()) {
                addAttachment(mimeMessageHelper, attachmentEmailInfo.get());
            }

            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        }
    }
}