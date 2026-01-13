package ci553.happyshop.client.customer;

import java.io.IOException;
import java.sql.SQLException;


public class CustomerController {
    public CustomerModel cusModel;

    public void doAction(String action) throws SQLException, IOException {
        switch (action) {
            case "Search":
                cusModel.search();
                break;
            case "Add to Trolley":
                cusModel.addToTrolley(1);
                break;
            case "Cancel":
                cusModel.cancel();
                break;
            case "Check Out":
                cusModel.checkOut();
                break;
            case "Remove":
                cusModel.removeFromTrolley();
            case "OK & Close":
                cusModel.closeReceipt();
                break;
        }
    }

    public void addSelectedProductToTrolley(int qty) throws SQLException, IOException {
        cusModel.addToTrolley(qty);
    }
}
