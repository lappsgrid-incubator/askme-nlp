package org.lappsgrid.eager.mining.web.nlp.stanford


import org.junit.Test
import org.lappsgrid.askme.nlp.Pipeline
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 *
 */
class PipelineTest {

    public static final String TEXT = "Karen flew to New York. Nancy flew to Bloomington."
//    Pipeline pipeline

//    public static Data data

//    @BeforeClass
//    static void init() {
//        Container container = new Container()
//        container.text = TEXT
//        container.language = "en"
//        data = new Data(Uri.LIF, container)
//    }
//    @Before
//    void setup() {
//        pipeline = new Pipeline()
//    }
//
//    @After
//    void teardown() {
//        pipeline = null
//    }

    @Test
    void segments() {
        Pipeline pipeline = Pipeline.Segmenter()
        Container container = pipeline.process(TEXT)
        assert 2 == container.views.size()
        List<View> views = container.findViewsThatContain(Uri.SENTENCE)
        assert 1 == views.size()
        assert 2 == views[0].annotations.size()
        views = container.findViewsThatContain(Uri.TOKEN)
        assert 1 == views.size()
        assert 11 == views[0].annotations.size()
    }

    @Test
    void container() {
        Container container = new Container()
        container.text = TEXT

        Pipeline pipeline = Pipeline.Segmenter()
        container = pipeline.process(container)
        assert 2 == container.views.size()
        List<View> views = container.findViewsThatContain(Uri.SENTENCE)
        assert 1 == views.size()
        assert 2 == views[0].annotations.size()

        views = container.findViewsThatContain(Uri.TOKEN)
        assert 1 == views.size()
        assert 11 == views[0].annotations.size()
    }

    @Test
    void pos() {
        Pipeline pipeline = Pipeline.Tagger()
        Container container = pipeline.process(TEXT)
        println Serializer.toPrettyJson(container)
        assert 2 == container.views.size()

        List<View> views = container.findViewsThatContain(Uri.POS)
        assert 1 == views.size()
        assert 11 == views[0].annotations.size()
        container.views[1].annotations.each { Annotation a ->
            assert a.features.pos != null
            assert a.features.lemma == null
        }
    }

    @Test
    void lemmas() {
        Pipeline pipeline = Pipeline.Lemmatizer()
        Container container = pipeline.process(TEXT)
        assert 2 == container.views.size()
        assert 11 == container.views[1].annotations.size()
        container.views[1].annotations.each { Annotation a ->
            assert a.features.pos != null
            assert a.features.lemma != null
        }
    }

    @Test
    void ner() {
        Pipeline pipeline = Pipeline.NamedEntityRecognizer()
        Container container = pipeline.process(TEXT)
        assert 3 == container.views.size()
        assert 4 == container.views[-1].annotations.size()
    }

}
