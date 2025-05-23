package guess.domain.source.cms.jrgcms.speaker;

import guess.domain.source.cms.jrgcms.JrgCmsPhoto;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JrgCmsSpeaker {
    private String id;
    private List<JrgCmsPhoto> photo;
    private Map<String, String> firstName;
    private Map<String, String> lastName;
    private String titulus;
    private Map<String, String> company;
    private Map<String, String> description;
    private List<JrgContact> contacts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<JrgCmsPhoto> getPhoto() {
        return photo;
    }

    public void setPhoto(List<JrgCmsPhoto> photo) {
        this.photo = photo;
    }

    public Map<String, String> getFirstName() {
        return firstName;
    }

    public void setFirstName(Map<String, String> firstName) {
        this.firstName = firstName;
    }

    public Map<String, String> getLastName() {
        return lastName;
    }

    public void setLastName(Map<String, String> lastName) {
        this.lastName = lastName;
    }

    public String getTitulus() {
        return titulus;
    }

    public void setTitulus(String titulus) {
        this.titulus = titulus;
    }

    public Map<String, String> getCompany() {
        return company;
    }

    public void setCompany(Map<String, String> company) {
        this.company = company;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public List<JrgContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<JrgContact> contacts) {
        this.contacts = contacts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JrgCmsSpeaker)) return false;
        JrgCmsSpeaker that = (JrgCmsSpeaker) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
