package com.rmtech.ecom.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;

public class EmailService { private final Resend resend;

    @Value("${email.from}")
    private String from;

    public EmailService(Resend resend) {
        this.resend = resend;
    }

    public void sendOrderConfirmation(
            String customerEmail,
            String orderId,
            Double totalAmount) {

        String html = """
                <!DOCTYPE html>
                <html>
                <body>

                    <h2>Order Confirmed 🎉</h2>

                    <p>Thank you for your order.</p>

                    <p>
                        <strong>Order ID:</strong> %d
                    </p>

                    <p>
                        <strong>Total Amount:</strong> ₹%.2f
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

            System.out.println(
                    "Email sent. ID: " + response.getId()
            );

        } catch (ResendException e) {

            throw new RuntimeException(
                    "Failed to send order confirmation email",
                    e
            );
        }
    }
}
