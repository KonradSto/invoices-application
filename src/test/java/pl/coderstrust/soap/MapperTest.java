package pl.coderstrust.soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.coderstrust.generators.CompanyGenerator.getRandomNumberAsString;
import static pl.coderstrust.soap.Mapper.mapInvoice;
import static pl.coderstrust.soap.Mapper.mapInvoices;
import static pl.coderstrust.soap.Mapper.mapLocalDateToXmlGregorianCalendar;
import static pl.coderstrust.soap.Mapper.mapXmlGregorianCalendarToLocalDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.coderstrust.generators.IdGenerator;
import pl.coderstrust.generators.InvoiceNumberGenerator;
import pl.coderstrust.generators.WordGenerator;
import pl.coderstrust.model.Company;
import pl.coderstrust.model.Invoice;
import pl.coderstrust.model.InvoiceEntry;
import pl.coderstrust.model.Vat;
import pl.coderstrust.soap.bindingclasses.Entries;

class MapperTest {

    private Invoice modelInvoice;
    private pl.coderstrust.soap.bindingclasses.Invoice soapInvoice;
    private List<Invoice> modelInvoiceList;
    private List<pl.coderstrust.soap.bindingclasses.Invoice> soapInvoiceList;

    @BeforeEach
    void setup() throws DatatypeConfigurationException {
        Random random = new Random();
        Long entryId = IdGenerator.getNextId();
        String entryProductName = WordGenerator.getRandomWord();
        double entryQuantity = random.nextInt(10);
        String entryUnit = "szt.";
        BigDecimal entryPrice = BigDecimal.valueOf(random.nextInt(2000));
        BigDecimal entryNettValue = BigDecimal.valueOf(entryQuantity).multiply(entryPrice);
        BigDecimal entryGrossValue = entryNettValue.multiply(BigDecimal.valueOf(1.23).setScale(2, BigDecimal.ROUND_HALF_EVEN));
        Long companyId = IdGenerator.getNextId();
        String companyName = WordGenerator.getRandomWord();
        String companyAddress = WordGenerator.getRandomWord();
        String companyTaxId = getRandomNumberAsString();
        String companyAccountNumber = getRandomNumberAsString();
        String companyPhoneNumber = getRandomNumberAsString();
        String companyEmail = WordGenerator.getRandomWord();
        pl.coderstrust.soap.bindingclasses.Company soapCompany = prepareSoapCompany(companyId, companyName, companyAddress, companyTaxId, companyAccountNumber, companyPhoneNumber, companyEmail);
        InvoiceEntry modelInvoiceEntry = new InvoiceEntry(entryId, entryProductName, entryQuantity, entryUnit, entryPrice, entryNettValue, entryGrossValue, Vat.VAT_23);
        List<InvoiceEntry> modelEntries = new ArrayList<>();
        modelEntries.add(modelInvoiceEntry);
        pl.coderstrust.soap.bindingclasses.InvoiceEntry soapInvoiceEntry = prepareSoapInvoiceEntry(entryId, entryProductName, entryQuantity, entryUnit, entryPrice, entryNettValue, entryGrossValue);
        pl.coderstrust.soap.bindingclasses.Entries soapInvoiceEntries = new Entries();
        soapInvoiceEntries.getInvoiceEntry().add(soapInvoiceEntry);
        Long invoiceId = IdGenerator.getNextId();
        String invoiceNumber = InvoiceNumberGenerator.getNextInvoiceNumber();
        LocalDate invoiceIssuedDate = LocalDate.of(2000, 1, 1);
        LocalDate invoiceDueDate = LocalDate.of(2000, 2, 1);
        Company modelCompany = new Company(companyId, companyName, companyAddress, companyTaxId, companyAccountNumber, companyPhoneNumber, companyEmail);
        prepareSoapInvoice(soapCompany, soapInvoiceEntries, invoiceId, invoiceNumber);
        prepareModelInvoice(modelEntries, invoiceId, invoiceNumber, invoiceIssuedDate, invoiceDueDate, modelCompany);
        prepareModelInvoiceList();
        prepareSoapInvoiceList();
    }

    private void prepareModelInvoice(List<InvoiceEntry> modelEntries, Long invoiceId, String invoiceNumber, LocalDate invoiceIssuedDate, LocalDate invoiceDueDate, Company modelCompany) {
        modelInvoice = new Invoice(invoiceId, invoiceNumber, invoiceIssuedDate, invoiceDueDate, modelCompany, modelCompany, modelEntries);
    }

    private pl.coderstrust.soap.bindingclasses.Company prepareSoapCompany(Long companyId, String companyName, String companyAddress, String companyTaxId, String companyAccountNumber, String companyPhoneNumber, String companyEmail) {
        pl.coderstrust.soap.bindingclasses.Company soapCompany = new pl.coderstrust.soap.bindingclasses.Company();
        soapCompany.setId(companyId);
        soapCompany.setName(companyName);
        soapCompany.setAddress(companyAddress);
        soapCompany.setTaxId(companyTaxId);
        soapCompany.setAccountNumber(companyAccountNumber);
        soapCompany.setPhoneNumber(companyPhoneNumber);
        soapCompany.setEmail(companyEmail);
        return soapCompany;
    }

    private pl.coderstrust.soap.bindingclasses.InvoiceEntry prepareSoapInvoiceEntry(Long entryId, String entryProductName, double entryQuantity, String entryUnit, BigDecimal entryPrice, BigDecimal entryNettValue, BigDecimal entryGrossValue) {
        pl.coderstrust.soap.bindingclasses.InvoiceEntry soapInvoiceEntry = new pl.coderstrust.soap.bindingclasses.InvoiceEntry();
        soapInvoiceEntry.setId(entryId);
        soapInvoiceEntry.setProductName(entryProductName);
        soapInvoiceEntry.setQuantity(entryQuantity);
        soapInvoiceEntry.setUnit(entryUnit);
        soapInvoiceEntry.setPrice(entryPrice);
        soapInvoiceEntry.setNettValue(entryNettValue);
        soapInvoiceEntry.setGrossValue(entryGrossValue);
        soapInvoiceEntry.setVatRate(pl.coderstrust.soap.bindingclasses.Vat.VAT_23);
        return soapInvoiceEntry;
    }

    private void prepareSoapInvoiceList() {
        soapInvoiceList = new ArrayList<>();
        soapInvoiceList.add(soapInvoice);
    }

    private void prepareModelInvoiceList() {
        modelInvoiceList = new ArrayList<>();
        modelInvoiceList.add(modelInvoice);
    }

    private void prepareSoapInvoice(pl.coderstrust.soap.bindingclasses.Company soapCompany, Entries soapInvoiceEntries, Long invoiceId, String invoiceNumber) throws DatatypeConfigurationException {
        soapInvoice = new pl.coderstrust.soap.bindingclasses.Invoice();
        soapInvoice.setId(invoiceId);
        soapInvoice.setNumber(invoiceNumber);
        soapInvoice.setIssuedDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(2000, 1, 1, 0, 0, 0, 0, 0));
        soapInvoice.setLocalDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(2000, 2, 1, 0, 0, 0, 0, 0));
        soapInvoice.setSeller(soapCompany);
        soapInvoice.setBuyer(soapCompany);
        soapInvoice.setEntries(soapInvoiceEntries);
    }

    @Test
    void shouldMapSoapInvoiceToOriginalInvoice() {
        //When
        Invoice resultInvoice = mapInvoice(soapInvoice);

        //Then
        assertEquals(modelInvoice, resultInvoice);
    }

    @Test
    void shouldMapOriginalInvoiceToSoapInvoice() throws DatatypeConfigurationException {
        //When
        pl.coderstrust.soap.bindingclasses.Invoice resultInvoice = Mapper.mapInvoice(modelInvoice);

        //Then
        assertEquals(soapInvoice, resultInvoice);
    }

    @Test
    void shouldMapOriginalInvoicesToSoapInvoices() throws DatatypeConfigurationException {
        //When
        List<pl.coderstrust.soap.bindingclasses.Invoice> resultInvoices = mapInvoices(modelInvoiceList);

        //Then
        assertEquals(soapInvoiceList, resultInvoices);
    }

    @Test
    void shouldConvertLocalDateToXmlGregorianCalendar() throws DatatypeConfigurationException {
        //When
        XMLGregorianCalendar resultDate = mapLocalDateToXmlGregorianCalendar(modelInvoice.getIssuedDate());

        //Then
        assertEquals(soapInvoice.getIssuedDate(), resultDate);
    }


    @Test
    void shouldConvertXmlGregorianCalendarToLocalDate() {
        //When
        LocalDate resultDate = mapXmlGregorianCalendarToLocalDate(soapInvoice.getIssuedDate());

        //Then
        assertEquals(modelInvoice.getIssuedDate(), resultDate);
    }
}
