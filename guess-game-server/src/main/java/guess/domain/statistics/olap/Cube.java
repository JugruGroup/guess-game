package guess.domain.statistics.olap;

import guess.domain.QuadFunction;
import guess.domain.statistics.olap.dimension.Dimension;
import guess.domain.statistics.olap.dimension.DimensionFactory;
import guess.domain.statistics.olap.measure.Measure;
import guess.domain.statistics.olap.measure.MeasureFactory;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OLAP cube.
 */
public class Cube {
    private record MeasureMaps<T, S>(Map<T, List<Measure<?>>> dimensionTotalMeasures1,
                                     Map<S, List<Measure<?>>> dimensionTotalMeasures2,
                                     Map<S, List<Measure<?>>> measuresByDimensionValue2) {
    }

    private final Set<DimensionType> dimensionTypes;
    private final Set<MeasureType> measureTypes;
    private final Map<DimensionType, Set<Dimension<?>>> dimensionMap = new EnumMap<>(DimensionType.class);
    private final Map<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> measureMap = new HashMap<>();

    public Cube(Set<DimensionType> dimensionTypes, Set<MeasureType> measureTypes) {
        this.dimensionTypes = dimensionTypes;
        this.measureTypes = measureTypes;
    }

    /**
     * Gets dimension types.
     *
     * @return dimension types
     */
    public Set<DimensionType> getDimensionTypes() {
        return dimensionTypes;
    }

    /**
     * Gets measure types.
     *
     * @return measure types
     */
    public Set<MeasureType> getMeasureTypes() {
        return measureTypes;
    }

    /**
     * Gets dimension values.
     *
     * @param dimensionType dimension type
     * @return dimension values
     */
    public List<Object> getDimensionValues(DimensionType dimensionType) {
        checkDimensionType(dimensionType);

        return dimensionMap.getOrDefault(dimensionType, Collections.emptySet()).stream()
                .map(v -> (Object) v.getValue())
                .toList();
    }

    /**
     * Checks the existence of dimension type.
     *
     * @param dimensionType dimension type
     */
    private void checkDimensionType(DimensionType dimensionType) {
        if (!dimensionTypes.contains(dimensionType)) {
            throw new IllegalArgumentException(String.format("Invalid dimension type %s for %s valid values", dimensionType, dimensionTypes));
        }
    }

    /**
     * Checks the existence of measure type.
     *
     * @param measureType measure type
     */
    private void checkMeasureType(MeasureType measureType) {
        if (!measureTypes.contains(measureType)) {
            throw new IllegalArgumentException(String.format("Invalid measure type %s for %s valid values", measureType, measureTypes));
        }
    }

    /**
     * Gets dimension type by dimension.
     *
     * @param dimension dimension
     * @return dimension type
     */
    private DimensionType getDimensionTypeByDimension(Dimension<?> dimension) {
        for (DimensionType dimensionType : dimensionTypes) {
            Set<Dimension<?>> dimensionSet = dimensionMap.get(dimensionType);

            if ((dimensionSet != null) && dimensionSet.contains(dimension)) {
                return dimensionType;
            }
        }

        return null;
    }

    /**
     * Checks dimension set.
     *
     * @param dimensions dimension set
     */
    private void checkDimensionSet(Set<Dimension<?>> dimensions) {
        // Check dimension set size
        if (dimensionTypes.size() != dimensions.size()) {
            throw new IllegalArgumentException(String.format("Invalid size of dimension set (%d), valid size is %d", dimensions.size(), dimensionTypes.size()));
        }

        Set<DimensionType> foundDimensionTypes = new HashSet<>();

        // Check dimensions existence
        dimensions.forEach(d -> {
            DimensionType foundDimensionType = getDimensionTypeByDimension(d);

            if (foundDimensionType == null) {
                throw new IllegalArgumentException("Dimension not found in cube");
            }

            foundDimensionTypes.add(foundDimensionType);
        });

        // Check the number of dimension types for found dimensions
        if (foundDimensionTypes.size() != dimensions.size()) {
            throw new IllegalArgumentException(String.format("Invalid size of found dimension set (%d), valid size is %d", dimensions.size(), dimensionTypes.size()));
        }
    }

    /**
     * Adds dimensions.
     *
     * @param dimensionType dimension type
     * @param values        dimension values
     */
    public void addDimensions(DimensionType dimensionType, Set<?> values) {
        checkDimensionType(dimensionType);

        Set<Dimension<?>> dimensions = values.stream()
                .map(v -> (Dimension<?>) DimensionFactory.create(dimensionType, v))
                .collect(Collectors.toSet());

        this.dimensionMap.put(dimensionType, dimensions);
    }

    /**
     * Adds measure entity.
     *
     * @param dimensions  dimension set
     * @param measureType measure type
     * @param entity      entity
     */
    public void addMeasureEntity(Set<Dimension<?>> dimensions, MeasureType measureType, Object entity) {
        checkDimensionSet(dimensions);
        checkMeasureType(measureType);

        Map<MeasureType, Measure<?>> dimensionMeasures = measureMap.computeIfAbsent(dimensions, k -> new HashMap<>());
        Measure<?> measure = dimensionMeasures.get(measureType);

        if (measure == null) {
            dimensionMeasures.put(measureType, MeasureFactory.create(measureType, Set.of(entity)));
        } else {
            measure.addEntity(entity);
        }
    }

    /**
     * Gets measure value by measures.
     *
     * @param measures    measure list
     * @param measureType measure type
     * @return measure value
     */
    public long getMeasureValue(List<? extends Measure<?>> measures, MeasureType measureType) {
        if ((measures == null) || measures.isEmpty()) {
            return 0L;
        } else if (measures.size() == 1) {
            return measures.get(0).calculateValue();
        } else {
            Set<Object> measureValues = measures.stream()
                    .flatMap(m -> m.getEntities().stream())
                    .collect(Collectors.toSet());
            Measure<?> measure = MeasureFactory.create(measureType, measureValues);

            return measure.calculateValue();
        }
    }

    /**
     * Gets measure value by dimensions.
     * <p>
     * Slow, only used in unit tests.
     *
     * @param dimensions  dimension set
     * @param measureType measure type
     * @return measure value
     */
    public long getMeasureValue(Set<Dimension<?>> dimensions, MeasureType measureType) {
        List<? extends Measure<?>> measures = measureMap.entrySet().stream()
                .filter(e -> e.getKey().containsAll(dimensions))
                .map(Map.Entry::getValue)
                .filter(e -> e.containsKey(measureType))
                .map(e -> e.get(measureType))
                .toList();

        return getMeasureValue(measures, measureType);
    }

    /**
     * Gets measure value entities.
     * <p>
     * Fast, used in the application.
     *
     * @param dimensionTypeValues1      values of first dimension type
     * @param dimensionTypeValues2      values of second dimension type
     * @param dimensionTypeValues2      values of third dimension type
     * @param filterDimensionTypeValues values of filter dimension type
     * @param measureType               measure type
     * @param entityQuadFunction        result element function
     * @param totalsTriFunction         totals function
     * @param resultTriFunction         result function
     * @param <T>                       first dimension type
     * @param <S>                       second dimension type
     * @param <U>                       third dimension type
     * @param <V>                       filter dimension type
     * @param <W>                       result element type
     * @param <X>                       totals type
     * @param <Y>                       result type
     * @return measure value entities
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, S, U, V, W, X, Y> Y getMeasureValueEntities(DimensionTypeValues<T> dimensionTypeValues1,
                                                           DimensionTypeValues<S> dimensionTypeValues2,
                                                           DimensionTypeValues<U> dimensionTypeValues3,
                                                           DimensionTypeValues<V> filterDimensionTypeValues,
                                                           MeasureType measureType,
                                                           QuadFunction<T, List<Long>, List<Long>, Long, W> entityQuadFunction,
                                                           TriFunction<List<Long>, List<Long>, Long, X> totalsTriFunction,
                                                           QuadFunction<List<S>, List<U>, List<W>, X, Y> resultTriFunction) {
        Set<Dimension> dimensions1 = dimensionTypeValues1.values().stream()
                .map(v -> DimensionFactory.create(dimensionTypeValues1.type(), v))
                .collect(Collectors.toSet());
        Set<Dimension> dimensions2 = dimensionTypeValues2.values().stream()
                .map(v -> DimensionFactory.create(dimensionTypeValues2.type(), v))
                .collect(Collectors.toSet());
        Set<Dimension> filterDimensions = filterDimensionTypeValues.values().stream()
                .map(v -> DimensionFactory.create(filterDimensionTypeValues.type(), v))
                .collect(Collectors.toSet());
        Map<T, Map<S, List<Measure<?>>>> measuresByDimensionValue1 = new HashMap<>();
        Map<T, List<Measure<?>>> dimensionTotalMeasures1 = new HashMap<>();
        Map<S, List<Measure<?>>> dimensionTotalMeasures2 = new HashMap<>();

        // Create intermediate measure map
        for (Map.Entry<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> entry : measureMap.entrySet()) {
            Set<Dimension<?>> entryDimensions = entry.getKey();

            // Search first dimension value
            for (Dimension<?> entryDimension1 : entryDimensions) {
                if (dimensions1.contains(entryDimension1)) {
                    T dimensionValue1 = (T) entryDimension1.getValue();
                    Map<S, List<Measure<?>>> measuresByDimensionValue2 = measuresByDimensionValue1.computeIfAbsent(dimensionValue1, k -> new HashMap<>());

                    // Search second dimension value
                    for (Dimension<?> entryDimension2 : entryDimensions) {
                        if (dimensions2.contains(entryDimension2)) {

                            // Filter by values of third dimension
                            MeasureMaps<T, S> measureMaps = new MeasureMaps<>(dimensionTotalMeasures1, dimensionTotalMeasures2, measuresByDimensionValue2);
                            filterByDimensionValues3(measureType, filterDimensions, measureMaps, entry, dimensionValue1, entryDimension2);

                            break;
                        }
                    }

                    break;
                }
            }
        }

        // Fill resulting list
        List<W> measureValueEntities = new ArrayList<>();

        fillResultingList(dimensionTypeValues1, dimensionTypeValues2, measureType, entityQuadFunction,
                measuresByDimensionValue1, dimensionTotalMeasures1, measureValueEntities);

        // Fill totals
        List<Long> totals = new ArrayList<>();
        List<Long> cumulativeTotals = new ArrayList<>();
        List<Measure<?>> allTotalMeasures = new ArrayList<>();

        fillTotals(dimensionTypeValues2, dimensionTotalMeasures2, measureType, totals, cumulativeTotals, allTotalMeasures);

        // Fill all total
        long allTotal = getMeasureValue(allTotalMeasures, measureType);

        return resultTriFunction.apply(
                dimensionTypeValues2.values(),
                dimensionTypeValues3.values(),
                measureValueEntities,
                totalsTriFunction.apply(totals, cumulativeTotals, allTotal));
    }

    /**
     * Filters by third dimension values.
     *
     * @param measureType      measure type
     * @param filterDimensions filter dimensions
     * @param measureMaps      measure maps
     * @param entry            entry of measure map
     * @param dimensionValue1  first dimension value
     * @param entryDimension2  second entry dimension
     * @param <T>              first dimension type
     * @param <S>              second dimension type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T, S> void filterByDimensionValues3(MeasureType measureType,
                                                 Set<Dimension> filterDimensions,
                                                 MeasureMaps<T, S> measureMaps,
                                                 Map.Entry<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> entry,
                                                 T dimensionValue1,
                                                 Dimension<?> entryDimension2) {
        Set<Dimension<?>> entryDimensions = entry.getKey();

        for (Dimension<?> entryDimension3 : entryDimensions) {
            if (filterDimensions.contains(entryDimension3)) {

                // filterByThirdDimensionValues
                Measure<?> measure = entry.getValue().get(measureType);

                if (measure != null) {
                    S dimensionValue2 = (S) entryDimension2.getValue();

                    // Measures of first and second dimension
                    measureMaps.measuresByDimensionValue2
                            .computeIfAbsent(dimensionValue2, k -> new ArrayList<>())
                            .add(measure);

                    // Measures for total of first dimension
                    measureMaps.dimensionTotalMeasures1
                            .computeIfAbsent(dimensionValue1, k -> new ArrayList<>())
                            .add(measure);

                    // Measures for total of second dimension
                    measureMaps.dimensionTotalMeasures2
                            .computeIfAbsent(dimensionValue2, k -> new ArrayList<>())
                            .add(measure);
                }

                break;
            }
        }
    }

    /**
     * Fill resulting list.
     *
     * @param dimensionTypeValues1      values of first dimension type
     * @param dimensionTypeValues2      values of second dimension type
     * @param measureType               measure type
     * @param entityQuadFunction        result element function
     * @param measuresByDimensionValue1 measures by first dimension value
     * @param dimensionTotalMeasures1   total measures of first dimension
     * @param measureValueEntities      measure value entities
     * @param <T>                       first dimension type
     * @param <S>                       second dimension type
     * @param <U>                       result element type
     */
    private <T, S, U> void fillResultingList(DimensionTypeValues<T> dimensionTypeValues1,
                                             DimensionTypeValues<S> dimensionTypeValues2,
                                             MeasureType measureType,
                                             QuadFunction<T, List<Long>, List<Long>, Long, U> entityQuadFunction,
                                             Map<T, Map<S, List<Measure<?>>>> measuresByDimensionValue1,
                                             Map<T, List<Measure<?>>> dimensionTotalMeasures1,
                                             List<U> measureValueEntities) {
        for (T dimensionValue1 : dimensionTypeValues1.values()) {
            Map<S, List<Measure<?>>> measuresByDimensionValue2 = measuresByDimensionValue1.get(dimensionValue1);
            List<Long> measureValues;
            List<Long> cumulativeMeasureValues;

            if (measuresByDimensionValue2 == null) {
                measureValues = Collections.nCopies(dimensionTypeValues2.values().size(), 0L);
                cumulativeMeasureValues = Collections.nCopies(dimensionTypeValues2.values().size(), 0L);
            } else {
                measureValues = new ArrayList<>();
                cumulativeMeasureValues = new ArrayList<>();
                List<Measure<?>> cumulativeMeasures = new ArrayList<>();

                for (S dimensionValue2 : dimensionTypeValues2.values()) {
                    List<Measure<?>> measures = measuresByDimensionValue2.get(dimensionValue2);

                    if ((measures != null) && !measures.isEmpty()) {
                        cumulativeMeasures.addAll(measures);
                    }

                    measureValues.add(getMeasureValue(measures, measureType));
                    cumulativeMeasureValues.add(getMeasureValue(cumulativeMeasures, measureType));
                }
            }

            List<Measure<?>> measures = dimensionTotalMeasures1.get(dimensionValue1);
            long total = getMeasureValue(measures, measureType);

            measureValueEntities.add(entityQuadFunction.apply(dimensionValue1, measureValues, cumulativeMeasureValues, total));
        }
    }

    /**
     * Fill totals.
     *
     * @param dimensionTypeValues2    values of second dimension type
     * @param dimensionTotalMeasures2 total measures of second dimension
     * @param measureType             measure type
     * @param totals                  totals
     * @param cumulativeTotals        cumulative totals
     * @param allTotalMeasures        all total measures
     * @param <S>                     second dimension type
     */
    private <S> void fillTotals(DimensionTypeValues<S> dimensionTypeValues2,
                                Map<S, List<Measure<?>>> dimensionTotalMeasures2,
                                MeasureType measureType, List<Long> totals, List<Long> cumulativeTotals,
                                List<Measure<?>> allTotalMeasures) {
        List<Measure<?>> cumulativeMeasures = new ArrayList<>();

        for (S dimensionValue2 : dimensionTypeValues2.values()) {
            List<Measure<?>> measures = dimensionTotalMeasures2.get(dimensionValue2);

            if ((measures != null) && !measures.isEmpty()) {
                cumulativeMeasures.addAll(measures);
            }

            long total = getMeasureValue(measures, measureType);
            long cumulativeTotal = getMeasureValue(cumulativeMeasures, measureType);

            totals.add(total);
            cumulativeTotals.add(cumulativeTotal);

            if (measures != null) {
                allTotalMeasures.addAll(measures);
            }
        }
    }
}
