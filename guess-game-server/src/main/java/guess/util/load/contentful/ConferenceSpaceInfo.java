package guess.util.load.contentful;

import guess.domain.source.cms.contentful.talk.fields.ContentfulTalkFields;
import guess.domain.source.cms.contentful.talk.response.*;

public enum ConferenceSpaceInfo {
    // Joker, JPoint, JBreak, TechTrain, C++ Russia, Hydra, SPTDC, DevOops, SmartData
    COMMON_SPACE_INFO("oxjq45e8ilak", "fdc0ca21c8c39ac5a33e1e20880cae6836ae837af73c2cfc822650483ee388fe",
            ConferenceSpaceInfo.FIELDS_SPEAKER_FIELD_NAME, ConferenceSpaceInfo.FIELDS_CONFERENCES_FIELD_NAME, "fields.javaChampion",
            "fields.talksPresentation,fields.talksPresentationLink", ContentfulTalkResponseCommon.class),                   // fields.talksPresentation is list, fields.talksPresentationLink is single value
    // HolyJS
    HOLY_JS_SPACE_INFO("nn534z2fqr9f", "1ca5b5d059930cd6681083617578e5a61187d1a71cbd75d4e0059cca3dc85f8c",
            "fields.speakers", "fields.conference", null,
            "fields.presentation,fields.presentationLink", ContentfulTalkResponseHolyJs.class),                             // fields.presentation is single value, fields.presentationLink is single value
    // DotNext
    DOT_NEXT_SPACE_INFO("9n3x4rtjlya6", "14e1427f8fbee9e5a089cd634fc60189c7aff2814b496fb0ad957b867a59503b",
            ConferenceSpaceInfo.FIELDS_SPEAKER_FIELD_NAME, "fields.conference", "fields.mvp,fields.mvpReconnect",
            "fields.talksPresentation,fields.presentation,fields.talksPresentationLink", ContentfulTalkResponseDotNext.class),   // fields.talksPresentation is list, fields.presentation is single value, fields.talksPresentationLink is single value
    // Heisenbug
    HEISENBUG_SPACE_INFO("ut4a3ciohj8i", "e7edd5951d844b80ef41166e30cb9645e4f89d11c8ac9eecdadb2a38c061b980",
            ConferenceSpaceInfo.FIELDS_SPEAKER_FIELD_NAME, ConferenceSpaceInfo.FIELDS_CONFERENCES_FIELD_NAME, null,
            "fields.talksPresentation,fields.talksPresentationLink", ContentfulTalkResponseHeisenbug.class),                // talksPresentation is single value, fields.talksPresentationLink is single value
    // Mobius
    MOBIUS_SPACE_INFO("2grufn031spf", "d0c680ed11f68287348b6b8481d3313fde8c2d23cc8ce24a2b0ae254dd779e6d",
            ConferenceSpaceInfo.FIELDS_SPEAKER_FIELD_NAME, ConferenceSpaceInfo.FIELDS_CONFERENCES_FIELD_NAME, null,
            "fields.talkPresentation,fields.presentationLink", ContentfulTalkResponseMobius.class);                         // talkPresentation is list, fields.presentationLink is single value

    private static final String FIELDS_SPEAKER_FIELD_NAME = "fields.speaker";
    private static final String FIELDS_CONFERENCES_FIELD_NAME = "fields.conferences";

    private final String spaceId;
    private final String accessToken;

    private final String speakerFlagFieldName;
    private final String conferenceFieldName;

    private final String speakerAdditionalFieldNames;
    private final String talkAdditionalFieldNames;
    private final Class<? extends ContentfulTalkResponse<? extends ContentfulTalkFields>> talkResponseClass;

    ConferenceSpaceInfo(String spaceId, String accessToken, String speakerFlagFieldName, String conferenceFieldName,
                        String speakerAdditionalFieldNames, String talkAdditionalFieldNames,
                        Class<? extends ContentfulTalkResponse<? extends ContentfulTalkFields>> talkResponseClass) {
        this.spaceId = spaceId;
        this.accessToken = accessToken;
        this.speakerFlagFieldName = speakerFlagFieldName;
        this.conferenceFieldName = conferenceFieldName;
        this.speakerAdditionalFieldNames = speakerAdditionalFieldNames;
        this.talkAdditionalFieldNames = talkAdditionalFieldNames;
        this.talkResponseClass = talkResponseClass;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getSpeakerFlagFieldName() {
        return speakerFlagFieldName;
    }

    public String getConferenceFieldName() {
        return conferenceFieldName;
    }

    public String getSpeakerAdditionalFieldNames() {
        return speakerAdditionalFieldNames;
    }

    public String getTalkAdditionalFieldNames() {
        return talkAdditionalFieldNames;
    }

    public Class<? extends ContentfulTalkResponse<? extends ContentfulTalkFields>> getTalkResponseClass() {
        return talkResponseClass;
    }
}
