package util.http;

public interface HttpCallback {
    void process(int statusCode, String returnContent);
}
