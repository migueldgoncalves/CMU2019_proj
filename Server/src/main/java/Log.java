import java.util.Date;

public class Log {

    private String operation;
    private Date timestamp;
    private AppRequest request;
    private AppResponse response;

    public Log(String operation, AppRequest request, AppResponse response) {
        this.operation = operation;
        this.timestamp = new Date();
        this.request = request;
        this.response = response;
    }

    public String getOperation() {
        return operation;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public AppRequest getRequest() {
        return request;
    }

    public AppResponse getResponse() {
        return response;
    }
}
