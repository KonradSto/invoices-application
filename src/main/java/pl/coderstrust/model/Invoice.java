package pl.coderstrust.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Invoice {

    private final Long id;
    private final String number;
    private final LocalDate issuedDate;
    private final LocalDate dueDate;
    private final Company seller;
    private final Company buyer;
    private final List<InvoiceEntry> entries;

    @JsonCreator
    public Invoice(@JsonProperty("id") Long id,
        @JsonProperty("number") String number,
        @JsonProperty("issuedDate") LocalDate issuedDate,
        @JsonProperty("dueDate") LocalDate dueDate,
        @JsonProperty("seller") Company seller,
        @JsonProperty("buyer") Company buyer,
        @JsonProperty("entries") List<InvoiceEntry> entries) {

        this.id = id;
        this.number = number;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.seller = seller;
        this.buyer = buyer;
        this.entries = entries != null ? new ArrayList(entries) : new ArrayList();
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Company getSeller() {
        return seller;
    }

    public Company getBuyer() {
        return buyer;
    }

    public List<InvoiceEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Invoice invoice = (Invoice) obj;
        return id.equals(invoice.id)
            && number.equals(invoice.number)
            && issuedDate.equals(invoice.issuedDate)
            && dueDate.equals(invoice.dueDate)
            && seller.equals(invoice.seller)
            && buyer.equals(invoice.buyer)
            && entries.equals(invoice.entries);
    }

    @Override
    public String toString() {
        return "Invoice{"
            + "id=" + id
            + ", number='" + number + '\''
            + ", issuedDate=" + issuedDate
            + ", dueDate=" + dueDate
            + ", seller=" + seller
            + ", buyer=" + buyer
            + ", entries=" + entries
            + '}';
    }
}
