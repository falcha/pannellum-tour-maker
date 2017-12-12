package com.leafyjava.pannellumtourmaker.domains;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.stream.Collectors;

public class Scene {
    @Id
    private String id;
    private String title;
    private float pitch;
    private float yaw;
    private Coordinates coordinates;
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

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(final Coordinates coordinates) {
        this.coordinates = coordinates;
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

    public void deleteHotSpots(String sceneId) {
        hotSpots = hotSpots.stream()
            .filter(hotSpot -> !hotSpot.getSceneId().equalsIgnoreCase(sceneId))
            .collect(Collectors.toList());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Scene scene = (Scene) o;

        return id.equals(scene.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
