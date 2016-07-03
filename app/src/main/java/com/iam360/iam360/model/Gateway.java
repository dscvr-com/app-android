package com.iam360.iam360.model;

public class Gateway {

    public static class CheckStatusData {
        private final String uuid;


        public CheckStatusData(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class CheckStatusResponse {
        public final static String MESSAGE_1 = "1";
        public final static String MESSAGE_2 = "2";
        public final static String MESSAGE_3 = "3";
        private final String status;
        private final String message;
        private final String request_text;

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

        @Override
        public String toString() {
            return "status : " + status + " message : " + message + " request_text : " + request_text;
        }
    }

    public static class RequestCodeData {
        private final String uuid;

        public RequestCodeData(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class RequestCodeResponse {
        private final String status;
        private final String message;
        private final String request_text;
        private final String prompt;

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

        @Override
        public String toString() {
            return "status : " + status + " message : " + message + " request_text : " + request_text + " prompt : " + prompt;
        }
    }

    public static class UseCodeData {
        private final String uuid;
        private final String code;

        public UseCodeData(String uuid, String code) {
            this.uuid = uuid;
            this.code = code;
        }
    }

    public static class UseCodeResponse {
        private final String status;
        private final String message;
        private final String prompt;

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

        @Override
        public String toString() {
            return "status : " + status + " message : " + message + " request_text : " + " prompt : " + prompt;
        }
    }

}