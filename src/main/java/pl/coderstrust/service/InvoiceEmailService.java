package pl.coderstrust.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.utils.ArgumentValidator;

@Service
public class InvoiceEmailService {

    private InvoicePdfService invoicePdfService;
    private JavaMailSender emailSender;
    private MailProperties mailProperties;
    private static Logger log = LoggerFactory.getLogger(InvoiceEmailService.class);

    @Autowired
    public InvoiceEmailService(InvoicePdfService invoicePdfService, JavaMailSender emailSender, MailProperties mailProperties) {
        ArgumentValidator.ensureNotNull(invoicePdfService, "invoicePdfService");
        ArgumentValidator.ensureNotNull(emailSender, "emailSender");
        ArgumentValidator.ensureNotNull(mailProperties, "mailProperties");
        this.invoicePdfService = invoicePdfService;
        this.emailSender = emailSender;
        this.mailProperties = mailProperties;
    }

    @Async
    public void sendEmailWithInvoice(Invoice invoice) {
        ArgumentValidator.ensureNotNull(invoice, "Invoice");
        log.debug("Sending an e-mail with invoice: {}", invoice);
        try {
            MimeMessage email = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(email, true);
            helper.addAttachment(String.format("%s.pdf", invoice.getNumber()), new ByteArrayResource(invoicePdfService.getInvoiceAsPdf(invoice)));
            helper.setTo(mailProperties.getProperties().get("receiver"));
            helper.setFrom(mailProperties.getUsername());
            helper.setSubject(String.format("Invoice nr: %s", invoice.getNumber()));
            helper.setText("Please see attachments for your saved invoice");
            emailSender.send(email);
        } catch (ServiceOperationException | MessagingException e) {
            log.error("An error occurred during sending an e-mail with invoice.", e);
        }
    }
}
