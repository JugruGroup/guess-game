import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { LocaleService } from '../../shared/services/locale.service';

export abstract class EntitiesListComponent {
  protected readonly DEFAULT_LETTER = 'A';
  protected readonly RUSSIAN_LANG = 'ru';

  public enLetters: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
    'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
  public ruLetters: string[] = ['А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р',
    'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Э', 'Ю', 'Я'];

  public selectedLetter = this.DEFAULT_LETTER;
  public paginatorFirst = 0;

  public language: string;
  protected languageSubscription: Subscription;

  protected constructor(public translateService: TranslateService, protected localeService: LocaleService) {
    this.language = localeService.getLanguage();
  }

  abstract changeLetter(letter: string);

  onLanguageChange() {
    this.changeLetter(this.DEFAULT_LETTER);
  }

  isRuLettersVisible(): boolean {
    return (this.translateService.currentLang === this.RUSSIAN_LANG);
  }
}
