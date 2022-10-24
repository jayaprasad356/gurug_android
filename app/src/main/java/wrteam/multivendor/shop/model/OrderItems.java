package wrteam.multivendor.shop.model;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderItems implements Serializable {
   String id,user_id,order_id,product_name,variant_name,product_variant_id,delivery_boy_id,quantity,price,discounted_price,tax_amount,tax_percentage,discount,sub_total,active_status,date_added,seller_id,is_credited,shipping_method,variant_id,name,image,manufacturer,made_in,return_status,cancelable_status,till_status,measurement,unit,seller_name,seller_store_name,return_days,current_status,shipment_id;
    boolean applied_for_return;

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getVariant_name() {
        return variant_name;
    }

    public String getProduct_variant_id() {
        return product_variant_id;
    }

    public String getDelivery_boy_id() {
        return delivery_boy_id;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getDiscounted_price() {
        return discounted_price;
    }

    public String getTax_amount() {
        return tax_amount;
    }

    public String getTax_percentage() {
        return tax_percentage;
    }

    public String getDiscount() {
        return discount;
    }

    public String getSub_total() {
        return sub_total;
    }

    public String getActive_status() {
        return active_status;
    }

    public void setActive_status(String active_status) {
        this.active_status = active_status;
    }

    public String getDate_added() {
        return date_added;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public String getIs_credited() {
        return is_credited;
    }

    public String getShipping_method() {
        return shipping_method;
    }

    public String getVariant_id() {
        return variant_id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getMade_in() {
        return made_in;
    }

    public String getReturn_status() {
        return return_status;
    }

    public String getCancelable_status() {
        return cancelable_status;
    }

    public String getTill_status() {
        return till_status;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getUnit() {
        return unit;
    }

    public String getSeller_name() {
        return seller_name;
    }

    public String getSeller_store_name() {
        return seller_store_name;
    }

    public String getReturn_days() {
        return return_days;
    }

    public String getCurrent_status() {
        return current_status;
    }

    public boolean isApplied_for_return() {
        return applied_for_return;
    }

    public String getShipment_id() {
        return shipment_id;
    }
}
