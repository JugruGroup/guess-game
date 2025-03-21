package guess.dto.result;

import guess.domain.GuessMode;
import guess.domain.Language;
import guess.domain.answer.ErrorDetails;
import guess.domain.answer.SpeakerAnswer;
import guess.domain.question.SpeakerQuestion;
import guess.domain.source.Speaker;
import guess.util.LocalizationUtils;

import java.util.List;
import java.util.Set;

/**
 * Account error details DTO.
 */
public record AccountErrorDetailsDto(SpeakerPairDto speaker, String twitter, String gitHub,
                                     List<AccountAnswerDto> yourAnswers) {
    private static AccountErrorDetailsDto convertToDto(ErrorDetails errorDetails, GuessMode guessMode, Language language) {
        if (GuessMode.GUESS_ACCOUNT_BY_SPEAKER_MODE.equals(guessMode) || GuessMode.GUESS_SPEAKER_BY_ACCOUNT_MODE.equals(guessMode)) {
            List<Speaker> speakers = errorDetails.availableAnswers().stream()
                    .map(q -> ((SpeakerAnswer) q).getSpeaker())
                    .toList();

            Set<Speaker> speakerDuplicates = LocalizationUtils.getSpeakerDuplicates(
                    speakers,
                    s -> LocalizationUtils.getString(s.getName(), language),
                    s -> true);

            var questionSpeaker = ((SpeakerQuestion) errorDetails.question()).getSpeaker();

            List<AccountAnswerDto> yourAnswers = errorDetails.yourAnswers().stream()
                    .map(a -> GuessMode.GUESS_ACCOUNT_BY_SPEAKER_MODE.equals(guessMode) ?
                            new AccountAnswerDto(
                                    null,
                                    ((SpeakerAnswer) a).getSpeaker().getTwitter(),
                                    ((SpeakerAnswer) a).getSpeaker().getGitHub()) :
                            new AccountAnswerDto(
                                    new SpeakerPairDto(
                                            LocalizationUtils.getSpeakerName(((SpeakerAnswer) a).getSpeaker(), language, speakerDuplicates),
                                            ((SpeakerAnswer) a).getSpeaker().getPhotoFileName()),
                                    null,
                                    null))
                    .toList();

            return new AccountErrorDetailsDto(
                    new SpeakerPairDto(
                            LocalizationUtils.getSpeakerName(questionSpeaker, language, speakerDuplicates),
                            questionSpeaker.getPhotoFileName()),
                    questionSpeaker.getTwitter(),
                    questionSpeaker.getGitHub(),
                    yourAnswers);
        } else {
            throw new IllegalArgumentException(String.format("Unknown guess mode: %s", guessMode));
        }
    }

    public static List<AccountErrorDetailsDto> convertToDto(List<ErrorDetails> errorDetailsList, GuessMode guessMode, Language language) {
        return errorDetailsList.stream()
                .map(e -> convertToDto(e, guessMode, language))
                .toList();
    }
}
