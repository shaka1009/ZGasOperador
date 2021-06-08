package zgas.operador.models;

public class IAData {

    String id;
    String title;
    Float distance;

    float left;
    float top;
    float right;
    float bottom;

    Integer color;

    public String getId() {
        return id;
    }

    public zgas.operador.models.IAData setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public zgas.operador.models.IAData setTitle(String title) {
        this.title = title;
        return this;
    }

    public Float getDistance() {
        return distance;
    }

    public zgas.operador.models.IAData setDistance(Float distance) {
        this.distance = distance;
        return this;
    }

    public float getLeft() {
        return left;
    }

    public zgas.operador.models.IAData setLeft(float left) {
        this.left = left;
        return this;
    }

    public float getTop() {
        return top;
    }

    public zgas.operador.models.IAData setTop(float top) {
        this.top = top;
        return this;
    }

    public float getRight() {
        return right;
    }

    public zgas.operador.models.IAData setRight(float right) {
        this.right = right;
        return this;
    }

    public float getBottom() {
        return bottom;
    }

    public zgas.operador.models.IAData setBottom(float bottom) {
        this.bottom = bottom;
        return this;
    }

    public Integer getColor() {
        return color;
    }

    public zgas.operador.models.IAData setColor(Integer color) {
        this.color = color;
        return this;
    }

    public IAData(){}

    public IAData(String id, String title, float distance, float left, float top, float right, float bottom, Integer color) {
        this.id = id;
        this.title = title;
        this.distance = distance;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.color = color;
    }
}
