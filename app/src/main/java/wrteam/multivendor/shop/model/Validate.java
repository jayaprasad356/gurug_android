package wrteam.multivendor.shop.model;

import java.io.Serializable;

public class Validate implements Serializable {

    boolean error;
    String message;
    String discounted_amount;
    String discount;

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getDiscounted_amount() {
        return discounted_amount;
    }

    public String getDiscount() {
        return discount;
    }
}
