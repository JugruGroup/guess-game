export abstract class OlapEntityMetrics {
  protected constructor(
    public id?: number,
    public name?: string,
    public measureValues?: number[],
    public cumulativeMeasureValues?: number[],
    public total?: number
  ) {
  }
}
