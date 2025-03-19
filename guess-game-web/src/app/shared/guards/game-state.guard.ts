import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { GameState } from '../models/game-state.model';
import { getRouteParams } from '../../modules/general/utility-functions';
import { LocaleService } from '../services/locale.service';
import { MessageService } from '../../modules/message/message.service';
import { StateService } from '../services/state.service';

export const gameStateCanActivate: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const stateService = inject(StateService);
  const messageService = inject(MessageService);
  const localeService = inject(LocaleService);
  const router = inject(Router);
  const params = getRouteParams(route);
  const pathLanguageCode = params.language;
  const language = localeService.getInitialLanguage(pathLanguageCode);

  return stateService.getState()
    .pipe(
      map(gameState => {
          const actualUrl: string = state.url;
          let expectedUrl: string;

          switch (gameState) {
            case GameState.StartState: {
              expectedUrl = `/${language}/game/start`;
              break;
            }
            case GameState.GuessNameByPhotoState: {
              expectedUrl = `/${language}/game/guess/name-by-photo`;
              break;
            }
            case GameState.GuessPhotoByNameState: {
              expectedUrl = `/${language}/game/guess/photo-by-name`;
              break;
            }
            case GameState.GuessTalkBySpeakerState: {
              expectedUrl = `/${language}/game/guess/talk-by-speaker`;
              break;
            }
            case GameState.GuessSpeakerByTalkState: {
              expectedUrl = `/${language}/game/guess/speaker-by-talk`;
              break;
            }
            case GameState.GuessCompanyBySpeakerState:
              expectedUrl = `/${language}/game/guess/company-by-speaker`;
              break;
            case GameState.GuessSpeakerByCompanyState:
              expectedUrl = `/${language}/game/guess/speaker-by-company`;
              break;
            case GameState.GuessAccountBySpeakerState: {
              expectedUrl = `/${language}/game/guess/account-by-speaker`;
              break;
            }
            case GameState.GuessSpeakerByAccountState: {
              expectedUrl = `/${language}/game/guess/speaker-by-account`;
              break;
            }
            case GameState.GuessTagCloudBySpeakerState: {
              expectedUrl = `/${language}/game/guess/tag-cloud-by-speaker`;
              break;
            }
            case GameState.GuessSpeakerByTagCloudState: {
              expectedUrl = `/${language}/game/guess/speaker-by-tag-cloud`;
              break;
            }
            case GameState.ResultState: {
              expectedUrl = `/${language}/game/result`;
              break;
            }
          }

          if (actualUrl === expectedUrl) {
            return true;
          } else {
            router.navigateByUrl(expectedUrl);
          }
        }
      ),
      catchError(response => {
          messageService.reportMessage(response);
          return of(false);
        }
      )
    );
};
