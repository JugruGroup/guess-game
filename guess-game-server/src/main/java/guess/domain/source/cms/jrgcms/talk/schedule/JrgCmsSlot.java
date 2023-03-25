package guess.domain.source.cms.jrgcms.talk.schedule;

import guess.domain.source.cms.jrgcms.talk.JrgCmsActivity;

public class JrgCmsSlot {
    private String slotStartTime;
    private String slotEndTime;
    private JrgCmsActivity activity;

    public String getSlotStartTime() {
        return slotStartTime;
    }

    public void setSlotStartTime(String slotStartTime) {
        this.slotStartTime = slotStartTime;
    }

    public String getSlotEndTime() {
        return slotEndTime;
    }

    public void setSlotEndTime(String slotEndTime) {
        this.slotEndTime = slotEndTime;
    }

    public JrgCmsActivity getActivity() {
        return activity;
    }

    public void setActivity(JrgCmsActivity activity) {
        this.activity = activity;
    }
}
