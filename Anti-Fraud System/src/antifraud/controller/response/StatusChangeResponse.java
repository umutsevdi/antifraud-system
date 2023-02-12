package antifraud.controller.response;


import lombok.Data;

@Data
public class StatusChangeResponse {
    public StatusChangeResponse() {

    }

    public StatusChangeResponse(String username, Boolean isUserLocked) {
        String status = isUserLocked ? "locked!" : "unlocked!";
        this.status = "User " + username + " " + status;
    }

    private String status;

}