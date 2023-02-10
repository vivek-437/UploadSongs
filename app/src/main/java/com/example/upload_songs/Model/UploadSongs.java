package com.example.upload_songs.Model;

public class UploadSongs {
    public String songscategory;
    public String songstitle;
    public String songsDuration;
    public String songsLink;

    public UploadSongs(String songscategory, String songstitle, String songsDuration, String songsLink) {
        if (songstitle.trim().equals("") ){
            songstitle="No title";
        }


        this.songscategory = songscategory;
        this.songstitle = songstitle;

        this.songsDuration = songsDuration;
        this.songsLink = songsLink;
        this.mkey = mkey;
    }

    public String mkey;

    public String getSongscategory() {
        return songscategory;
    }

    public void setSongscategory(String songscategory) {
        this.songscategory = songscategory;
    }

    public String getSongstitle() {
        return songstitle;
    }

    public void setSongstitle(String songstitle) {
        this.songstitle = songstitle;
    }


    public String getSongsDuration() {
        return songsDuration;
    }

    public void setSongsDuration(String songsDuration) {
        this.songsDuration = songsDuration;
    }

    public String getSongsLink() {
        return songsLink;
    }

    public void setSongsLink(String songsLink) {
        this.songsLink = songsLink;
    }

    public String getMkey() {
        return mkey;
    }

    public void setMkey(String mkey) {
        this.mkey = mkey;
    }
}
