// PanoramioItem.java
// Andrew Davison, November 2010, ad@fivedots.coe.psu.ac.th

/*
 * Holds one item returned from the Panoramio server. There are get methods for
 * each of the attributes of the item. e.g. { "height": 377, "latitude":
 * 7.158508, "longitude": 100.556853, "owner_id": 36909, "owner_name":
 * "Luthfi Amara", "owner_url": "http://www.panoramio.com/user/36909",
 * "photo_file_url":
 * "http://mw2.google.com/mw-panoramio/photos/medium/188716.jpg", "photo_id":
 * 188716, "photo_title": "sleeping Budha like a shape of mountain behind",
 * "photo_url": "http://www.panoramio.com/photo/188716", "upload_date":
 * "19 December 2006", "width": 500 } There is a getImage() method which
 * constructs the item's image by combining the image at photo_url along with
 * the other display requirements at http://www.panoramio.com/api/data/api.html
 */
package org.outlander.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.outlander.utils.Ut;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PanoramioItem {

    // private static final String LOGO_FNM = "panoramioLogo.png";
    // private static final String NOTICE =
    // "Photos provided by Panoramio are under the copyright of their owners.";
    private static final String ORI_IMG_URL = "http://static.panoramio.com/photos/original/";
    // image details
    private int                 photoID;
    private String              photoURL, photoFileURL, photoTitle;
    private Date                uploadDate;
    private int                 width, height;                                               // of
                                                                                              // image
    private double              latitude, longitude;                                         // image
                                                                                              // location
    // image owner details
    private int                 ownerID;
    private String              ownerName, ownerURL;
    private Bitmap              image;
    private Bitmap              oriImage;

    public PanoramioItem(final JSONObject j) {
        try {
            photoID = Integer.parseInt(j.getString("photo_id"));
            photoURL = j.getString("photo_url");
            photoFileURL = j.getString("photo_file_url");
            photoTitle = j.getString("photo_title");

            final DateFormat formatter = new SimpleDateFormat("dd MMMMM yyyy");
            uploadDate = formatter.parse(j.getString("upload_date"));

            width = Integer.parseInt(j.getString("width"));
            height = Integer.parseInt(j.getString("height"));
            latitude = Double.parseDouble(j.getString("latitude"));
            longitude = Double.parseDouble(j.getString("longitude"));

            ownerID = Integer.parseInt(j.getString("owner_id"));
            ownerName = j.getString("owner_name");
            ownerURL = j.getString("owner_url");
        }
        catch (final Exception e) {
            System.out.println(e);
        }
    } // end of PanoramioItem()

    public int getPhotoID() {
        return photoID;
    }

    public String getphotoURL() {
        return photoURL;
    }

    public String getOriPhotoURL() {

        final int pos = getPhotoFileURL().lastIndexOf("/");

        final String oriUrl = PanoramioItem.ORI_IMG_URL + getPhotoFileURL().substring(pos + 1);

        return oriUrl;
    }

    public String getPhotoFileURL() {
        return photoFileURL;
    }

    public String getPhotoTitle() {
        return photoTitle;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerURL() {
        return ownerURL;
    }

    private Bitmap downloadImage(final String fileUrl) {
        Bitmap image = null;

        try {
            final URL url = new URL(fileUrl);
            Ut.d("Downloading image at: " + fileUrl);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.connect();
            final InputStream is = conn.getInputStream();
            image = BitmapFactory.decodeStream(is);
        }
        catch (final IOException e) {
            Ut.d("Problem image downloading: " + fileUrl);
        }

        return image;
    }

    public Bitmap getImage() {

        if (image == null) {
            image = downloadImage(getPhotoFileURL());
        }
        return image;
    }

    public Bitmap getOriginalImage() {

        if (oriImage == null) {

            oriImage = downloadImage(getOriPhotoURL());
        }
        return oriImage;
    }

    // private BufferedImage addRequirements(BufferedImage photoIm) /*
    // Requirements for using a medium-size image:
    // (from http://www.panoramio.com/api/data/api.html)
    // - link to the photo page on the Panoramio.com domain
    // (e.g, http://www.panoramio.com/photo/532693)
    // - show the Panoramio logo
    // - display "author: name"
    // - link to the author's Panoramio photos homepage
    // (e.g., http://www.panoramio.com/user/1429589)
    // - display the text
    // "Photos provided by Panoramio are under the copyright of their owners"
    // */ {
    // // load the logo
    // BufferedImage logoIm = null;
    // try {
    // logoIm = ImageIO.read(new File(LOGO_FNM));
    // } catch (IOException e) {
    // System.out.println("Could not load logo from " + LOGO_FNM);
    // }
    //
    // // build the new image
    // BufferedImage im = new BufferedImage(width, height,
    // BufferedImage.TYPE_INT_ARGB);
    // Graphics2D g2d = im.createGraphics();
    //
    // FontMetrics fm = g2d.getFontMetrics();
    //
    // g2d.drawImage(photoIm, 0, 0, null); // original image
    //
    // // add request info
    // g2d.drawImage(logoIm, 5, 5, null); // logo at top-left
    //
    // // transparent white for highlighting the text
    // Color bgColor = new Color(1, 1, 1, 0.5f);
    //
    //
    // // top-right
    // int textWidth = fm.stringWidth(photoURL);
    // g2d.setPaint(bgColor); // draw background box
    // g2d.fillRect(width - 7 - textWidth, 3, textWidth + 6, 2 * (4 +
    // fm.getHeight()) + 2);
    // g2d.setPaint(Color.BLACK);
    //
    // g2d.drawString(photoURL, width - 5 - textWidth, 2 * (4 +
    // fm.getHeight())); // 2nd line
    // textWidth = fm.stringWidth(photoTitle);
    // g2d.drawString(photoTitle, width - 5 - textWidth, 4 + fm.getHeight()); //
    // 1st line
    //
    //
    // // bottom-left
    // textWidth = fm.stringWidth(NOTICE);
    // g2d.setPaint(bgColor); // draw background box
    // g2d.fillRect(2, height - 3 - 3 * (fm.getHeight() + 4), textWidth + 4,
    // 3 * (fm.getHeight() + 4) + 2);
    // g2d.setPaint(Color.BLACK);
    //
    // g2d.drawString("Author: " + ownerName, 5, height - 5 - 2 *
    // (fm.getHeight() + 4));
    // g2d.drawString(ownerURL, 5, height - 5 - (fm.getHeight() + 4));
    // g2d.drawString(NOTICE, 5, height - 5);
    //
    // g2d.dispose();
    // return im;
    // } // end of addRequirements()
} // end of PanoramioItem class

