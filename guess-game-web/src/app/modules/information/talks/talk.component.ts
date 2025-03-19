import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { YouTubePlayer } from '@angular/youtube-player';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../../shared/services/locale.service';
import { TalkDetails } from '../../../shared/models/talk/talk-details.model';
import { TalkService } from '../../../shared/services/talk.service';
import { VideoSize } from '../../../shared/models/talk/video-size.model';
import {
  getSpeakersWithCompaniesString,
  getTalkClassnameByFilename,
  getTalkTimes
} from '../../general/utility-functions';
import getVideoId from 'get-video-id';

@Component({
    selector: 'app-talk',
    templateUrl: './talk.component.html',
    standalone: false
})
export class TalkComponent implements OnInit, AfterViewInit, AfterViewChecked, OnDestroy {
  private imageDirectory = 'assets/images';
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public eventsImageDirectory = `${this.imageDirectory}/events`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;
  public twitterUrlPrefix = 'https://twitter.com';
  public gitHubUrlPrefix = 'https://github.com';
  public habrUrlPrefix = 'https://habr.com/users';

  public id: number;
  public talkDetails: TalkDetails = new TalkDetails();

  public language: string;
  private languageSubscription: Subscription;

  @ViewChild('youtubePlayerDiv') youtubePlayerDiv: ElementRef<HTMLDivElement>;
  @ViewChildren('youtubePlayer') youtubePlayers: QueryList<YouTubePlayer>;
  private initialVideoSizes: Map<string, VideoSize> = new Map<string, VideoSize>();
  private resizedVideoSizes: Map<string, VideoSize> = new Map<string, VideoSize>();
  private isVideoSizesInitialized = false;

  constructor(private talkService: TalkService, public translateService: TranslateService,
              private activatedRoute: ActivatedRoute, private changeDetector: ChangeDetectorRef,
              private localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe(params => {
      const idString: string = params['id'];
      const idNumber = Number(idString);

      if (!isNaN(idNumber)) {
        this.id = idNumber;
        this.loadTalk(this.id);

        this.languageSubscription = this.translateService.onLangChange
          .subscribe(() => {
            this.language = this.localeService.getLanguage();
            this.loadTalk(this.id);
          });
      }
    });
  }

  ngAfterViewInit(): void {
    window.addEventListener('resize', this.onResize);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);

    if (this.languageSubscription) {
      this.languageSubscription.unsubscribe();
    }
  }

  ngAfterViewChecked() {
    if (!this.isVideoSizesInitialized && this.youtubePlayerDiv && this.youtubePlayers) {
      let needResize = false;

      this.youtubePlayers.forEach((yp) => {
        const videoId = yp.videoId;
        const width = yp.width;
        const height = yp.height;

        if (videoId && !isNaN(width) && !isNaN(height)) {
          this.initialVideoSizes.set(videoId, new VideoSize(width, height));

          needResize = true;
        }
      });

      if (needResize) {
        this.onResize();
        this.changeDetector.detectChanges();
      }

      this.isVideoSizesInitialized = true;
    }
  }

  loadTalk(id: number) {
    if (this.translateService.currentLang) {
      this.talkService.getTalk(id, this.language)
        .subscribe(data => {
          this.talkDetails = this.getTalkDetailsWithFilledAttributes(data);

          this.addYouTubePlayer();
        });
    }
  }

  getTalkDetailsWithFilledAttributes(talkDetails: TalkDetails): TalkDetails {
    // Times
    if (talkDetails?.talk) {
      talkDetails.talk.displayTimes = getTalkTimes(talkDetails.talk.talkStartTime, talkDetails.talk.talkEndTime, this.translateService);
    }

    // YouTube video ids
    if (talkDetails?.talk?.videoLinks) {
      // const getVideoId = require('get-video-id');
      const videoLinksVideoIds: string[] = [];

      talkDetails.talk.videoLinks.forEach(v => {
          const videoId = getVideoId(v);

          if (videoId && (videoId['service'] === 'youtube')) {
            videoLinksVideoIds.push(videoId['id']);
          }
        }
      );

      talkDetails.talk.videoLinksVideoIds = videoLinksVideoIds;
    }

    // Company names of speakers
    if (talkDetails?.speakers) {
      talkDetails.speakers = getSpeakersWithCompaniesString(talkDetails.speakers);
    }

    return talkDetails;
  }

  addYouTubePlayer() {
    // This code loads the IFrame Player API code asynchronously, according to the instructions at
    // https://developers.google.com/youtube/iframe_api_reference#Getting_Started
    const tag = document.createElement('script');

    tag.src = 'https://www.youtube.com/iframe_api';
    document.body.appendChild(tag);
  }

  onResize = (): void => {
    if (this.youtubePlayerDiv && this.youtubePlayers) {
      this.youtubePlayers.forEach((yp) => {
        const videoSize = this.initialVideoSizes.get(yp.videoId);

        if (videoSize) {
          const videoWidth = Math.min(this.youtubePlayerDiv.nativeElement.clientWidth, videoSize.width);

          if (videoWidth != yp.width) {
            const videoHeight = videoWidth * videoSize.height / videoSize.width;

            this.resizedVideoSizes.set(yp.videoId, new VideoSize(videoWidth, videoHeight));
          }
        }
      });
    }
  }

  isSpeakersListVisible() {
    return ((this.talkDetails.speakers) && (this.talkDetails.speakers.length > 0));
  }

  isPresentationLinksListVisible() {
    return ((this.talkDetails.talk?.presentationLinks) && (this.talkDetails.talk.presentationLinks.length > 0));
  }

  isMaterialLinksListVisible() {
    return ((this.talkDetails.talk?.materialLinks) && (this.talkDetails.talk.materialLinks.length > 0));
  }

  isLinksListsVisible() {
    return this.isPresentationLinksListVisible() || this.isMaterialLinksListVisible();
  }

  isVideoLinksVideoIdsListVisible() {
    return ((this.talkDetails.talk?.videoLinksVideoIds) && (this.talkDetails.talk.videoLinksVideoIds.length > 0));
  }

  isMaterialsVisible() {
    return this.isLinksListsVisible() || this.isVideoLinksVideoIdsListVisible();
  }

  getPlayerWidth(videoId: string): number {
    const videoSize = this.resizedVideoSizes.get(videoId);

    return (videoSize) ? videoSize.width : null;
  }

  getPlayerHeight(videoId: string): number {
    const videoSize = this.resizedVideoSizes.get(videoId);

    return (videoSize) ? videoSize.height : null;
  }

  getClassnameByFilename(filename: string): string {
    return getTalkClassnameByFilename(filename);
  }
}
