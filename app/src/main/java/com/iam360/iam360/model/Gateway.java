package com.iam360.iam360.model;

public class Gateway {

    public static class CheckStatusData {
        final String uuid;

        public CheckStatusData(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class CheckStatusResponse {
        final String status;
        final String message;
        final String request_text;

        public CheckStatusResponse(String status, String message, String request_text) {
            this.status = status;
            this.message = message;
            this.request_text = request_text;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getRequestText() {
            return request_text;
        }
    }

    public static class RequestCodeData {
        final String uuid;

        public RequestCodeData(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class RequestCodeResponse {
        final String status;
        final String message;
        final String request_text;
        final String prompt;

        public RequestCodeResponse(String status, String message, String request_text, String prompt) {
            this.status = status;
            this.message = message;
            this.request_text = request_text;
            this.prompt = prompt;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getRequestText() {
            return request_text;
        }

        public String getPrompt() {
            return prompt;
        }
    }

    public static class UseCodeData {
        final String uuid;
        final String code;

        public UseCodeData(String uuid, String code) {
            this.uuid = uuid;
            this.code = code;
        }
    }

    public static class UseCodeResponse {
        final String status;
        final String message;
        final String prompt;

        public UseCodeResponse(String status, String message, String prompt) {
            this.status = status;
            this.message = message;
            this.prompt = prompt;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getPrompt() {
            return prompt;
        }
    }

}