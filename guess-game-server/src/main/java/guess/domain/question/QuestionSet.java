package guess.domain.question;

import guess.domain.source.Event;

import java.util.List;
import java.util.Objects;

public class QuestionSet {
    private final Event event;
    private final List<SpeakerQuestion> speakerQuestions;
    private final List<TalkQuestion> talkQuestions;
    private final List<CompanyBySpeakerQuestion> companyBySpeakerQuestions;
    private final List<SpeakerByCompanyQuestion> speakerByCompanyQuestions;
    private final List<SpeakerQuestion> accountQuestions;
    private final List<TagCloudQuestion> tagCloudQuestions;

    public QuestionSet(Event event, List<SpeakerQuestion> speakerQuestions, List<TalkQuestion> talkQuestions,
                       List<CompanyBySpeakerQuestion> companyBySpeakerQuestions,
                       List<SpeakerByCompanyQuestion> speakerByCompanyQuestions,
                       List<SpeakerQuestion> accountQuestions,
                       List<TagCloudQuestion> tagCloudQuestions) {
        this.event = event;
        this.speakerQuestions = speakerQuestions;
        this.talkQuestions = talkQuestions;
        this.companyBySpeakerQuestions = companyBySpeakerQuestions;
        this.speakerByCompanyQuestions = speakerByCompanyQuestions;
        this.accountQuestions = accountQuestions;
        this.tagCloudQuestions = tagCloudQuestions;
    }

    public Event getEvent() {
        return event;
    }

    public List<SpeakerQuestion> getSpeakerQuestions() {
        return speakerQuestions;
    }

    public List<TalkQuestion> getTalkQuestions() {
        return talkQuestions;
    }

    public List<CompanyBySpeakerQuestion> getCompanyBySpeakerQuestions() {
        return companyBySpeakerQuestions;
    }

    public List<SpeakerByCompanyQuestion> getSpeakerByCompanyQuestions() {
        return speakerByCompanyQuestions;
    }

    public List<SpeakerQuestion> getAccountQuestions() {
        return accountQuestions;
    }

    public List<TagCloudQuestion> getTagCloudBySpeakerQuestions() {
        return tagCloudQuestions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionSet that = (QuestionSet) o;
        return Objects.equals(event, that.event) &&
                Objects.equals(speakerQuestions, that.speakerQuestions) &&
                Objects.equals(talkQuestions, that.talkQuestions) &&
                Objects.equals(companyBySpeakerQuestions, that.companyBySpeakerQuestions) &&
                Objects.equals(speakerByCompanyQuestions, that.speakerByCompanyQuestions) &&
                Objects.equals(accountQuestions, that.accountQuestions) &&
                Objects.equals(tagCloudQuestions, that.tagCloudQuestions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, speakerQuestions, talkQuestions, accountQuestions, tagCloudQuestions);
    }

    @Override
    public String toString() {
        return "QuestionSet{" +
                "event=" + event +
                ", speakerQuestions=" + speakerQuestions +
                ", talkQuestions=" + talkQuestions +
                ", companyBySpeakerQuestions=" + companyBySpeakerQuestions +
                ", speakerByCompanyQuestions=" + speakerByCompanyQuestions +
                ", accountQuestions=" + accountQuestions +
                ", tagCloudQuestions=" + tagCloudQuestions +
                '}';
    }
}
