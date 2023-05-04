import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { StateService } from '../services/state.service';
import { MessageService } from '../../modules/message/message.service';
import { GameState } from '../models/game-state.model';

export const gameStateCanActivate: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const stateService = inject(StateService);
  const messageService = inject(MessageService);
  const router = inject(Router);

  return stateService.getState()
    .pipe(
      map(gameState => {
          const actualUrl: string = state.url;
          let expectedUrl: string;

          switch (gameState) {
            case GameState.StartState: {
              expectedUrl = '/game/start';
              break;
            }
            case GameState.GuessNameByPhotoState: {
              expectedUrl = '/game/guess/name-by-photo';
              break;
            }
            case GameState.GuessPhotoByNameState: {
              expectedUrl = '/game/guess/photo-by-name';
              break;
            }
            case GameState.GuessTalkBySpeakerState: {
              expectedUrl = '/game/guess/talk-by-speaker';
              break;
            }
            case GameState.GuessSpeakerByTalkState: {
              expectedUrl = '/game/guess/speaker-by-talk';
              break;
            }
            case GameState.GuessCompanyBySpeakerState:
              expectedUrl = '/game/guess/company-by-speaker';
              break;
            case GameState.GuessSpeakerByCompanyState:
              expectedUrl = '/game/guess/speaker-by-company';
              break;
            case GameState.GuessAccountBySpeakerState: {
              expectedUrl = '/game/guess/account-by-speaker';
              break;
            }
            case GameState.GuessSpeakerByAccountState: {
              expectedUrl = '/game/guess/speaker-by-account';
              break;
            }
            case GameState.GuessTagCloudBySpeakerState: {
              expectedUrl = '/game/guess/tag-cloud-by-speaker';
              break;
            }
            case GameState.GuessSpeakerByTagCloudState: {
              expectedUrl = '/game/guess/speaker-by-tag-cloud';
              break;
            }
            case GameState.ResultState: {
              expectedUrl = '/game/result';
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
