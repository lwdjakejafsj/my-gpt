package io.luowei.chatgpt.common.exception;

public class ChatGptException extends RuntimeException{
    /**
     * 异常码
     */
    private String code;

    /**
     * 异常信息
     */
    private String message;

    public ChatGptException(String code) {
        this.code = code;
    }

    public ChatGptException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    public ChatGptException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ChatGptException(String code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        super.initCause(cause);
    }
}
