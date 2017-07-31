package ontology.util;

import ontology.qual.Ontology;
import ontology.qual.OntologyValue;
import ontology.qual.PolyOntology;

import org.checkerframework.framework.util.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;
import org.checkerframework.javacutil.TypesUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class OntologyUtils {

    private static OntologyUtils singletonInstance;

    public static AnnotationMirror ONTOLOGY, ONTOLOGY_TOP, ONTOLOGY_BOTTOM, POLY_ONTOLOGY;

    private OntologyUtils(ProcessingEnvironment processingEnv, Elements elements) {
        ONTOLOGY_TOP = OntologyUtils.createOntologyAnnotationByValues(processingEnv, OntologyValue.TOP);
        ONTOLOGY_BOTTOM = OntologyUtils.createOntologyAnnotationByValues(processingEnv, OntologyValue.BOTTOM);
        ONTOLOGY = AnnotationUtils.fromClass(elements, Ontology.class);
        POLY_ONTOLOGY = AnnotationUtils.fromClass(elements, PolyOntology.class);
    }

    public static void initOntologyUtils (ProcessingEnvironment processingEnv, Elements elements) {
        if (singletonInstance == null) {
            singletonInstance = new OntologyUtils(processingEnv, elements);
        }
    }

    public static OntologyUtils getInstance() {
        if (singletonInstance == null) {
            ErrorReporter.errorAbort("getInstance() get called without initialization!");
        }
        return singletonInstance;
    }

    public static OntologyValue determineOntologyValue(TypeMirror type) {
        if (TypesUtils.isDeclaredOfName(type, "java.util.LinkedList")
                || TypesUtils.isDeclaredOfName(type, "java.util.ArrayList")
                || type.getKind().equals(TypeKind.ARRAY)) {
            return OntologyValue.SEQUENCE;
        }
        // cannot determine OntologyValue by the given type
        return OntologyValue.TOP;
    }

    public static AnnotationMirror createOntologyAnnotationByValues(ProcessingEnvironment processingEnv,
            OntologyValue... values) {
        validateOntologyValues(values);
        AnnotationBuilder builder = new AnnotationBuilder(processingEnv, Ontology.class);
        builder.setValue("values", values);
        return builder.build();
    }

    public static OntologyValue[] getOntologyValues(AnnotationMirror type) {
        List<OntologyValue> ontologyValueList = AnnotationUtils.getElementValueEnumArray(type, "values", OntologyValue.class, true);
        return ontologyValueList.toArray(new OntologyValue[ontologyValueList.size()]);
    }

    public static EnumSet<OntologyValue> lubOfOntologyValues(EnumSet<OntologyValue> valueSet1, EnumSet<OntologyValue> valueSet2) {
        EnumSet<OntologyValue> lub = EnumSet.noneOf(OntologyValue.class);

        for (OntologyValue value1 : valueSet1) {
            if (value1 == OntologyValue.TOP) {
                lub.clear();
                break;
            }
            if (valueSet2.contains(value1)) {
                lub.add(value1);
            }
        }

        if (lub.isEmpty()) {
            lub.add(OntologyValue.TOP);
        }

        return lub;
    }

    public static boolean isOntologyBottom(AnnotationMirror type) {
        if (!AnnotationUtils.areSameIgnoringValues(type, OntologyUtils.ONTOLOGY)) {
            return false;
        }

        EnumSet<OntologyValue> enumSet = EnumSet.noneOf(OntologyValue.class);
        enumSet.addAll(Arrays.asList(getOntologyValues(type)));

        return enumSet.size() == OntologyValue.values.length || enumSet.contains(OntologyValue.BOTTOM);
    }

    public static boolean isOntologyTop(AnnotationMirror type) {
        if (!AnnotationUtils.areSameIgnoringValues(type, OntologyUtils.ONTOLOGY)) {
            return false;
        }

        EnumSet<OntologyValue> enumSet = EnumSet.noneOf(OntologyValue.class);
        enumSet.addAll(Arrays.asList(getOntologyValues(type)));

        return enumSet.isEmpty() || enumSet.contains(OntologyValue.TOP);
    }
    /**
     * check whether the passed values are validated as arguments of Ontology qualifier
     * valid values should not be null, and contains at least one ontology value, and
     * doesn't cotains null element inside the array.
     * @param values the checking values
     */
    protected static void validateOntologyValues(OntologyValue... values) {
        if (values == null || values.length < 1) {
            ErrorReporter.errorAbort("ontology values are invalid: " + values);
        }
        for (OntologyValue value : values) {
            if (value == null) {
                ErrorReporter.errorAbort("ontology values are invalid: " + values);
            }
        }
    }
}
