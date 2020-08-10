package ogorkiewicz.jakub.my_blog.service;

import static ogorkiewicz.jakub.my_blog.resource.MailResource.COMMENT_CONFIRMATION_PATH;
import static ogorkiewicz.jakub.my_blog.resource.MailResource.MAIL_PATH;
import static ogorkiewicz.jakub.my_blog.resource.MailResource.POST_CONFIRMATION_PATH;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import lombok.extern.jbosslog.JBossLog;
import ogorkiewicz.jakub.my_blog.dto.MailDto;

@ApplicationScoped
@JBossLog
public class MailService {

    @ConfigProperty(name = "my-blog.server")
    private String server;
    private ReactiveMailer mailer;
    private Configuration cfg;

    private final String templatesDir = "/templates";
    public final static String LOGO = "/images/logo.png";

    public MailService(ReactiveMailer mailer){
        this.cfg = new Configuration(Configuration.VERSION_2_3_30);
        this.cfg.setClassForTemplateLoading(this.getClass(),templatesDir);
        this.cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        this.cfg.setLocale(Locale.ENGLISH);
        this.cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.mailer = mailer;
    }


    public void sendPostConfirmationEmail(String email, String post, Path imagePath, String token){

        HashMap<String,Object> input = new HashMap<>();
        input.put("activationLink",createActivationLink(token,POST_CONFIRMATION_PATH));
        input.put("post",post);

        Writer writer = new StringWriter();
        try {
            Template template = cfg.getTemplate("postConfirmation.html");
            template.process(input,writer);
        } catch (IOException | TemplateException e )  {
            e.printStackTrace();
            log.error("Mail template read operation failed. Mail has not been sent.");
        }

        mailer.send(Mail.withHtml(email,"This is a confirmation email - please do not reply",
                writer.toString())
                .addInlineAttachment("my-blog.jpg",getLogo(),
                        "image/png", "<logo@my-blog>")
                .addInlineAttachment(FilenameUtils.getName(imagePath.toString()), imagePath.toFile(),
                        "image/png", "<post@image>"))
                .subscribeAsCompletionStage();

    }

    public void sendCommentConfirmationEmail(String email, String comment, String postTitle, String token){

        HashMap<String,Object> input = new HashMap<>();
        input.put("activationLink",createActivationLink(token,COMMENT_CONFIRMATION_PATH));
        input.put("comment",comment);

        Writer writer = new StringWriter();
        try {
            Template template = cfg.getTemplate("commentConfirmation.html");
            template.process(input,writer);
        } catch (IOException | TemplateException e )  {
            e.printStackTrace();
            log.error("Mail template read operation failed. Mail has not been sent.");
        }

        mailer.send(Mail.withHtml(email,"This is a confirmation email - please do not reply",
                writer.toString())
                .addInlineAttachment("my-blog.jpg",getLogo(),
                        "image/png", "<logo@my-blog>"))
                .subscribeAsCompletionStage();

    }

    private String createActivationLink(String token, String resourcePath){
        try {
            URL link = new URL(server + MAIL_PATH +
                    resourcePath + "?token=" + token);
            return link.toString();
        } catch (MalformedURLException e) {
            log.error("Mail has been sent with no activation link.");
            return null;
        }
    }

    private byte[] getLogo(){
        try {
            return IOUtils.toByteArray(MailService.class.getResourceAsStream(LOGO));
        } catch (IOException e) {
            log.error("Logo image read operation failed. Mail has not been sent.");
            return null;
        }
    }

    public void sendEmail(MailDto mailDto){
        mailDto.setSubject("Test email");
        HashMap<String,Object> input = new HashMap<>();
        input.put("mailDto",mailDto);

        Writer writer = new StringWriter();
        try {
            Template template = cfg.getTemplate("mail.html");
            template.process(input,writer);
        } catch (IOException | TemplateException e )  {
            e.printStackTrace();
            log.error("Mail template read operation failed. Mail has not been sent.");
        }

        mailer.send(Mail.withHtml(mailDto.getEmail(),"The following mail has been sent to the author of this blog",
                writer.toString())
                .addInlineAttachment("my-blog.jpg",getLogo(),
                        "image/png", "<logo@my-blog>"))
                .subscribeAsCompletionStage();
    }

}

