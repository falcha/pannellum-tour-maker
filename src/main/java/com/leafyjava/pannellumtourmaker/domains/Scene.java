package com.leafyjava.pannellumtourmaker.domains;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

public class Scene {
    @Id
    private String id;
    private String title;
    private float pitch;
    private float yaw;
    private Gps gps;
    private float northOffset;
    private PhotoMeta photoMeta;
    private String type;
    private MultiRes multiRes;
    private List<HotSpot> hotSpots;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public Gps getGps() {
        return gps;
    }

    public void setGps(final Gps gps) {
        this.gps = gps;
    }

    public float getNorthOffset() {
        return northOffset;
    }

    public void setNorthOffset(final float northOffset) {
        this.northOffset = northOffset;
    }

    public PhotoMeta getPhotoMeta() {
        return photoMeta;
    }

    public void setPhotoMeta(final PhotoMeta photoMeta) {
        this.photoMeta = photoMeta;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public MultiRes getMultiRes() {
        return multiRes;
    }

    public void setMultiRes(final MultiRes multiRes) {
        this.multiRes = multiRes;
    }

    public List<HotSpot> getHotSpots() {
        return hotSpots;
    }

    public void setHotSpots(final List<HotSpot> hotSpots) {
        this.hotSpots = hotSpots;
    }
}
