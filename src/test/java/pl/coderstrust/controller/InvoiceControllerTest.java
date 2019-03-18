package pl.coderstrust.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.coderstrust.generators.InvoiceGenerator;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.service.InvoiceService;
import pl.coderstrust.service.ServiceOperationException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc
class InvoiceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnInvoice() throws Exception {
        //Given
        Invoice invoice = InvoiceGenerator.getRandomInvoice();
        when(invoiceService.getInvoice(1L)).thenReturn(Optional.ofNullable(invoice));

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/{id}", 1L).accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        Invoice actualInvoice = mapper.readValue(result.getResponse().getContentAsString(), Invoice.class);

        //Then
        assertEquals(HttpStatus.OK.value(), actualHttpStatus);
        assertEquals(invoice, actualInvoice);
        verify(invoiceService).getInvoice(1L);
    }

    @Test
    void shouldReturnNotFoundDuringGettingInvoiceWhenInvoiceDoesNotExist() throws Exception {
        //Given
        Long nonExistentId = 10L;
        when(invoiceService.getInvoice(nonExistentId)).thenReturn(Optional.empty());

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/{id}", nonExistentId).accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.NOT_FOUND.value(), actualHttpStatus);
        verify(invoiceService).getInvoice(nonExistentId);
    }

    @Test
    void shouldReturnInternalServerErrorDuringGettingInvoiceWhenSomethingWentWrongOnServer() throws Exception {
        //Given
        Long id = 10L;
        when(invoiceService.getInvoice(id)).thenThrow(ServiceOperationException.class);

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/{id}", id).accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).getInvoice(id);
    }

    @Test
    void shouldReturnAllInvoices() throws Exception {
        //Given
        Invoice invoice1 = InvoiceGenerator.getRandomInvoice();
        Invoice invoice2 = InvoiceGenerator.getRandomInvoice();
        Collection<Invoice> invoices = Arrays.asList(invoice1, invoice2);
        when(invoiceService.getAllInvoices()).thenReturn(invoices);

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices").accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        List<Invoice> actualInvoices = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {
        });

        //Then
        assertEquals(HttpStatus.OK.value(), actualHttpStatus);
        assertEquals(invoices, actualInvoices);
        verify(invoiceService).getAllInvoices();
    }

    @Test
    void shouldReturnInternalServerErrorDuringGettingAllInvoicesWhenSomethingWentWrongOnServer() throws Exception {
        //Given
        when(invoiceService.getAllInvoices()).thenThrow(ServiceOperationException.class);

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices").accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).getAllInvoices();
    }

    @Test
    void shouldReturnAllInvoicesIssuedWithinGivenDates() throws Exception {
        //Given
        String fromDate = "2018-01-01";
        String toDate = "2018-01-31";
        Invoice invoice1 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse("2017-04-19"));
        Invoice invoice2 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse("2017-12-31"));
        Invoice invoice3 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse(fromDate));
        Invoice invoice4 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse("2018-01-15"));
        Invoice invoice5 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse(toDate));
        Invoice invoice6 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse("2018-02-01"));
        Invoice invoice7 = InvoiceGenerator.getRandomInvoiceWithSpecificIssueDate(LocalDate.parse("2018-04-24"));
        List<Invoice> expected = Arrays.asList(invoice3, invoice4, invoice5);
        when(invoiceService.getAllInvoicesByDate(LocalDate.parse(fromDate), LocalDate.parse(toDate))).thenReturn(expected);

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/byDate")
                .param("fromDate", fromDate)
                .param("toDate", toDate)
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        List<Invoice> actualInvoices = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {
        });

        //Then
        assertEquals(HttpStatus.OK.value(), actualHttpStatus);
        assertEquals(expected, actualInvoices);
        verify(invoiceService).getAllInvoicesByDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @Test
    void shouldReturnInternalServerErrorDuringGettingAllInvoicesByDateWhenSomethingWentWrongOnServer() throws Exception {
        //Given
        String fromDate = "2018-01-01";
        String toDate = "2018-01-31";
        when(invoiceService.getAllInvoicesByDate(LocalDate.parse(fromDate), LocalDate.parse(toDate))).thenThrow(ServiceOperationException.class);

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/byDate")
                .param("fromDate", fromDate)
                .param("toDate", toDate)
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).getAllInvoicesByDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @Test
    void shouldReturnBadRequestWhenFromDateParsedValueIsNullDuringGettingAllInvoicesByDate() throws Exception {
        //Given
        String fromDate = "2018-01-01";
        String toDate = "2018-01-31";

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/byDate")
                .param("fromDate", "")
                .param("toDate", toDate)
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), actualHttpStatus);
        verify(invoiceService, never()).getAllInvoicesByDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @Test
    void shouldReturnBadRequestWhenToDateParsedValueIsNullDuringGettingAllInvoicesByDate() throws Exception {
        //Given
        String fromDate = "2018-01-01";
        String toDate = "2018-01-31";

        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/byDate")
                .param("fromDate", fromDate)
                .param("toDate", "")
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), actualHttpStatus);
        verify(invoiceService, never()).getAllInvoicesByDate(LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    @Test
    void shouldReturnInvoicesByGivenBuyerId() throws Exception {
        //Given
        Invoice invoice1 = InvoiceGenerator.getRandomInvoiceWithSpecificBuyerId(1L);
        Invoice invoice2 = InvoiceGenerator.getRandomInvoiceWithSpecificBuyerId(1L);
        List<Invoice> expected = Arrays.asList(invoice1, invoice2);
        when(invoiceService.getAllInvoicesByBuyer(1L)).thenReturn(expected);

        //When
        MvcResult result = mockMvc.perform(
            get(String.format("/invoices/byBuyer/%d", 1L))
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        List<Invoice> actualInvoices = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {
        });

        //Then
        assertEquals(HttpStatus.OK.value(), actualHttpStatus);
        assertEquals(expected, actualInvoices);
        verify(invoiceService).getAllInvoicesByBuyer(1L);
    }

    @Test
    void shouldReturnInternalServerErrorWhenExceptionThrownByInvoiceServiceDuringGettingAllInvoicesByBuyerId() throws Exception {
        //Given
        when(invoiceService.getAllInvoicesByBuyer(1L)).thenThrow(ServiceOperationException.class);

        //When
        MvcResult result = mockMvc.perform(
            get(String.format("/invoices/byBuyer/%d", 1L))
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).getAllInvoicesByBuyer(1L);
    }

    @Test
    void shouldReturnBadRequestWhenParsedIdValueIsNullDuringGettingAllInvoicesByBuyerId() throws Exception {
        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/byBuyer")
                .param("id", "")
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), actualHttpStatus);
        verify(invoiceService, never()).getAllInvoicesByBuyer(null);
    }

    @Test
    void shouldReturnInvoicesByGivenSellerId() throws Exception {
        //Given
        Invoice invoice1 = InvoiceGenerator.getRandomInvoiceWithSpecificSellerId(1L);
        Invoice invoice2 = InvoiceGenerator.getRandomInvoiceWithSpecificSellerId(1L);
        List<Invoice> expected = Arrays.asList(invoice1, invoice2);
        when(invoiceService.getAllInvoicesBySeller(1L)).thenReturn(expected);

        //When
        MvcResult result = mockMvc.perform(
            get(String.format("/invoices/bySeller/%d", 1L))
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        List<Invoice> actualInvoices = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Invoice>>() {
        });

        //Then
        assertEquals(HttpStatus.OK.value(), actualHttpStatus);
        assertEquals(expected, actualInvoices);
        verify(invoiceService).getAllInvoicesBySeller(1L);
    }

    @Test
    void shouldReturnInternalServerErrorWhenExceptionThrownByInvoiceServiceDuringGettingAllInvoicesBySellerId() throws Exception {
        //Given
        when(invoiceService.getAllInvoicesBySeller(1L)).thenThrow(ServiceOperationException.class);

        //When
        MvcResult result = mockMvc.perform(
            get(String.format("/invoices/bySeller/%d", 1L))
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        verify(invoiceService, never()).getAllInvoicesByBuyer(1L);
        verify(invoiceService).getAllInvoicesBySeller(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
    }

    @Test
    void shouldReturnBadRequestWhenParsedIdValueIsNullDuringGettingAllInvoicesBySellerId() throws Exception {
        //When
        MvcResult result = mockMvc.perform(
            get("/invoices/bySeller")
                .param("id", "")
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), actualHttpStatus);
        verify(invoiceService, never()).getAllInvoicesBySeller(null);
    }

    @Test
    void shouldDeleteInvoice() throws Exception {
        //When
        MvcResult result = mockMvc.perform(
            delete("/invoices/{id}", 1L).accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        //Then
        assertEquals(HttpStatus.NO_CONTENT.value(), actualHttpStatus);
        verify(invoiceService).deleteInvoice(1L);
    }

    @Test
    void shouldReturnInternalServerErrorDuringDeletingInvoiceWhenSomethingWentWrongOnServer() throws Exception {
        //Given
        Long id = 10L;
        doThrow(ServiceOperationException.class)
            .when(invoiceService)
            .deleteInvoice(id);

        //When
        MvcResult result = mockMvc.perform(
            delete("/invoices/{id}", id).accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).deleteInvoice(id);
    }

    @Test
    void shouldDeleteAllInvoices() throws Exception {
        //When
        MvcResult result = mockMvc.perform(
            delete("/invoices").accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        //Then
        assertEquals(HttpStatus.NO_CONTENT.value(), actualHttpStatus);
        verify(invoiceService).deleteAllInvoices();
    }

    @Test
    void shouldReturnInternalServerErrorDuringDeletingAllInvoicesWhenSomethingWentWrongOnServer() throws Exception {
        //Given
        doThrow(ServiceOperationException.class)
            .when(invoiceService)
            .deleteAllInvoices();

        //When
        MvcResult result = mockMvc.perform(
            delete("/invoices").accept(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).deleteAllInvoices();
    }

    @Test
    void shouldSaveInvoice() throws Exception {
        //Given
        Invoice invoice = InvoiceGenerator.getRandomInvoice();
        when(invoiceService.saveInvoice(invoice)).thenReturn(invoice);
        String invoiceAsJson = mapper.writeValueAsString(invoice);

        //When
        MvcResult result = mockMvc.perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(invoiceAsJson))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();
        Invoice actualInvoice = mapper.readValue(result.getResponse().getContentAsString(), Invoice.class);

        //Then
        assertEquals(HttpStatus.CREATED.value(), actualHttpStatus);
        assertEquals(invoice, actualInvoice);
        verify(invoiceService).saveInvoice(invoice);
    }

    @Test
    void shouldReturnInternalServerErrorDuringSavingInvoiceWhenSomethingWentWrongOnServer() throws Exception {
        //Given
        Invoice invoice = InvoiceGenerator.getRandomInvoice();
        String invoiceAsJson = mapper.writeValueAsString(invoice);
        doThrow(ServiceOperationException.class)
            .when(invoiceService)
            .saveInvoice(invoice);

        //When
        MvcResult result = mockMvc.perform(
            post("/invoices")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(invoiceAsJson))
            .andReturn();
        int actualHttpStatus = result.getResponse().getStatus();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actualHttpStatus);
        verify(invoiceService).saveInvoice(invoice);
    }
}
