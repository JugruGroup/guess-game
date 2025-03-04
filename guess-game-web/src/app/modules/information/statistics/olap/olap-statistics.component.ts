import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { formatPercent } from '@angular/common';
import { SelectItem } from 'primeng/api';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { TranslateService } from '@ngx-translate/core';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { Company } from '../../../../shared/models/company/company.model';
import { EventType } from '../../../../shared/models/event-type/event-type.model';
import { OlapChartKind } from '../../../../shared/models/statistics/olap/olap-chart-kind.model';
import { OlapChartType } from '../../../../shared/models/statistics/olap/olap-chart-type.model';
import { OlapCityMetrics } from '../../../../shared/models/statistics/olap/metrics/olap-city-metrics.model';
import { OlapCityParameters } from '../../../../shared/models/statistics/olap/parameters/olap-city-parameters.model';
import { OlapCompanyMetrics } from '../../../../shared/models/statistics/olap/metrics/olap-company-metrics.model';
import {
  Olap3dCubeStatistics
} from '../../../../shared/models/statistics/olap/statistics/olap-3d-cube-statistics.model';
import { OlapCubeType } from '../../../../shared/models/statistics/olap/olap-cube-type.model';
import { OlapEntityMetrics } from '../../../../shared/models/statistics/olap/metrics/olap-entity-metrics.model';
import {
  OlapEntityStatistics
} from '../../../../shared/models/statistics/olap/statistics/olap-entity-statistics.model';
import { OlapEventTypeMetrics } from '../../../../shared/models/statistics/olap/metrics/olap-event-type-metrics.model';
import {
  OlapEventTypeParameters
} from '../../../../shared/models/statistics/olap/parameters/olap-event-type-parameters.model';
import { OlapMeasureType } from '../../../../shared/models/statistics/olap/olap-measure-type.model';
import { OlapSpeakerMetrics } from '../../../../shared/models/statistics/olap/metrics/olap-speaker-metrics.model';
import {
  OlapSpeakerParameters
} from '../../../../shared/models/statistics/olap/parameters/olap-speaker-parameters.model';
import { OlapParameters } from '../../../../shared/models/statistics/olap/parameters/olap-parameters.model';
import { OlapStatistics } from '../../../../shared/models/statistics/olap/statistics/olap-statistics.model';
import { Organizer } from '../../../../shared/models/organizer/organizer.model';
import { SelectedEntities } from '../../../../shared/models/common/selected-entities.model';
import { Speaker } from '../../../../shared/models/speaker/speaker.model';
import { EventTypeService } from '../../../../shared/services/event-type.service';
import { EventService } from '../../../../shared/services/event.service';
import { LocaleService } from '../../../../shared/services/locale.service';
import { OrganizerService } from '../../../../shared/services/organizer.service';
import { StatisticsService } from '../../../../shared/services/statistics.service';
import { SpeakerService } from '../../../../shared/services/speaker.service';
import { CompanyService } from '../../../../shared/services/company.service';
import {
  findEventTypesByIds,
  findOrganizerById,
  fixOlapEntityStatistics,
  getColorByIndex,
  getOlapEventTypeStatisticsWithSortName,
  hexToRgbA
} from '../../../general/utility-functions';
import { ChartZoomInComponent } from './chart/chart-zoom-in.component';
import { DynamicDialogChartData } from '../../../../shared/models/statistics/olap/dynamic-dialog-chart-data.model';
import {
  ThreeDimensionsCubeData
} from '../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-data.model';
import {
  ThreeDimensionsCubeOptions
} from '../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-options.model';
import { ThreeDimensionsZoomInComponent } from './three-dimensions/three-dimensions-zoom-in.component';
import { Olap3dCubeMetrics } from '../../../../shared/models/statistics/olap/metrics/olap-3d-cube-metrics.model';
import {
  ThreeDimensionsCubeDataset
} from "../../../../shared/models/statistics/olap/three-dimensions/cube/three-dimensions-cube-dataset.model";

@Component({
  selector: 'app-olap-statistics',
  templateUrl: './olap-statistics.component.html',
  providers: [DialogService],
  standalone: false
})
export class OlapStatisticsComponent implements AfterViewInit, OnDestroy, OnInit {
  private readonly EVENT_TYPES_CUBE_TYPE_KEY = 'cubeType.eventTypes';
  private readonly SPEAKERS_CUBE_TYPE_KEY = 'cubeType.speakers';
  private readonly COMPANIES_CUBE_TYPE_KEY = 'cubeType.companies';

  private readonly DURATION_MEASURE_TYPE_KEY = 'measureType.duration';
  private readonly EVENT_TYPES_QUANTITY_MEASURE_TYPE_KEY = 'measureType.eventTypesQuantity';
  private readonly EVENTS_QUANTITY_MEASURE_TYPE_KEY = 'measureType.eventsQuantity';
  private readonly TALKS_QUANTITY_MEASURE_TYPE_KEY = 'measureType.talksQuantity';
  private readonly SPEAKERS_QUANTITY_MEASURE_TYPE_KEY = 'measureType.speakersQuantity';
  private readonly COMPANIES_QUANTITY_MEASURE_TYPE_KEY = 'measureType.companiesQuantity';
  private readonly JAVA_CHAMPIONS_QUANTITY_MEASURE_TYPE_KEY = 'measureType.javaChampionsQuantity';
  private readonly MVPS_QUANTITY_MEASURE_TYPE_KEY = 'measureType.mvpsQuantity';

  private readonly TOTAL_LABEL_KEY = 'statistics.olap.chart.totalLabel';

  private readonly MEASURE_VALUE_FIELD_NAME_PREFIX = 'measureValue';

  private readonly EVENT_TYPE_CHART_DATASET_QUANTITY = -1;
  private readonly SPEAKER_CHART_DATASET_QUANTITY = 5;
  private readonly COMPANY_CHART_DATASET_QUANTITY = 5;

  private readonly MARGIN_WIDTH = 30;
  private readonly SMALL_WIDTH = 576 - this.MARGIN_WIDTH;
  private readonly MEDIUM_WIDTH = 768 - this.MARGIN_WIDTH;
  private readonly LARGE_WIDTH = 1200 - this.MARGIN_WIDTH;
  private readonly EXTRA_LARGE_WIDTH = 1400 - this.MARGIN_WIDTH;

  private readonly EXTRA_SMALL_ASPECT_RATIO = 1.5;
  private readonly SMALL_ASPECT_RATIO = 2.25;
  private readonly MEDIUM_ASPECT_RATIO = 3;
  private readonly LARGE_ASPECT_RATIO = 3.75;
  private readonly EXTRA_LARGE_ASPECT_RATIO = 5;

  private readonly EXTRA_SMALL_BOX_WIDTH = 20;
  private readonly SMALL_BOX_WIDTH = 30;
  private readonly DEFAULT_BOX_WIDTH = 40;

  private readonly DEFAULT_CUBE_CANVAS_ASPECT_RATIO = 1;
  private readonly DEFAULT_CUBE_CANVAS_HEIGHT = '100px';

  private imageDirectory = 'assets/images';
  public eventsImageDirectory = `${this.imageDirectory}/events`;
  public degreesImageDirectory = `${this.imageDirectory}/degrees`;
  public speakersImageDirectory = `${this.imageDirectory}/speakers`;

  public cubeTypes: OlapCubeType[] = [];
  public selectedCubeType: OlapCubeType;
  public cubeTypeSelectItems: SelectItem[] = [];

  public measureTypes: OlapMeasureType[] = [];
  public selectedMeasureType: OlapMeasureType;
  public measureTypeSelectItems: SelectItem[] = [];

  public isConferences = true;
  public isMeetups = true;

  public organizers: Organizer[] = [];
  public selectedOrganizer: Organizer;
  public organizerSelectItems: SelectItem[] = [];

  public eventTypes: EventType[] = [];
  public selectedEventTypes: EventType[] = [];
  public eventTypeSelectItems: SelectItem[] = [];

  public selectedSpeakers: Speaker[] = [];
  public speakerSuggestions: Speaker[];

  public selectedCompanies: Company[] = [];
  public companySuggestions: Company[];

  public olapStatistics = new OlapStatistics();
  public eventTypeMultiSortMeta: any[] = [];
  public cityMultiSortMeta: any[] = [];
  public speakerMultiSortMeta: any[] = [];
  public companyMultiSortMeta: any[] = [];

  public eventTypeExpandedRows = {};
  public speakerExpandedRows = {};
  public companyExpandedRows = {};

  public lineOptions: any = {};
  public pieOptions: any = {};
  public radarOptions: any = {};
  public cubeOptions: ThreeDimensionsCubeOptions = new ThreeDimensionsCubeOptions(this.DEFAULT_CUBE_CANVAS_ASPECT_RATIO, this.DEFAULT_CUBE_CANVAS_HEIGHT);

  public allLineData: any = {};
  public totalLineData: any = {};
  public allLineWithCumulativeData: any = {};
  public totalLineWithCumulativeData: any = {};
  public pieData: any = {};
  public allRadarData: any = {};
  public totalRadarData: any = {};
  public cubeData: ThreeDimensionsCubeData = new ThreeDimensionsCubeData([], [], []);

  public chartTypes: SelectItem[] = [
    {label: 'statistics.olap.chart.detailsLabel', value: OlapChartType.Details},
    {label: 'statistics.olap.chart.totalLabel', value: OlapChartType.Total}
  ];
  public selectedChartType = OlapChartType.Details;

  public chartKinds: SelectItem[] = [
    {icon: 'bi-graph-up', value: OlapChartKind.Line, label: 'statistics.olap.chart.lineChartToolTipText'},
    {
      icon: 'bi-graph-up-arrow',
      value: OlapChartKind.LineWithCumulativeTotal,
      label: 'statistics.olap.chart.lineChartWithCumulativeTotalToolTipText'
    },
    {icon: 'bi-pie-chart', value: OlapChartKind.Pie, label: 'statistics.olap.chart.pieChartToolTipText'},
    {icon: 'bi-pentagon', value: OlapChartKind.Radar, label: 'statistics.olap.chart.radarChartToolTipText'},
    {icon: 'bi-box', value: OlapChartKind.Cube, label: 'statistics.olap.chart.3dCubeToolTipText'}];
  public selectedChartKind = OlapChartKind.Line;

  @ViewChildren('chartDiv') chartDivs: QueryList<ElementRef<HTMLDivElement>>;
  private chartDiv: ElementRef<HTMLDivElement>;

  public chartPlugins = [ChartDataLabels];

  private topicMetricsMap = new Map<number, OlapEntityMetrics>();
  private cubeMetricsMap = new Map<number, Olap3dCubeMetrics>();

  private zoomInDialogRef: DynamicDialogRef;
  public zoomInDialogClosed = true;

  public language: string;

  constructor(private statisticsService: StatisticsService, private eventTypeService: EventTypeService,
              private eventService: EventService, private organizerService: OrganizerService,
              public translateService: TranslateService, private speakerService: SpeakerService,
              private companyService: CompanyService, public dialogService: DialogService,
              private localeService: LocaleService) {
    this.eventTypeMultiSortMeta.push({field: 'sortName', order: 1});

    this.cityMultiSortMeta.push({field: 'name', order: 1});

    this.speakerMultiSortMeta.push({field: 'total', order: -1});
    this.speakerMultiSortMeta.push({field: 'name', order: 1});

    this.companyMultiSortMeta.push({field: 'total', order: -1});
    this.companyMultiSortMeta.push({field: 'name', order: 1});

    this.language = localeService.getLanguage();
  }

  ngOnInit(): void {
    this.loadCubeTypes();
    this.fillChartKinds();

    this.translateService.onLangChange
      .subscribe(() => {
          this.language = this.localeService.getLanguage();
          this.loadOrganizers();
          this.fillChartKinds();
          this.fillChartOptions();
        }
      );
  }

  ngAfterViewInit(): void {
    window.addEventListener('resize', this.onResize);

    this.chartDivs.changes.subscribe((divs) => {
      this.chartDiv = divs.first;
      this.onResize();
    });
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onResize);
  }

  getCubeTypeMessageKeyByCube(cubeType: OlapCubeType): string {
    switch (cubeType) {
      case OlapCubeType.EventTypes: {
        return this.EVENT_TYPES_CUBE_TYPE_KEY;
      }
      case OlapCubeType.Speakers: {
        return this.SPEAKERS_CUBE_TYPE_KEY;
      }
      case OlapCubeType.Companies: {
        return this.COMPANIES_CUBE_TYPE_KEY;
      }
      default: {
        return null;
      }
    }
  }

  getMeasureTypeMessageKeyByCube(measureType: OlapMeasureType): string {
    switch (measureType) {
      case OlapMeasureType.Duration: {
        return this.DURATION_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.EventTypesQuantity: {
        return this.EVENT_TYPES_QUANTITY_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.EventsQuantity: {
        return this.EVENTS_QUANTITY_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.TalksQuantity: {
        return this.TALKS_QUANTITY_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.SpeakersQuantity: {
        return this.SPEAKERS_QUANTITY_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.CompaniesQuantity: {
        return this.COMPANIES_QUANTITY_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.JavaChampionsQuantity: {
        return this.JAVA_CHAMPIONS_QUANTITY_MEASURE_TYPE_KEY;
      }
      case OlapMeasureType.MvpsQuantity: {
        return this.MVPS_QUANTITY_MEASURE_TYPE_KEY;
      }
      default: {
        return null;
      }
    }
  }

  fillCubeTypes(cubeTypes: OlapCubeType[]) {
    this.cubeTypes = cubeTypes;
    this.cubeTypeSelectItems = this.cubeTypes.map(c => {
        const messageKey = this.getCubeTypeMessageKeyByCube(c);

        return {label: messageKey, value: c};
      }
    );
  }

  fillMeasureTypes(measureTypes: OlapMeasureType[]) {
    this.measureTypes = measureTypes;
    this.measureTypeSelectItems = this.measureTypes.map(m => {
        const messageKey = this.getMeasureTypeMessageKeyByCube(m);

        return {label: messageKey, value: m};
      }
    );
  }

  fillOrganizers(organizers: Organizer[]) {
    this.organizers = organizers;
    this.organizerSelectItems = this.organizers.map(o => {
        return {label: o.name, value: o};
      }
    );
  }

  fillEventTypes(eventTypes: EventType[]) {
    this.eventTypes = eventTypes;
    this.eventTypeSelectItems = this.eventTypes.map(et => {
        return {label: et.name, value: et};
      }
    );
  }

  fillChartKinds() {
    if (this.translateService.currentLang) {
      const keys = this.chartKinds
        .map(e => e.label);

      this.translateService.get(keys)
        .subscribe(data => {
          this.chartKinds = this.chartKinds
            .map(e => {
              e.title = data[e.label];

              return e;
            });
        });
    }
  }

  fillChartOptions() {
    if (this.chartDiv) {
      const clientWidth = this.chartDiv.nativeElement.clientWidth;
      const aspectRatio = this.getAspectRatio(clientWidth);
      const boxWidth = this.getBoxWidth(clientWidth);

      if (clientWidth > 0) {
        this.lineOptions = this.createLineOptions(true, aspectRatio, boxWidth);
        this.pieOptions = this.createPieOptions(true, aspectRatio, boxWidth);
        this.radarOptions = this.createRadarOptions(true, aspectRatio, boxWidth);
        this.cubeOptions = this.createCubeOptions(aspectRatio, clientWidth / aspectRatio);
      }
    }
  }

  loadCubeTypes() {
    this.statisticsService.getCubeTypes()
      .subscribe(cubeTypeData => {
        this.fillCubeTypes(cubeTypeData);
        this.selectedCubeType = (cubeTypeData && (cubeTypeData.length > 0)) ? cubeTypeData[0] : null;

        this.statisticsService.getMeasureTypes(this.selectedCubeType)
          .subscribe(measureTypeData => {
            this.selectedMeasureType = (measureTypeData && (measureTypeData.length > 0)) ? measureTypeData[0] : null;
            this.fillMeasureTypes(measureTypeData);

            this.organizerService.getOrganizers(this.language)
              .subscribe(organizerData => {
                this.fillOrganizers(organizerData);

                this.eventService.getDefaultEvent(this.language)
                  .subscribe(defaultEventData => {
                    this.selectedOrganizer = (defaultEventData) ? findOrganizerById(defaultEventData.organizerId, this.organizers) : null;

                    this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
                      .subscribe(eventTypesData => {
                        this.fillEventTypes(eventTypesData);

                        if (this.eventTypes.length > 0) {
                          this.selectedEventTypes = (defaultEventData) ? findEventTypesByIds([defaultEventData.eventTypeId], this.eventTypes) : [];
                        } else {
                          this.selectedEventTypes = [];
                        }

                        this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
                          this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
                      });
                  });
              });
          });
      });
  }

  loadOrganizers() {
    const currentSelectedOrganizer = this.selectedOrganizer;
    const currentSelectedEventTypes = this.selectedEventTypes;

    this.organizerService.getOrganizers(this.language)
      .subscribe(organizerData => {
        this.fillOrganizers(organizerData);

        this.selectedOrganizer = (currentSelectedOrganizer) ? findOrganizerById(currentSelectedOrganizer.id, this.organizers) : null;

        this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
          .subscribe(eventTypesData => {
            this.fillEventTypes(eventTypesData);

            if (this.eventTypes.length > 0) {
              this.selectedEventTypes = (currentSelectedEventTypes) ? findEventTypesByIds(currentSelectedEventTypes.map(et => et.id), this.eventTypes) : [];
            } else {
              this.selectedEventTypes = [];
            }

            this.loadSelectedEntities(() => {
              this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
                this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
            });
          });
      });
  }

  loadEventTypes() {
    this.eventTypeService.getFilterEventTypes(this.isConferences, this.isMeetups, this.selectedOrganizer, this.language)
      .subscribe(eventTypesData => {
        this.fillEventTypes(eventTypesData);

        this.selectedEventTypes = [];

        this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
          this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
      });
  }

  loadSelectedEntities(complete?: (() => void)) {
    switch (this.selectedCubeType) {
      case OlapCubeType.EventTypes:
        complete();
        break;
      case OlapCubeType.Speakers:
        this.speakerService.getSelectedSpeakers(new SelectedEntities(this.selectedSpeakers.map(s => s.id)))
          .subscribe(speakersData => {
            this.selectedSpeakers = speakersData;
            complete();
          });
        break;
      case OlapCubeType.Companies:
        this.companyService.getSelectedCompanies(new SelectedEntities(this.selectedCompanies.map(c => c.id)))
          .subscribe(companiesData => {
            this.selectedCompanies = companiesData;
            complete();
          });
    }
  }

  loadOlapStatistics(cubeType: OlapCubeType, measureType: OlapMeasureType, isConferences: boolean, isMeetups: boolean,
                     organizer: Organizer, eventTypes: EventType[], speakers: Speaker[], companies: Company[]) {
    this.statisticsService.getOlapStatistics(
      new OlapParameters(
        cubeType,
        measureType,
        isConferences,
        isMeetups,
        (organizer) ? organizer.id : null,
        (eventTypes) ? eventTypes.map(et => et.id) : null,
        (speakers) ? speakers.map(s => s.id) : null,
        (companies) ? companies.map(c => c.id) : null))
      .subscribe(data => {
          const olapStatistics = data;

          if (olapStatistics?.eventTypeStatistics) {
            olapStatistics.eventTypeStatistics = getOlapEventTypeStatisticsWithSortName(olapStatistics.eventTypeStatistics);
            fixOlapEntityStatistics(olapStatistics.eventTypeStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            this.eventTypeExpandedRows = {};

            this.loadLineChartTotalData(olapStatistics.eventTypeStatistics);
            this.loadLineChartWithCumulativeTotalData(olapStatistics.eventTypeStatistics);
          }

          if (olapStatistics?.speakerStatistics) {
            fixOlapEntityStatistics(olapStatistics.speakerStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            this.speakerExpandedRows = {};

            this.loadLineChartTotalData(olapStatistics.speakerStatistics);
            this.loadLineChartWithCumulativeTotalData(olapStatistics.speakerStatistics);
          }

          if (olapStatistics?.companyStatistics) {
            fixOlapEntityStatistics(olapStatistics.companyStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            this.companyExpandedRows = {};

            this.loadLineChartTotalData(olapStatistics.companyStatistics);
            this.loadLineChartWithCumulativeTotalData(olapStatistics.companyStatistics);
          }

          if (olapStatistics?.topicStatistics) {
            fixOlapEntityStatistics(olapStatistics.topicStatistics, null, false);
            this.loadRadarChartTotalData(olapStatistics.topicStatistics);
          }

          this.fillTopicMetricsMap(olapStatistics.topicStatistics);
          this.fill3dCubeMetricsMap(olapStatistics.threeDimensionsStatistics);

          this.olapStatistics = olapStatistics;
        }
      );
  }

  fillTopicMetricsMap(olapEntityStatistics: OlapEntityStatistics<string, OlapEntityMetrics>) {
    this.topicMetricsMap.clear();

    olapEntityStatistics.metricsList.forEach(m => this.topicMetricsMap.set(m.id, m));
  }

  fill3dCubeMetricsMap(olap3dCubeStatistics: Olap3dCubeStatistics) {
    this.cubeMetricsMap.clear();

    olap3dCubeStatistics.metricsList.forEach(m => this.cubeMetricsMap.set(m.id, m));
  }

  onCubeTypeChange() {
    this.selectedSpeakers = [];
    this.selectedCompanies = [];

    this.statisticsService.getMeasureTypes(this.selectedCubeType)
      .subscribe(measureTypeData => {
        this.selectedMeasureType = (measureTypeData && (measureTypeData.length > 0)) ? measureTypeData[0] : null;
        this.fillMeasureTypes(measureTypeData);

        this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
          this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
      });
  }

  onMeasureTypeChange() {
    this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
      this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
  }

  onEventTypeKindChange() {
    this.loadEventTypes();
  }

  onOrganizerChange() {
    this.loadEventTypes();
  }

  onEventTypeChange() {
    this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
      this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
  }

  isSpeakersVisible(): boolean {
    return (OlapCubeType.Speakers === this.selectedCubeType);
  }

  isCompaniesVisible(): boolean {
    return (OlapCubeType.Companies == this.selectedCubeType);
  }

  speakerSearch(event) {
    this.speakerService.getSpeakersByFirstLetters(event.query)
      .subscribe(data => {
          this.speakerSuggestions = data;
        }
      );
  }

  selectSpeaker() {
    this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
      this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
  }

  unselectSpeaker() {
    this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
      this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
  }

  companySearch(event) {
    this.companyService.getCompaniesByFirstLetters(event.query)
      .subscribe(data => {
          this.companySuggestions = data;
        }
      );
  }

  selectCompany() {
    this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
      this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
  }

  unselectCompany() {
    this.loadOlapStatistics(this.selectedCubeType, this.selectedMeasureType, this.isConferences, this.isMeetups,
      this.selectedOrganizer, this.selectedEventTypes, this.selectedSpeakers, this.selectedCompanies);
  }

  isNoEventTypesDataFoundVisible() {
    return ((this.selectedCubeType === OlapCubeType.EventTypes) &&
      this.olapStatistics?.eventTypeStatistics?.metricsList &&
      (this.olapStatistics.eventTypeStatistics.metricsList.length === 0));
  }

  isEventTypesListVisible() {
    return ((this.selectedCubeType === OlapCubeType.EventTypes) &&
      this.olapStatistics?.eventTypeStatistics?.metricsList &&
      (this.olapStatistics.eventTypeStatistics.metricsList.length > 0));
  }

  isNoSpeakersDataFoundVisible() {
    return ((this.selectedCubeType === OlapCubeType.Speakers) &&
      this.olapStatistics?.speakerStatistics?.metricsList &&
      (this.olapStatistics.speakerStatistics.metricsList.length === 0));
  }

  isSpeakersListVisible() {
    return ((this.selectedCubeType === OlapCubeType.Speakers) &&
      this.olapStatistics?.speakerStatistics?.metricsList &&
      (this.olapStatistics.speakerStatistics.metricsList.length > 0));
  }

  isNoCompaniesDataFoundVisible() {
    return ((this.selectedCubeType === OlapCubeType.Companies) &&
      this.olapStatistics?.companyStatistics?.metricsList &&
      (this.olapStatistics.companyStatistics.metricsList.length === 0));
  }

  isCompaniesListVisible() {
    return ((this.selectedCubeType === OlapCubeType.Companies) &&
      this.olapStatistics?.companyStatistics?.metricsList &&
      (this.olapStatistics.companyStatistics.metricsList.length > 0));
  }

  getMeasureValueFieldNamePrefix(num: number): string {
    return this.MEASURE_VALUE_FIELD_NAME_PREFIX + num;
  }

  eventTypeRowExpand(event) {
    const eventTypeMetrics: OlapEventTypeMetrics = event.data;

    if (!eventTypeMetrics.cityStatistics) {
      this.statisticsService.getOlapCityStatistics(
        new OlapCityParameters(
          this.selectedCubeType,
          this.selectedMeasureType,
          eventTypeMetrics.id
        ))
        .subscribe(data => {
            const olapCityStatistics: OlapEntityStatistics<number, OlapCityMetrics> = data;

            fixOlapEntityStatistics(olapCityStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            eventTypeMetrics.cityStatistics = olapCityStatistics;
          }
        );
    }
  }

  speakerRowExpand(event) {
    const speakerMetrics: OlapSpeakerMetrics = event.data;

    if (!speakerMetrics.eventTypeStatistics) {
      this.statisticsService.getOlapEventTypeStatistics(
        new OlapEventTypeParameters(
          this.selectedCubeType,
          this.selectedMeasureType,
          this.isConferences,
          this.isMeetups,
          (this.selectedOrganizer) ? this.selectedOrganizer.id : null,
          (this.selectedEventTypes) ? this.selectedEventTypes.map(et => et.id) : null,
          speakerMetrics.id,
          null
        ))
        .subscribe(data => {
            const olapEventTypeStatistics: OlapEntityStatistics<number, OlapEventTypeMetrics> = data;

            fixOlapEntityStatistics(olapEventTypeStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            speakerMetrics.eventTypeStatistics = getOlapEventTypeStatisticsWithSortName(olapEventTypeStatistics);
          }
        );
    }
  }

  companyRowExpand(event) {
    const companyMetrics: OlapCompanyMetrics = event.data;

    if (!companyMetrics.eventTypeStatistics) {
      this.statisticsService.getOlapEventTypeStatistics(
        new OlapEventTypeParameters(
          this.selectedCubeType,
          this.selectedMeasureType,
          this.isConferences,
          this.isMeetups,
          (this.selectedOrganizer) ? this.selectedOrganizer.id : null,
          (this.selectedEventTypes) ? this.selectedEventTypes.map(et => et.id) : null,
          null,
          companyMetrics.id
        ))
        .subscribe(data => {
            const olapEventTypeStatistics: OlapEntityStatistics<number, OlapEventTypeMetrics> = data;

            fixOlapEntityStatistics(olapEventTypeStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            olapEventTypeStatistics.metricsList.forEach(m => m.companyId = companyMetrics.id);
            companyMetrics.eventTypeStatistics = getOlapEventTypeStatisticsWithSortName(olapEventTypeStatistics);
          }
        );
    }
  }

  companyEventTypeRowExpand(event) {
    const eventTypeMetrics: OlapEventTypeMetrics = event.data;

    if (!eventTypeMetrics.speakerStatistics) {
      this.statisticsService.getOlapSpeakerStatistics(
        new OlapSpeakerParameters(
          this.selectedCubeType,
          this.selectedMeasureType,
          eventTypeMetrics.companyId,
          eventTypeMetrics.id
        ))
        .subscribe(data => {
            const olapSpeakerStatistics: OlapEntityStatistics<number, OlapSpeakerMetrics> = data;

            fixOlapEntityStatistics(olapSpeakerStatistics, this.MEASURE_VALUE_FIELD_NAME_PREFIX);
            eventTypeMetrics.speakerStatistics = olapSpeakerStatistics;
          }
        );
    }
  }

  createLineOptions(maintainAspectRatio: boolean, aspectRatio: number, boxWidth: number): any {
    return {
      animation: false,
      maintainAspectRatio: maintainAspectRatio,
      aspectRatio: aspectRatio,
      locale: this.translateService.currentLang,
      plugins: {
        legend: {
          labels: {
            boxWidth: boxWidth
          }
        }
      }
    };
  }

  createPieOptions(maintainAspectRatio: boolean, aspectRatio: number, boxWidth: number): any {
    return {
      elements: {
        arc: {
          borderWidth: 1
        }
      },
      animation: false,
      maintainAspectRatio: maintainAspectRatio,
      aspectRatio: aspectRatio,
      locale: this.translateService.currentLang,
      plugins: {
        datalabels: {
          formatter: (value, context) => {
            const total = context.chart.data.datasets[0].data.reduce((a, b) => a + b, 0);

            return formatPercent(value / total, context.chart.options.locale, '1.1-1');
          },
          color: '#ffffff',
          display: 'auto'
        },
        tooltip: {
          callbacks: {
            label: function (tooltipItem) {
              const value = tooltipItem.dataset.data[tooltipItem.dataIndex];
              const total = tooltipItem.dataset.data.reduce((a, b) => a + b, 0);
              const percents = formatPercent(value / total, this.chart.options.locale, '1.1-1');

              return `${tooltipItem.formattedValue} (${percents})`;
            }
          }
        },
        legend: {
          labels: {
            boxWidth: boxWidth
          }
        }
      }
    };
  }

  createRadarOptions(maintainAspectRatio: boolean, aspectRatio: number, boxWidth: number): any {
    return {
      animation: false,
      maintainAspectRatio: maintainAspectRatio,
      aspectRatio: aspectRatio,
      locale: this.translateService.currentLang,
      plugins: {
        legend: {
          labels: {
            boxWidth: boxWidth
          }
        }
      }
    };
  }

  createCubeOptions(aspectRatio: number, height: number): ThreeDimensionsCubeOptions {
    return new ThreeDimensionsCubeOptions(aspectRatio, `${height}px`);
  }

  loadLineChartDetailsData(olapEntityStatistics: OlapEntityStatistics<number, OlapEntityMetrics>,
                           sortedMetricsList: OlapEntityMetrics[], quantity: number) {
    const metricsList = (quantity <= 0) ? sortedMetricsList : sortedMetricsList.slice(0, quantity);

    this.allLineData = {
      labels: olapEntityStatistics.dimensionValues,
      datasets: metricsList.map((value, index) => {
        const color = getColorByIndex(index);

        return {
          label: value.name,
          data: value.measureValues,
          fill: false,
          tension: 0.4,
          backgroundColor: color,
          borderColor: color
        }
      })
    };
  }

  loadLineChartTotalData(olapEntityStatistics: OlapEntityStatistics<number, OlapEntityMetrics>) {
    const color = getColorByIndex(0);

    this.translateService.get(this.TOTAL_LABEL_KEY)
      .subscribe(data => {
          this.totalLineData = {
            labels: olapEntityStatistics.dimensionValues,
            datasets: [
              {
                label: data,
                data: olapEntityStatistics.totals.measureValues,
                fill: false,
                tension: 0.4,
                backgroundColor: color,
                borderColor: color
              }
            ]
          };
        }
      );
  }

  loadLineChartWithCumulativeDetailsData(olapEntityStatistics: OlapEntityStatistics<number, OlapEntityMetrics>,
                                         sortedMetricsList: OlapEntityMetrics[], quantity: number) {
    const metricsList = (quantity <= 0) ? sortedMetricsList : sortedMetricsList.slice(0, quantity);

    this.allLineWithCumulativeData = {
      labels: olapEntityStatistics.dimensionValues,
      datasets: metricsList.map((value, index) => {
        const color = getColorByIndex(index);

        return {
          label: value.name,
          data: value.cumulativeMeasureValues,
          fill: false,
          tension: 0.4,
          backgroundColor: color,
          borderColor: color
        }
      })
    };
  }

  loadLineChartWithCumulativeTotalData(olapEntityStatistics: OlapEntityStatistics<number, OlapEntityMetrics>) {
    const color = getColorByIndex(0);

    this.translateService.get(this.TOTAL_LABEL_KEY)
      .subscribe(data => {
          this.totalLineWithCumulativeData = {
            labels: olapEntityStatistics.dimensionValues,
            datasets: [
              {
                label: data,
                data: olapEntityStatistics.totals.cumulativeMeasureValues,
                fill: false,
                tension: 0.4,
                backgroundColor: color,
                borderColor: color
              }
            ]
          };
        }
      );
  }

  loadPieChartData(olapEntityStatistics: OlapEntityStatistics<number, OlapEntityMetrics>,
                   sortedMetricsList: OlapEntityMetrics[], quantity: number) {
    const metricsList = (quantity <= 0) ? sortedMetricsList : sortedMetricsList.slice(0, quantity);
    const colors = metricsList.map((value, index) => getColorByIndex(index));

    this.pieData = {
      labels: metricsList.map((value) => value.name),
      datasets: [
        {
          data: metricsList.map((value) => value.total),
          backgroundColor: colors,
          hoverBackgroundColor: colors,
          borderColor: colors
        }
      ]
    };
  }

  getNonZeroValues(dimensionValueSize: number, metricsList: OlapEntityMetrics[]): boolean[] {
    const result = [];

    // Unmark all dimensions
    for (let i = 0; i < dimensionValueSize; i++) {
      result.push(false);
    }

    // Mark non-value dimensions
    for (const metrics of metricsList) {
      for (let i = 0; i < metrics.measureValues.length; i++) {
        if (metrics.measureValues[i] > 0) {
          result[i] = true;
        }
      }
    }

    // Count non-value dimensions
    const count = result
      .filter(value => value)
      .length;

    if (count < 3) {
      // Mark all dimensions
      for (let i = 0; i < dimensionValueSize; i++) {
        result[i] = true;
      }
    }

    return result;
  }

  loadRadarChartDetailsData(olapEntityStatistics: OlapEntityStatistics<string, OlapEntityMetrics>,
                            sortedMetricsList: OlapEntityMetrics[], quantity: number) {
    const metricsList = (quantity <= 0) ? sortedMetricsList : sortedMetricsList.slice(0, quantity);
    const topicMetricsList = metricsList.map(value => this.topicMetricsMap.get(value.id));
    const nonZeroValues = this.getNonZeroValues(olapEntityStatistics.dimensionValues.length, topicMetricsList);
    const filteredDimensionValues = olapEntityStatistics.dimensionValues
      .filter((element, index) => nonZeroValues[index]);

    this.allRadarData = {
      labels: filteredDimensionValues,
      datasets: topicMetricsList.map((value, index) => {
        const color = getColorByIndex(index);
        const backgroundColor = hexToRgbA(color, '0.2');
        const filteredMeasureValues = value.measureValues
          .filter((element, index) => nonZeroValues[index]);

        return {
          label: value.name,
          data: filteredMeasureValues,
          backgroundColor: backgroundColor,
          borderColor: color
        }
      })
    };
  }

  loadRadarChartTotalData(olapEntityStatistics: OlapEntityStatistics<string, OlapEntityMetrics>) {
    const color = getColorByIndex(0);
    const backgroundColor = hexToRgbA(color, '0.2');
    const topicMetricsList = [olapEntityStatistics.totals];
    const nonZeroValues = this.getNonZeroValues(olapEntityStatistics.dimensionValues.length, topicMetricsList);
    const filteredDimensionValues = olapEntityStatistics.dimensionValues
      .filter((element, index) => nonZeroValues[index]);
    const filteredMeasureValues = olapEntityStatistics.totals.measureValues
      .filter((element, index) => nonZeroValues[index]);

    this.translateService.get(this.TOTAL_LABEL_KEY)
      .subscribe(data => {
          this.totalRadarData = {
            labels: filteredDimensionValues,
            datasets: [
              {
                label: data,
                data: filteredMeasureValues,
                backgroundColor: backgroundColor,
                borderColor: color
              }
            ]
          };
        }
      );
  }

  getUsedDimensionIds(metricsList: Olap3dCubeMetrics[]): Set<number> {
    const result = new Set<number>();

    for (const metrics of metricsList) {
      for (const measureValue of metrics.measureValueList) {
        result.add(measureValue.dimensionId);
      }
    }

    return result;
  }

  load3dCubeChartData(olap3dCubeStatistics: Olap3dCubeStatistics, sortedMetricsList: OlapEntityMetrics[], quantity: number) {
    const metricsList = (quantity <= 0) ? sortedMetricsList : sortedMetricsList.slice(0, quantity);
    const cubeMetricsList = metricsList.map(value => this.cubeMetricsMap.get(value.id));
    const labelsX = olap3dCubeStatistics.dimensionValues1.map(value => value.toString());
    const usedDimensionIds = this.getUsedDimensionIds(cubeMetricsList);
    const filteredDimensionValues2 = olap3dCubeStatistics.dimensionValues2
      .filter(element => usedDimensionIds.has(element.id));
    const labelsY = filteredDimensionValues2.map(value => value.name);

    const datasets = cubeMetricsList
      .map((value, index) => {
        const data = [];
        const measureValuesMap = new Map<number, number[]>();

        value.measureValueList.forEach(m => measureValuesMap.set(m.dimensionId, m.measureValues));
        filteredDimensionValues2.forEach(v => data.push(measureValuesMap.get(v.id)));

        return new ThreeDimensionsCubeDataset(
          value.name,
          getColorByIndex(index),
          data
        );
      });

    this.cubeData = new ThreeDimensionsCubeData(labelsX, labelsY, datasets);
  }

  onResize = (): void => {
    this.fillChartOptions();
  }

  getAspectRatio(clientWidth: number): number {
    if (clientWidth < this.SMALL_WIDTH) {
      return this.EXTRA_SMALL_ASPECT_RATIO;
    } else if (clientWidth < this.MEDIUM_WIDTH) {
      return this.SMALL_ASPECT_RATIO;
    } else if (clientWidth < this.LARGE_WIDTH) {
      return this.MEDIUM_ASPECT_RATIO;
    } else if (clientWidth < this.EXTRA_LARGE_WIDTH) {
      return this.LARGE_ASPECT_RATIO;
    } else {
      return this.EXTRA_LARGE_ASPECT_RATIO;
    }
  }

  getBoxWidth(clientWidth: number): number {
    if (clientWidth < this.SMALL_WIDTH) {
      return this.EXTRA_SMALL_BOX_WIDTH;
    } else if (clientWidth < this.MEDIUM_WIDTH) {
      return this.SMALL_BOX_WIDTH;
    } else {
      return this.DEFAULT_BOX_WIDTH;
    }
  }

  isChartsVisible() {
    return this.isEventTypesListVisible() || this.isSpeakersListVisible() || this.isCompaniesListVisible();
  }

  isChartTypeSwitcherVisible() {
    return ((this.selectedChartKind !== OlapChartKind.Pie) && (this.selectedChartKind !== OlapChartKind.Cube));
  }

  isDetailsLineChartVisible() {
    return ((this.selectedChartType === OlapChartType.Details) && (this.selectedChartKind === OlapChartKind.Line));
  }

  isTotalLineChartVisible() {
    return ((this.selectedChartType === OlapChartType.Total) && (this.selectedChartKind === OlapChartKind.Line));
  }

  isDetailsLineWithCumulativeChartVisible() {
    return ((this.selectedChartType === OlapChartType.Details) && (this.selectedChartKind === OlapChartKind.LineWithCumulativeTotal));
  }

  isTotalLineWithCumulativeChartVisible() {
    return ((this.selectedChartType === OlapChartType.Total) && (this.selectedChartKind === OlapChartKind.LineWithCumulativeTotal));
  }

  isPieChartVisible() {
    return (this.selectedChartKind === OlapChartKind.Pie);
  }

  isDetailsRadarChartVisible() {
    return ((this.selectedChartType === OlapChartType.Details) && (this.selectedChartKind === OlapChartKind.Radar));
  }

  isTotalRadarChartVisible() {
    return ((this.selectedChartType === OlapChartType.Total) && (this.selectedChartKind === OlapChartKind.Radar));
  }

  isCubeVisible() {
    return (this.selectedChartKind === OlapChartKind.Cube);
  }

  sortEventTypeStatistics(value) {
    this.loadLineChartDetailsData(this.olapStatistics.eventTypeStatistics, value, this.EVENT_TYPE_CHART_DATASET_QUANTITY);
    this.loadLineChartWithCumulativeDetailsData(this.olapStatistics.eventTypeStatistics, value, this.EVENT_TYPE_CHART_DATASET_QUANTITY);
    this.loadPieChartData(this.olapStatistics.eventTypeStatistics, value, this.EVENT_TYPE_CHART_DATASET_QUANTITY);
    this.loadRadarChartDetailsData(this.olapStatistics.topicStatistics, value, this.EVENT_TYPE_CHART_DATASET_QUANTITY);
    this.load3dCubeChartData(this.olapStatistics.threeDimensionsStatistics, value, this.EVENT_TYPE_CHART_DATASET_QUANTITY);
  }

  sortSpeakerStatistics(value) {
    this.loadLineChartDetailsData(this.olapStatistics.speakerStatistics, value, this.SPEAKER_CHART_DATASET_QUANTITY);
    this.loadLineChartWithCumulativeDetailsData(this.olapStatistics.speakerStatistics, value, this.SPEAKER_CHART_DATASET_QUANTITY);
    this.loadPieChartData(this.olapStatistics.speakerStatistics, value, this.SPEAKER_CHART_DATASET_QUANTITY);
    this.loadRadarChartDetailsData(this.olapStatistics.topicStatistics, value, this.SPEAKER_CHART_DATASET_QUANTITY);
    this.load3dCubeChartData(this.olapStatistics.threeDimensionsStatistics, value, this.SPEAKER_CHART_DATASET_QUANTITY);
  }

  sortCompanyStatistics(value) {
    this.loadLineChartDetailsData(this.olapStatistics.companyStatistics, value, this.COMPANY_CHART_DATASET_QUANTITY);
    this.loadLineChartWithCumulativeDetailsData(this.olapStatistics.companyStatistics, value, this.COMPANY_CHART_DATASET_QUANTITY);
    this.loadPieChartData(this.olapStatistics.companyStatistics, value, this.COMPANY_CHART_DATASET_QUANTITY);
    this.loadRadarChartDetailsData(this.olapStatistics.topicStatistics, value, this.COMPANY_CHART_DATASET_QUANTITY);
    this.load3dCubeChartData(this.olapStatistics.threeDimensionsStatistics, value, this.COMPANY_CHART_DATASET_QUANTITY);
  }

  zoomInChart() {
    let type: string;
    let plugins: any[];
    let data: any;
    let options: any;

    if (this.selectedChartKind === OlapChartKind.Line) {
      type = 'line';
      plugins = [];
      options = this.createLineOptions(false, 2, this.DEFAULT_BOX_WIDTH);

      if (this.selectedChartType === OlapChartType.Details) {
        data = this.allLineData;
      } else {
        data = this.totalLineData;
      }
    } else if (this.selectedChartKind === OlapChartKind.LineWithCumulativeTotal) {
      type = 'line';
      plugins = [];
      options = this.createLineOptions(false, 2, this.DEFAULT_BOX_WIDTH);

      if (this.selectedChartType === OlapChartType.Details) {
        data = this.allLineWithCumulativeData;
      } else {
        data = this.totalLineWithCumulativeData;
      }
    } else if (this.selectedChartKind === OlapChartKind.Pie) {
      type = 'pie';
      plugins = this.chartPlugins;
      data = this.pieData;
      options = this.createPieOptions(false, 1, this.DEFAULT_BOX_WIDTH);
    } else if (this.selectedChartKind === OlapChartKind.Radar) {
      type = 'radar';
      plugins = [];
      options = this.createRadarOptions(false, 1, this.DEFAULT_BOX_WIDTH);

      if (this.selectedChartType === OlapChartType.Details) {
        data = this.allRadarData;
      } else {
        data = this.totalRadarData;
      }
    } else {
      type = 'cube';
      plugins = [];
      data = this.cubeData;
      options = new ThreeDimensionsCubeOptions(this.DEFAULT_CUBE_CANVAS_ASPECT_RATIO, '100%');
    }

    const cubeTypeMessageKey = this.getCubeTypeMessageKeyByCube(this.selectedCubeType);
    const measureTypeMessageKey = this.getMeasureTypeMessageKeyByCube(this.selectedMeasureType);
    const keys = [cubeTypeMessageKey, measureTypeMessageKey];
    const dynamicDialogChartData = new DynamicDialogChartData(type, plugins, data, options);
    const componentType = (this.selectedChartKind === OlapChartKind.Cube) ? ThreeDimensionsZoomInComponent : ChartZoomInComponent;

    this.translateService.get(keys)
      .subscribe(labels => {
        const cubeTypeMessage = labels[cubeTypeMessageKey];
        const measureTypeMessage = labels[measureTypeMessageKey];

        this.zoomInDialogClosed = false;
        this.zoomInDialogRef = this.dialogService.open(componentType, {
          data: dynamicDialogChartData,
          width: '100%',
          height: '100%',
          styleClass: 'chart-zoom-in-dialog',
          header: `${cubeTypeMessage} – ${measureTypeMessage}`,
          closable: true,
          closeOnEscape: true
        });

        this.zoomInDialogRef.onClose.subscribe(() => {
          this.zoomInDialogClosed = true;
        });
      });
  }
}
