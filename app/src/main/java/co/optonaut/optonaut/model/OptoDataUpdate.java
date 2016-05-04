package co.optonaut.optonaut.model;

/**
 * Created by Mariel on 4/30/2016.
 */
public class OptoDataUpdate {
    final String text;
    final boolean is_private;
    final boolean is_published;
    final boolean post_facebook;
    final boolean post_twitter;

    public OptoDataUpdate(String text, boolean is_private, boolean is_published, boolean post_facebook,
                          boolean post_twitter) {
        this.text = text;
        this.is_private = is_private;
        this.is_published = is_published;
        this.post_facebook = post_facebook;
        this.post_twitter = post_twitter;
    }
}
