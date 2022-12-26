export abstract class AbstractEvent {
  constructor(
    public id?: number,
    public eventTypeId?: number,
    public organizerId?: number,
    public name?: string,
    public siteLink?: string,
    public youtubeLink?: string,
    public eventTypeLogoFileName?: string,
    public displayName?: string,
    public vkLink?: string,
    public twitterLink?: string,
    public facebookLink?: string,
    public telegramLink?: string,
    public speakerdeckLink?: string,
    public habrLink?: string,
    public description?: string,
    public topicExist?: boolean
  ) {
  }
}
