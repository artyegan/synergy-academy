package entities;


import io.vertx.core.json.JsonArray;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.mail.MailClient;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

public class MailVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(MailVerticle.class);
    private MailClient mailClient;

    @Override
    public void start() throws Exception {
        WebClient client = WebClient.create(vertx);

        String md5Hex = DigestUtils
                .md5Hex("o6GCc7OmwAkNCtqVF7qVjpqRJ0IuPrDI" +
                        "yf2cnoKd5dXGf9aUv1gtjt8xeNIGcOQP7sPCXHLZ" +
                        Instant.now().getEpochSecond());

//        client.getAbs("https://api.classmarker.com/v1.json")
//                .addQueryParam("api_key", "o6GCc7OmwAkNCtqVF7qVjpqRJ0IuPrDI")
//                .addQueryParam("signature", md5Hex)
//                .addQueryParam("timestamp", String.valueOf(Instant.now().getEpochSecond()))
//                .rxSend().subscribe(
//                        res -> System.out.println(res.body()),
//                        err -> System.out.println(err)
//                );

        client.getAbs("https://api.classmarker.com/v1/links/1217900/tests/1938918/recent_results.json")
                .addQueryParam("api_key", "o6GCc7OmwAkNCtqVF7qVjpqRJ0IuPrDI")
                .addQueryParam("signature", md5Hex)
                .addQueryParam("timestamp", String.valueOf(Instant.now().getEpochSecond()))
                .addQueryParam("finishedAfterTimestamp", "1650049943")
                .rxSend().subscribe(
                        res -> System.out.println(res.body()),
                        err -> System.out.println(err)
                );

        vertx.eventBus().consumer("send.mail", this::sendMailHandler);

        this.mailClient = MailClient.create(vertx,
                new MailConfig()
                        .setHostname("smtp.mail.ru")
                        .setPort(587)
                        .setAuthMethods("PLAIN")
                        .setUsername("mail@mail.ru")
                        .setPassword("pass")
        );


    }

    private void sendMailHandler(Message<JsonArray> msg) {
        MailMessage message = createMessage(
                "mail@mail.ru",
                msg.body(),
                "Synergy Academy test link",
                "https://www.classmarker.com/online-test/start/?quiz=nde6276ebbcd4a1e"
        );
//test

        mailClient.rxSendMail(message)
                .subscribe(
                        res -> {
                            LOGGER.info("Invitation links sent successfully");
                            LOGGER.info(String.format("Sent message: %s", res));
                            msg.reply(new JsonArray());
                        },
                        err -> {
                            LOGGER.error(err);
                            msg.fail(500, err.getMessage());
                        }
                );
    }

    private MailMessage createMessage(String from, JsonArray to, String subject, String text) {
        return new MailMessage()
                .setFrom(from)
                .setTo(to.stream().map(String.class::cast).toList())
                .setSubject(subject)
                .setText(text);
    }
}
