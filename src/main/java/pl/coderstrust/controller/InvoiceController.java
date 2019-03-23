package pl.coderstrust.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.service.InvoiceService;
import pl.coderstrust.utils.ArgumentValidator;

@RestController
@RequestMapping("/invoices")
@Api(tags = "Faktury", description = "Operacje na fakturach")
public class InvoiceController {

    private InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        ArgumentValidator.ensureNotNull(invoiceService, "invoiceService");
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Get a single invoice", notes = "Gets an invoice by id")
    ResponseEntity<?> getInvoiceById(@PathVariable Long id) {
        try {
            Optional<Invoice> invoice = invoiceService.getInvoice(id);
            if (!invoice.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok().body(invoice.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping()
    @ApiOperation(value = "Get all invoices", notes = "Gets all available invoices from database")
    ResponseEntity<?> getAllInvoices() {
        try {
            Collection<Invoice> invoices = invoiceService.getAllInvoices();
            return ResponseEntity.status(HttpStatus.OK).body(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byDate")
    @ApiOperation(value = "Get all invoices by dates", notes = "Gets all invoices issued between specified dates (inclusive) fromDate and toDate")
    ResponseEntity<?> getInvoicesByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (fromDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fromDate parameter cannot be null.");
        }
        if (toDate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("toDate parameter cannot be null.");
        }
        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fromDate cannot be after toDate.");
        }
        try {
            Collection<Invoice> invoices = invoiceService.getAllInvoicesByDate(fromDate, toDate);
            return ResponseEntity.status(HttpStatus.OK).body(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byBuyer")
    @ApiOperation(value = "Get all invoices by buyer", notes = "Gets all invoices issued to specified buyer")
    ResponseEntity<?> getInvoicesByBuyer(@RequestParam Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("id cannot be null.");
        }
        try {
            Collection<Invoice> invoices = invoiceService.getAllInvoicesByBuyer(id);
            return ResponseEntity.status(HttpStatus.OK).body(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bySeller")
    @ApiOperation(value = "Get all invoices by seller", notes = "Gets all invoices issued to specified seller")
    ResponseEntity<?> getInvoicesBySeller(@RequestParam Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("id cannot be null.");
        }
        try {
            Collection<Invoice> invoices = invoiceService.getAllInvoicesBySeller(id);
            return ResponseEntity.status(HttpStatus.OK).body(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete invoice by id", notes = "Deletes invoice by specified id from database")
    ResponseEntity<?> deleteInvoice(@PathVariable Long id) {
        try {
            Optional<Invoice> invoice = invoiceService.getInvoice(id);
            if (!invoice.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            invoiceService.deleteInvoice(id);
            return ResponseEntity.status(HttpStatus.OK).body(invoice.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    @ApiOperation(value = "Delete ALL invoices", notes = "WARNING!!! This operation deletes ALL available invoices from database")
    ResponseEntity<?> deleteAllInvoices() {
        try {
            invoiceService.deleteAllInvoices();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @ApiOperation(value = "Save or update an invoice", notes = "When id field left blank - saves an invoice to database. When id field filled with number - then application updates the existing invoice with form data")
    ResponseEntity<?> saveInvoice(@RequestBody(required = false) Invoice invoice) {
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invoice cannot be null.");
        }
        try {
            Invoice savedInvoice = invoiceService.saveInvoice(invoice);
            return ResponseEntity.status(HttpStatus.OK).body(savedInvoice);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
