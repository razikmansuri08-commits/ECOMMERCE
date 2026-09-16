package com.rmtech.ecom.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.rmtech.ecom.Exception.EmailSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    private final Resend resend;

    @Value("${email.from}")
    private String from;

    public EmailService(Resend resend) {
        this.resend = resend;
    }

    public void sendOrderConfirmation(
            String customerEmail,
            String orderId,
            Double totalAmount) {

        String thread = Thread.currentThread().getName();
        log.info("[thread={}] Sending order confirmation email for order: {} to {}",
                thread, orderId, customerEmail);

        String html = """
                <!DOCTYPE html>
                <html>
                <body>

                    <h2>Order Confirmed</h2>

                    <p>Thank you for your order.</p>

                    <p>
                        <strong>Order ID:</strong> %s
                    </p>

                    <p>
                        <strong>Total Amount:</strong> Rs.%.2f
                    </p>

                    <p>
                        Your order has been successfully placed.
                    </p>

                    <p>
                        Regards,<br>
                        RM Tech E-Commerce
                    </p>

                </body>
                </html>
                """.formatted(
                orderId,
                totalAmount
        );

        CreateEmailOptions params =
                CreateEmailOptions.builder()
                        .from(from)
                        .to(customerEmail)
                        .subject("Order Confirmation #" + orderId)
                        .html(html)
                        .build();

        try {
            CreateEmailResponse response =
                    resend.emails().send(params);

            log.info("[thread={}] Email sent for order: {}. Resend ID: {}",
                    Thread.currentThread().getName(), orderId, response.getId());

        } catch (ResendException e) {
            log.error("[thread={}] Failed to send order confirmation email for order: {}",
                    Thread.currentThread().getName(), orderId, e);
            throw new EmailSendException(
                    "Failed to send order confirmation email to " + customerEmail,
                    e
            );
        }
    }
}
