package psam1.domain;

import java.math.BigDecimal;

public class CustomerRecord {
    private final String recordType; // 'C' for customer
    private final String customerId;
    private final String name;
    private final String occupation;
    private final BigDecimal accountBalance; // scale 2
    private final int ordersYtd;

    public CustomerRecord(String recordType, String customerId, String name, String occupation,
                          BigDecimal accountBalance, int ordersYtd) {
        this.recordType = recordType;
        this.customerId = customerId;
        this.name = name;
        this.occupation = occupation;
        this.accountBalance = accountBalance;
        this.ordersYtd = ordersYtd;
    }

    public String getRecordType() { return recordType; }
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getOccupation() { return occupation; }
    public BigDecimal getAccountBalance() { return accountBalance; }
    public int getOrdersYtd() { return ordersYtd; }
}
