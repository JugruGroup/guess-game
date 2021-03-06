import { Company } from '../company/company.model';

export class Speaker {
  constructor(
    public id?: number,
    public photoFileName?: string,
    public displayName?: string,
    public name?: string,
    public companies?: Company[],
    public companiesString?: string,
    public bio?: string,
    public twitter?: string,
    public gitHub?: string,
    public habr?: string,
    public javaChampion?: boolean,
    public mvp?: boolean,
    public mvpReconnect?: boolean,
    public anyMvp?: boolean
  ) {
  }
}
