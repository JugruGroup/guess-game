package guess.domain.statistics.olap;

import guess.domain.function.QuadFunction;
import guess.domain.statistics.olap.dimension.Dimension;
import guess.domain.statistics.olap.dimension.DimensionFactory;
import guess.domain.statistics.olap.measure.Measure;
import guess.domain.statistics.olap.measure.MeasureFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * OLAP cube.
 */
public class Cube {
    private record MeasureMaps<T, S, U>(Map<T, List<Measure<?>>> dimensionTotalMeasures1,
                                        Map<S, List<Measure<?>>> dimensionTotalMeasures2,
                                        Map<T, Map<S, List<Measure<?>>>> measuresByDimensionValue1,
                                        Map<T, Map<U, Map<S, List<Measure<?>>>>> subMeasuresByDimensionValue1) {
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
     * Slow, used only in unit tests.
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
     * @param dimensionTypeValues3      values of third dimension type
     * @param filterDimensionTypeValues values of filter dimension type
     * @param measureType               measure type
     * @param <T>                       first dimension type
     * @param <S>                       second dimension type
     * @param <U>                       third dimension type
     * @param <V>                       filter dimension type
     * @param <W>                       result sub metrics element type
     * @param <X>                       result metrics element type
     * @param <Y>                       totals type
     * @param <Z>                       result type
     * @return measure value entities
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, S, U, V, W, X, Y, Z> Z getMeasureValueEntities(DimensionTypeValues<T> dimensionTypeValues1,
                                                              DimensionTypeValues<S> dimensionTypeValues2,
                                                              DimensionTypeValues<U> dimensionTypeValues3,
                                                              DimensionTypeValues<V> filterDimensionTypeValues,
                                                              MeasureType measureType,
                                                              ResultFunctions<T, S, U, W, X, Y, Z> resultFunctions) {
        Set<Dimension> dimensions1 = dimensionTypeValues1.values().stream()
                .map(v -> DimensionFactory.create(dimensionTypeValues1.type(), v))
                .collect(Collectors.toSet());
        Set<Dimension> dimensions2 = dimensionTypeValues2.values().stream()
                .map(v -> DimensionFactory.create(dimensionTypeValues2.type(), v))
                .collect(Collectors.toSet());
        Set<Dimension> dimensions3 = dimensionTypeValues3.values().stream()
                .map(v -> DimensionFactory.create(dimensionTypeValues3.type(), v))
                .collect(Collectors.toSet());
        Set<Dimension> filterDimensions = filterDimensionTypeValues.values().stream()
                .map(v -> DimensionFactory.create(filterDimensionTypeValues.type(), v))
                .collect(Collectors.toSet());
        Map<T, Map<S, List<Measure<?>>>> measuresByDimensionValue1 = new HashMap<>();
        Map<T, Map<U, Map<S, List<Measure<?>>>>> subMeasuresByDimensionValue1 = new HashMap<>();
        Map<T, List<Measure<?>>> dimensionTotalMeasures1 = new HashMap<>();
        Map<S, List<Measure<?>>> dimensionTotalMeasures2 = new HashMap<>();

        for (Map.Entry<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> entry : measureMap.entrySet()) {
            Set<Dimension<?>> entryDimensions = entry.getKey();

            // Search first dimension values
            for (Dimension<?> entryDimension1 : entryDimensions) {
                if (dimensions1.contains(entryDimension1)) {
                    // Search filter dimension values
                    for (Dimension<?> filterEntryDimension : entryDimensions) {
                        if (filterDimensions.contains(filterEntryDimension)) {
                            T dimensionValue1 = (T) entryDimension1.getValue();
                            MeasureMaps<T, S, U> measureMaps = new MeasureMaps<>(
                                    dimensionTotalMeasures1, dimensionTotalMeasures2,
                                    measuresByDimensionValue1, subMeasuresByDimensionValue1);

                            fillDetails(measureType, dimensions2, dimensions3, measureMaps, entry, dimensionValue1);

                            break;
                        }
                    }

                    break;
                }
            }
        }

        // Fill resulting list
        List<X> measureValueEntities = getMeasureValueEntities(dimensionTypeValues1.values(), dimensionTypeValues2.values(),
                measureType, measuresByDimensionValue1, resultFunctions.resultMetricsQuadFunction(), dimensionTotalMeasures1);
        List<W> subMeasureValueEntities = getSubMeasureValueEntities(dimensionTypeValues1.values(), dimensionTypeValues2.values(),
                dimensionTypeValues3.values(), measureType, subMeasuresByDimensionValue1, resultFunctions.resultSubMetricsBiFunction());

        // Fill totals
        List<Long> totals = new ArrayList<>();
        List<Long> cumulativeTotals = new ArrayList<>();
        List<Measure<?>> allTotalMeasures = new ArrayList<>();

        fillTotals(dimensionTypeValues2.values(), dimensionTotalMeasures2, measureType, totals, cumulativeTotals, allTotalMeasures);

        // Fill all total
        long allTotal = getMeasureValue(allTotalMeasures, measureType);

        return resultFunctions.resultQuintFunction().apply(
                dimensionTypeValues2.values(),
                dimensionTypeValues3.values(),
                subMeasureValueEntities,
                measureValueEntities,
                resultFunctions.totalsTriFunction().apply(totals, cumulativeTotals, allTotal));
    }

    /**
     * Fill details.
     *
     * @param measureType     measure type
     * @param dimensions2     second dimensions
     * @param dimensions3     third dimensions
     * @param measureMaps     measure maps
     * @param entry           entry of measure map
     * @param dimensionValue1 first dimension value
     * @param <T>             first dimension type
     * @param <S>             second dimension type
     * @param <U>             third dimension type
     */
    @SuppressWarnings("rawtypes")
    private <T, S, U> void fillDetails(MeasureType measureType,
                                       Set<Dimension> dimensions2,
                                       Set<Dimension> dimensions3,
                                       MeasureMaps<T, S, U> measureMaps,
                                       Map.Entry<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> entry,
                                       T dimensionValue1) {
        // Search second dimension values
        fillMeasureValues(measureType, dimensions2, measureMaps, entry, dimensionValue1);

        // Search second and third dimension values
        if (!dimensions3.isEmpty()) {
            fillSubMeasureValues(measureType, dimensions2, dimensions3, measureMaps, entry, dimensionValue1);
        }
    }

    /**
     * Fill measure values.
     *
     * @param measureType     measure type
     * @param dimensions2     second dimensions
     * @param measureMaps     measure maps
     * @param entry           entry of measure map
     * @param dimensionValue1 first dimension value
     * @param <T>             first dimension type
     * @param <S>             second dimension type
     * @param <U>             third dimension type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T, S, U> void fillMeasureValues(MeasureType measureType,
                                             Set<Dimension> dimensions2,
                                             MeasureMaps<T, S, U> measureMaps,
                                             Map.Entry<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> entry,
                                             T dimensionValue1) {
        Set<Dimension<?>> entryDimensions = entry.getKey();
        Map<S, List<Measure<?>>> measuresByDimensionValue2 =
                measureMaps.measuresByDimensionValue1.computeIfAbsent(dimensionValue1, k -> new HashMap<>());

        for (Dimension<?> entryDimension2 : entryDimensions) {
            if (dimensions2.contains(entryDimension2)) {
                Measure<?> measure = entry.getValue().get(measureType);

                if (measure != null) {
                    S dimensionValue2 = (S) entryDimension2.getValue();

                    // Measures of first and second dimensions
                    measuresByDimensionValue2
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
     * Fill sub measure values.
     *
     * @param measureType     measure type
     * @param dimensions2     second dimensions
     * @param dimensions3     third dimensions
     * @param measureMaps     measure maps
     * @param entry           entry of measure map
     * @param dimensionValue1 first dimension value
     * @param <T>             first dimension type
     * @param <S>             second dimension type
     * @param <U>             third dimension type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T, S, U> void fillSubMeasureValues(MeasureType measureType,
                                                Set<Dimension> dimensions2,
                                                Set<Dimension> dimensions3,
                                                MeasureMaps<T, S, U> measureMaps,
                                                Map.Entry<Set<Dimension<?>>, Map<MeasureType, Measure<?>>> entry,
                                                T dimensionValue1) {
        Set<Dimension<?>> entryDimensions = entry.getKey();
        Map<U, Map<S, List<Measure<?>>>> subMeasuresByDimensionValue2 =
                measureMaps.subMeasuresByDimensionValue1.computeIfAbsent(dimensionValue1, k -> new HashMap<>());

        for (Dimension<?> entryDimension3 : entryDimensions) {
            if (dimensions3.contains(entryDimension3)) {
                U dimensionValue3 = (U) entryDimension3.getValue();
                Map<S, List<Measure<?>>> subMeasuresByDimensionValue3 =
                        subMeasuresByDimensionValue2.computeIfAbsent(dimensionValue3, k -> new HashMap<>());

                for (Dimension<?> entryDimension2 : entryDimensions) {
                    if (dimensions2.contains(entryDimension2)) {
                        Measure<?> measure = entry.getValue().get(measureType);

                        if (measure != null) {
                            S dimensionValue2 = (S) entryDimension2.getValue();

                            // Sub measures of first, second and third dimensions
                            subMeasuresByDimensionValue3
                                    .computeIfAbsent(dimensionValue2, k -> new ArrayList<>())
                                    .add(measure);
                        }

                        break;
                    }
                }

                break;
            }
        }
    }

    /**
     * Gets measure value entities.
     *
     * @param dimensionValues1          values of first dimension type
     * @param dimensionValues2          values of second dimension type
     * @param measureType               measure type
     * @param measuresByDimensionValue1 measures by first dimension value
     * @param resultMetricsQuadFunction result metrics element function
     * @param dimensionTotalMeasures1   total measures of first dimension
     * @param <T>                       first dimension type
     * @param <S>                       second dimension type
     * @param <U>                       result metrics element type
     * @return measure value entities
     */
    private <T, S, U> List<U> getMeasureValueEntities(List<T> dimensionValues1,
                                                      List<S> dimensionValues2,
                                                      MeasureType measureType,
                                                      Map<T, Map<S, List<Measure<?>>>> measuresByDimensionValue1,
                                                      QuadFunction<T, List<Long>, List<Long>, Long, U> resultMetricsQuadFunction,
                                                      Map<T, List<Measure<?>>> dimensionTotalMeasures1) {
        List<U> measureValueEntities = new ArrayList<>();

        // Iterate values of first dimension type
        for (T dimensionValue1 : dimensionValues1) {
            Map<S, List<Measure<?>>> measuresByDimensionValue2 = measuresByDimensionValue1.get(dimensionValue1);
            List<Long> measureValues = new ArrayList<>();
            List<Long> cumulativeMeasureValues = new ArrayList<>();

            if (measuresByDimensionValue2 != null) {
                List<Measure<?>> cumulativeMeasures = new ArrayList<>();

                // Iterate values of second dimension type
                for (S dimensionValue2 : dimensionValues2) {
                    List<Measure<?>> measures = measuresByDimensionValue2.get(dimensionValue2);

                    // Measure values
                    measureValues.add(getMeasureValue(measures, measureType));

                    // Cumulative measures
                    if ((measures != null) && !measures.isEmpty()) {
                        cumulativeMeasures.addAll(measures);
                    }

                    // Cumulative measure values
                    cumulativeMeasureValues.add(getMeasureValue(cumulativeMeasures, measureType));
                }
            }

            if (!measureValues.isEmpty()) {
                List<Measure<?>> measures = dimensionTotalMeasures1.get(dimensionValue1);
                long total = getMeasureValue(measures, measureType);

                measureValueEntities.add(resultMetricsQuadFunction.apply(dimensionValue1, measureValues, cumulativeMeasureValues, total));
            }
        }

        return measureValueEntities;
    }

    /**
     * Gets sub measure value entities.
     *
     * @param dimensionValues1             values of first dimension type
     * @param dimensionValues2             values of second dimension type
     * @param dimensionValues3             values of third dimension type
     * @param measureType                  measure type
     * @param subMeasuresByDimensionValue1 sub measures by first dimension value
     * @param resultSubMetricsBiFunction   result sub metrics element function
     * @param <T>                          first dimension type
     * @param <S>                          second dimension type
     * @param <U>                          third dimension type
     * @param <V>                          result sub metrics element type
     * @return sub measure value entities
     */
    private <T, S, U, V> List<V> getSubMeasureValueEntities(List<T> dimensionValues1,
                                                            List<S> dimensionValues2,
                                                            List<U> dimensionValues3,
                                                            MeasureType measureType,
                                                            Map<T, Map<U, Map<S, List<Measure<?>>>>> subMeasuresByDimensionValue1,
                                                            BiFunction<T, Map<U, List<Long>>, V> resultSubMetricsBiFunction) {
        if (dimensionValues3.isEmpty()) {
            return Collections.emptyList();
        }

        List<V> subMeasureValueEntities = new ArrayList<>();

        // Iterate values of first dimension type
        for (T dimensionValue1 : dimensionValues1) {
            Map<U, Map<S, List<Measure<?>>>> subMeasuresByDimensionValue2 = subMeasuresByDimensionValue1.get(dimensionValue1);
            Map<U, List<Long>> measureValues = new HashMap<>();

            if (subMeasuresByDimensionValue2 != null) {
                // Iterate values of third dimension type
                for (U dimensionValue3 : dimensionValues3) {
                    Map<S, List<Measure<?>>> subMeasuresByDimensionValue3 = subMeasuresByDimensionValue2.get(dimensionValue3);

                    if (subMeasuresByDimensionValue3 != null) {
                        // Iterate values of second dimension type
                        iterateDimensionValues2(dimensionValues2, subMeasuresByDimensionValue3, measureType, measureValues, dimensionValue3);
                    }
                }
            }

            if (!measureValues.isEmpty()) {
                subMeasureValueEntities.add(resultSubMetricsBiFunction.apply(dimensionValue1, measureValues));
            }
        }

        return subMeasureValueEntities;
    }

    /**
     * Iterates values of second dimension type.
     *
     * @param dimensionValues2             values of second dimension type
     * @param subMeasuresByDimensionValue3 sub measures by second dimension value
     * @param measureType                  measure type
     * @param measureValues                measure values
     * @param dimensionValue3              third dimension value
     * @param <S>                          second dimension type
     * @param <U>                          third dimension type
     */
    private <S, U> void iterateDimensionValues2(List<S> dimensionValues2,
                                                Map<S, List<Measure<?>>> subMeasuresByDimensionValue3,
                                                MeasureType measureType,
                                                Map<U, List<Long>> measureValues,
                                                U dimensionValue3) {
        List<Long> subMeasureValues = new ArrayList<>();

        for (S dimensionValue2 : dimensionValues2) {
            List<Measure<?>> measures = subMeasuresByDimensionValue3.get(dimensionValue2);

            subMeasureValues.add(getMeasureValue(measures, measureType));
        }

        if (!subMeasureValues.isEmpty()) {
            measureValues.put(dimensionValue3, subMeasureValues);
        }
    }

    /**
     * Fill totals.
     *
     * @param dimensionValues2        values of second dimension type
     * @param dimensionTotalMeasures2 total measures of second dimension
     * @param measureType             measure type
     * @param totals                  totals
     * @param cumulativeTotals        cumulative totals
     * @param allTotalMeasures        all total measures
     * @param <S>                     second dimension type
     */
    private <S> void fillTotals(List<S> dimensionValues2,
                                Map<S, List<Measure<?>>> dimensionTotalMeasures2,
                                MeasureType measureType, List<Long> totals, List<Long> cumulativeTotals,
                                List<Measure<?>> allTotalMeasures) {
        List<Measure<?>> cumulativeMeasures = new ArrayList<>();

        for (S dimensionValue2 : dimensionValues2) {
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
