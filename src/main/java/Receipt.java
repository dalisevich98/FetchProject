package main.java;

import java.util.List;

public class Receipt {
    private String retailer;
    private String purchaseDate;
    private String purchaseTime;
    private List<Item> items;
    private String total;

    public String getRetailer() { return retailer; }
    public String getPurchaseDate() { return purchaseDate; }
    public String getPurchaseTime() { return purchaseTime; }
    public List<Item> getItems() { return items; }
    public String getTotal() { return total; }
}
