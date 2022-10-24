package wrteam.multivendor.shop.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PromoCode implements Serializable {

    String promo_code;
    String message;
    String discount;
    ArrayList<Validate> is_validate;

    public String getPromo_code() {
        return promo_code;
    }

    public String getMessage() {
        return message;
    }

    public String getDiscount() {
        return discount;
    }


    public ArrayList<Validate> getIs_validate() {
        return is_validate;
    }
}
