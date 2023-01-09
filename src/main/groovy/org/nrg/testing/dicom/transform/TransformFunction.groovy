package org.nrg.testing.dicom.transform

import org.dcm4che3.data.DatasetWithFMI

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

/**
 * Class for defining a function to apply to a list of DICOM objects to transform them into a new list.
 * The static methods allow increasingly strict assumptions about the transformation desired with the payoff of simpler
 * function implementations.
 */
class TransformFunction {

    Function<List<DatasetWithFMI>, List<DatasetWithFMI>> function
    public static final TransformFunction IDENTITY = simple(new Consumer<DatasetWithFMI>() {
        @Override
        void accept(DatasetWithFMI datasetWithFMI) {}
    })

    TransformFunction(Function<List<DatasetWithFMI>, List<DatasetWithFMI>> function) {
        this.function = function
    }

    List<DatasetWithFMI> apply(List<DatasetWithFMI> input) {
        function.apply(input)
    }

    static TransformFunction generalTransform(Function<List<DatasetWithFMI>, List<DatasetWithFMI>> function) {
        new TransformFunction(function)
    }

    static TransformFunction generateFromScratch(Supplier<List<DatasetWithFMI>> function) {
        generalTransform(
                (List<DatasetWithFMI> ignored) -> {
                    function.get()
                }
        )
    }

    static TransformFunction composition(TransformFunction... transforms) {
        generalTransform(
                (List<DatasetWithFMI> listOfDicom) -> {
                    List<DatasetWithFMI> current = listOfDicom
                    transforms.each { transform ->
                        current = transform.apply(current)
                    }
                    current
                }
        )
    }

    // if we're going to modify the provided instances, but we can return the input list
    static TransformFunction strictlyTransformative(Consumer<List<DatasetWithFMI>> function) {
        generalTransform(
                (List<DatasetWithFMI> listOfDicom) -> {
                    function.accept(listOfDicom)
                    listOfDicom
                }
        )
    }

    // Same assumption as strictlyTransformative, but also assumes the instances can be transformed independently
    static TransformFunction simple(Consumer<DatasetWithFMI> function) {
        strictlyTransformative(
                (List<DatasetWithFMI> listOfDicom) -> {
                    listOfDicom.each { instance ->
                        function.accept(instance)
                    }
                }
        )
    }

}
