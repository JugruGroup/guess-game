package guess.domain.source;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Talk.
 */
public class Talk extends Descriptionable {
    public static class TalkLinks {
        private final List<String> presentationLinks;
        private final List<String> materialLinks;
        private final List<String> videoLinks;

        public TalkLinks(List<String> presentationLinks, List<String> materialLinks, List<String> videoLinks) {
            this.presentationLinks = presentationLinks;
            this.materialLinks = materialLinks;
            this.videoLinks = videoLinks;
        }
    }

    public static class TalkAttributes {
        private final String language;
        private final Topic topic;

        public TalkAttributes(String language, Topic topic) {
            this.language = language;
            this.topic = topic;
        }
    }

    private Long talkDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long track;
    private String language;
    private List<String> presentationLinks;
    private List<String> materialLinks;
    private List<String> videoLinks;

    private List<Long> speakerIds;
    private List<Speaker> speakers = new ArrayList<>();

    private Long topicId;
    private Topic topic;

    public Talk() {
    }

    public Talk(Descriptionable descriptionable, Long talkDay, LocalTime startTime, LocalTime endTime, Long track, TalkLinks links,
                List<Speaker> speakers, TalkAttributes attributes) {
        super(descriptionable.getId(), descriptionable.getName(), descriptionable.getShortDescription(), descriptionable.getLongDescription());

        this.talkDay = talkDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.track = track;
        this.language = attributes.language;
        this.presentationLinks = links.presentationLinks;
        this.materialLinks = links.materialLinks;
        this.videoLinks = links.videoLinks;
        this.speakers = speakers;
        this.speakerIds = speakers.stream()
                .map(Speaker::getId)
                .collect(Collectors.toCollection(ArrayList::new));

        this.topic = attributes.topic;
        this.topicId = (topic != null) ? topic.getId() : null;
    }

    public Long getTalkDay() {
        return talkDay;
    }

    public void setTalkDay(Long talkDay) {
        this.talkDay = talkDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Long getTrack() {
        return track;
    }

    public void setTrack(Long track) {
        this.track = track;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getPresentationLinks() {
        return presentationLinks;
    }

    public void setPresentationLinks(List<String> presentationLinks) {
        this.presentationLinks = presentationLinks;
    }

    public List<String> getMaterialLinks() {
        return materialLinks;
    }

    public void setMaterialLinks(List<String> materialLinks) {
        this.materialLinks = materialLinks;
    }

    public List<String> getVideoLinks() {
        return videoLinks;
    }

    public void setVideoLinks(List<String> videoLinks) {
        this.videoLinks = videoLinks;
    }

    public List<Long> getSpeakerIds() {
        return speakerIds;
    }

    public void setSpeakerIds(List<Long> speakerIds) {
        this.speakerIds = speakerIds;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Talk{" +
                "id=" + getId() +
                ", name=" + getName() +
                '}';
    }
}
