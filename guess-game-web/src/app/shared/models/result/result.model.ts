import { GuessMode } from '../guess-mode.model';
import { SpeakerErrorDetails } from './speaker-error-details.model';
import { TalkErrorDetails } from './talk-error-details.model';
import { CompanyErrorDetails } from './company-error-details.model';
import { AccountErrorDetails } from './account-error-details.model';
import { TagCloudErrorDetails } from './tag-cloud-error-details.model';

export class Result {
  constructor(
    public correctAnswers?: number,
    public wrongAnswers?: number,
    public skippedAnswers?: number,
    public correctPercents?: number,
    public wrongPercents?: number,
    public skippedPercents?: number,
    public guessMode?: GuessMode,
    public speakerErrorDetailsList?: SpeakerErrorDetails[],
    public talkErrorDetailsList?: TalkErrorDetails[],
    public companyErrorDetailsList?: CompanyErrorDetails[],
    public accountErrorDetailsList?: AccountErrorDetails[],
    public tagCloudErrorDetailsList?: TagCloudErrorDetails[]
  ) {
  }
}
