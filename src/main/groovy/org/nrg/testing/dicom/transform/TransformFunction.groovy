package org.nrg.testing.dicom.transform

import org.dcm4che3.data.Attributes

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

/**
 * Class for defining a function to apply to a list of DICOM objects to transform them into a new list.
 * The static methods allow increasingly strict assumptions about the transformation desired with the payoff of simpler
 * function implementations.
 */
class TransformFunction {

    Function<List<Attributes>, List<Attributes>> function
    public static final TransformFunction IDENTITY = simple(new Consumer<Attributes>() {
        @Override
        void accept(Attributes attributes) {}
    })

    TransformFunction(Function<List<Attributes>, List<Attributes>> function) {
        this.function = function
    }

    List<Attributes> apply(List<Attributes> input) {
        function.apply(input)
    }

    static TransformFunction generalTransform(Function<List<Attributes>, List<Attributes>> function) {
        new TransformFunction(function)
    }

    static TransformFunction generateFromScratch(Supplier<List<Attributes>> function) {
        generalTransform(
                (List<Attributes> ignored) -> {
                    function.get()
                }
        )
    }

    static TransformFunction composition(TransformFunction... transforms) {
        generalTransform(
                (List<Attributes> listOfDicom) -> {
                    List<Attributes> current = listOfDicom
                    transforms.each { transform ->
                        current = transform.apply(current)
                    }
                    current
                }
        )
    }

    // if we're going to modify the provided instances, but we can return the input list
    static TransformFunction strictlyTransformative(Consumer<List<Attributes>> function) {
        generalTransform(
                (List<Attributes> listOfDicom) -> {
                    function.accept(listOfDicom)
                    listOfDicom
                }
        )
    }

    // Same assumption as strictlyTransformative, but also assumes the instances can be transformed independently
    static TransformFunction simple(Consumer<Attributes> function) {
        strictlyTransformative(
                (List<Attributes> listOfDicom) -> {
                    listOfDicom.each { instance ->
                        function.accept(instance)
                    }
                }
        )
    }

}
