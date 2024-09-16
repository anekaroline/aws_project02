package br.com.project.aws_project02.model;

import br.com.project.aws_project02.enums.EventType;

public class ProductEventLogDto{
        private final String code;
        private final EventType eventType;
        private final long productId;
        private final String username;
        private final long timestamp;
        private final String messageId;


    public ProductEventLogDto(ProductEventLog productEventLog){
        this.code = productEventLog.getPk();
        this.eventType = productEventLog.getEventType();
        this.productId = productEventLog.getProductId();
        this.username = productEventLog.getUsername();
        this.timestamp = productEventLog.getTimestamp();
        this.messageId = productEventLog.getMessageId();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public long getProductId() {
        return productId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getCode() {
        return code;
    }

    public String getMessageId() {
        return messageId;
    }
}
