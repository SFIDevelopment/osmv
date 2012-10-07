package net.londatiga.android;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Action item, displayed as menu with icon and text.
 * 
 * @author Lorensius. W. L. T
 * 
 */
public class ActionItem {
    private Drawable icon;
    private Bitmap   thumb;
    private String   title;
    private boolean  selected;
    private boolean  clickable = true;

    /**
     * Constructor
     */
    public ActionItem() {
    }

    /**
     * Constructor
     * 
     * @param icon
     *            {@link Drawable} action icon
     */
    public ActionItem(final Drawable icon) {
        this.icon = icon;
    }

    /**
     * Set action title
     * 
     * @param title
     *            action title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Get action title
     * 
     * @return action title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set action icon
     * 
     * @param icon
     *            {@link Drawable} action icon
     */
    public void setIcon(final Drawable icon) {
        this.icon = icon;
    }

    /**
     * Get action icon
     * 
     * @return {@link Drawable} action icon
     */
    public Drawable getIcon() {
        return icon;
    }

    /**
     * Set selected flag;
     * 
     * @param selected
     *            Flag to indicate the item is selected
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    /**
     * Check if item is selected
     * 
     * @return true or false
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set thumb
     * 
     * @param thumb
     *            Thumb image
     */
    public void setThumb(final Bitmap thumb) {
        this.thumb = thumb;
    }

    /**
     * Get thumb image
     * 
     * @return Thumb image
     */
    public Bitmap getThumb() {
        return thumb;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(final boolean clickable) {
        this.clickable = clickable;
    }

}