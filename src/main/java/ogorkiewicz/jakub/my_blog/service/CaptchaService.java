package ogorkiewicz.jakub.my_blog.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import ogorkiewicz.jakub.my_blog.exception.ErrorCode;
import ogorkiewicz.jakub.my_blog.exception.MyBlogException;
import ogorkiewicz.jakub.my_blog.model.Captcha;
import ogorkiewicz.jakub.my_blog.repository.CaptchaRepository;

@ApplicationScoped
@JBossLog
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@Inject))
public class CaptchaService {

    private CaptchaRepository captchaRepository;

    final int imageWidth = 300;
    final int imageHeight = 100;
    final int padding = 20;
    final String fontFamily = "DejaVu Serif";
    final char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPRSTUVWXYZ0123456789".toCharArray();

    public InputStream createCaptcha(String captchaKey) throws MyBlogException {
        char[] captchaCharArray = generateCaptchaCharArray();
        ThreadLocalRandom tlr = ThreadLocalRandom.current();

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(generateColor());
        g2d.fillRect(0, 0, imageWidth, imageHeight);

        int x = padding;

        for (int i = 0; i < 4; i++) {
            g2d.setColor(generateColor());
            int fontSize = tlr.nextInt(20, 80);
            int y = tlr.nextInt(fontSize, imageHeight - padding);
            double rotation = tlr.nextDouble(-Math.toRadians(90), Math.toRadians(90));
            Font font = new Font(fontFamily, Font.PLAIN, fontSize);
            g2d.setFont(font);
            int fontHeight = g2d.getFontMetrics().getHeight();
            g2d.translate(x, y);
            g2d.rotate(rotation);
            g2d.setColor(generateColor());
            g2d.drawString(Character.toString(captchaCharArray[i]), 0, 0);
            g2d.rotate(-rotation);
            g2d.translate(-x, -y);
            x = x + fontHeight;
        }

        Captcha captcha = captchaRepository.getCaptchaByKey(captchaKey);
        if (captcha == null) {
            captchaRepository.persist(new Captcha(String.valueOf(captchaCharArray), captchaKey));
        } else {
            captcha.setCaptcha(String.valueOf(captchaCharArray));
        }

        g2d.dispose();

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            log.error("Unable to save file" + e.getMessage());
            throw new MyBlogException(ErrorCode.READ_WRITE_ERROR, Captcha.class);
        }
    }

    public InputStream confirmCaptcha(String captchaText, String captchaKey) throws MyBlogException {
        Captcha captcha = captchaRepository.getCaptchaByKey(captchaKey);
        if (captcha != null) {
            if (captcha != null && captchaText.equals(captcha.getCaptcha())) {
                captchaRepository.deleteById(captcha.getId());
                return null;
            } else {
                return createCaptcha(captchaKey);
            }
        } else {
            throw new MyBlogException(ErrorCode.NOT_EXIST, Captcha.class);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    private void purgeCaptchas() {
        captchaRepository.deleteAll();
    }

    private char[] generateCaptchaCharArray() {
        char[] captchaCharArray = new char[4];
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        for (int i = 0; i < 4; i++) {
            captchaCharArray[i] = alphabet[tlr.nextInt(alphabet.length)];
        }
        return captchaCharArray;
    }

    private Color generateColor() {
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        float r = tlr.nextFloat();
        float g = tlr.nextFloat();
        float b = tlr.nextFloat();
        return new Color(r, g, b);
    }

}