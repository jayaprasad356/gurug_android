package wrteam.multivendor.shop.model;


public class Slider {
    final String image;
    String type;
    String type_id;
    String name;
    String slider_url;

    public Slider(String image) {
        this.image = image;
    }

    public String getLink() {
        return slider_url;
    }

    public String getType_id() {
        return type_id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getType() {
        return type;
    }
}
