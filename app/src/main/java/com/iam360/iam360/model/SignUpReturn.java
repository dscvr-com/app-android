package com.iam360.iam360.model;

/**
 * Created by Mariel on 3/31/2016.
 */
/*if failed,
{
"message": "insert person failed"
}
*/
public class SignUpReturn {
    private String message;

    public SignUpReturn() {
        message = "";
    }

    public String getMessage() {
        return message;
    }
}
