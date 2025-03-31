export class EventPlaceMetrics {
  constructor(
    public id?: number,
    public placeCity?: string,
    public placeVenueAddress?: string,
    public mapCoordinates?: string,
    public startDate?: Date,
    public endDate?: Date,
    public duration?: number,
    public eventsQuantity?: number,
    public eventTypesQuantity?: number,
    public talksQuantity?: number,
    public speakersQuantity?: number,
    public companiesQuantity?: number,
    public javaChampionsQuantity?: number,
    public mvpsQuantity?: number
  ) {
  }
}
