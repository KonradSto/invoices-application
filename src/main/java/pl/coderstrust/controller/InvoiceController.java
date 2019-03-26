package pl.coderstrust.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.service.InvoiceService;
import pl.coderstrust.utils.ArgumentValidator;

@RestController
@RequestMapping("/invoices")
@Api(tags = "Invoices", description = "Operations")
public class InvoiceController {

    private InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        ArgumentValidator.ensureNotNull(invoiceService, "invoiceService");
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get a single invoice", notes = "Gets an invoice by id")
    @ApiResponses({
        @ApiResponse(code = 404, message = "Invoice with given id does not exist in database."),
        @ApiResponse(code = 200, message = "Success. Invoice retrieved from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
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
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get all invoices", notes = "Gets all available invoices from database.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Success. All invoices retrieved from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
    ResponseEntity<?> getAllInvoices() {
        try {
            Collection<Invoice> invoices = invoiceService.getAllInvoices();
            return ResponseEntity.status(HttpStatus.OK).body(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/byDate")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get all invoices by dates", notes = "Gets all invoices issued between specified dates (inclusive) fromDate and toDate.")
    @ApiResponses({
        @ApiResponse(code = 400, message = "Please make sure that fromDate  and toDate parameters are present and in the correct format ie. YYYY.MM.DD. Make sure toDate parameter is after toDate parameter."),
        @ApiResponse(code = 200, message = "Success. All invoices issued within given dates retrieved from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
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
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get all invoices by buyer", notes = "Gets all invoices issued to specified buyer.")
    @ApiResponses({
        @ApiResponse(code = 400, message = "Please provide buyerId parameter."),
        @ApiResponse(code = 200, message = "Success. All invoices issued to given buyer retrieved from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
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
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get all invoices by seller", notes = "Gets all invoices issued to specified seller.")
    @ApiResponses({
        @ApiResponse(code = 400, message = "Please provide sellerId parameter."),
        @ApiResponse(code = 200, message = "Success. All invoices issued to given seller retrieved from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
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
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete an invoice by id", notes = "Deletes invoice by specified id from database.")
    @ApiResponses({
        @ApiResponse(code = 404, message = "Invoice with given id does not exist in database."),
        @ApiResponse(code = 200, message = "Success. Invoice with given id deleted from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete ALL invoices", notes = "WARNING!!! This operation deletes ALL available invoices from database.")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Success. All invoices deleted from database."),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
    ResponseEntity<?> deleteAllInvoices() {
        try {
            invoiceService.deleteAllInvoices();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Save or update an invoice", notes = "When invoice id field is set to null - application saves the invoice to database under id which is automatically generated to that invoice. When id field filled with number - then "
        + "application assumes that the user wants to update the invoice but before proceeding with update- checks if given id exists in database, if so then updates the existing invoice with form data, otherwise 500 error is returned.")
    @ApiResponses({
        @ApiResponse(code = 400, message = "invoice cannot be null."),
        @ApiResponse(code = 200, message = "Success. Invoice saved/updated in database"),
        @ApiResponse(code = 500, message = "Something went wrong on the server.")})
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
