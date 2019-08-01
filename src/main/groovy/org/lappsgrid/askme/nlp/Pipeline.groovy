package org.lappsgrid.askme.nlp

import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.CoreEntityMention
import edu.stanford.nlp.pipeline.CoreSentence
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.Pair
import groovy.util.logging.Slf4j
import org.lappsgrid.serialization.LifException
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.*;
import org.lappsgrid.vocabulary.Features

/**
 *
 */
@Slf4j('logger')
public class Pipeline
{
    // Factory methods

    static public Pipeline Segmenter() {
        return new Pipeline("tokenize,ssplit")
    }
    static public Pipeline Tagger() {
        return new Pipeline("tokenize,ssplit,pos")
    }
    static public Pipeline Lemmatizer() {
        return new Pipeline("tokenize,ssplit,pos,lemma")
    }
    static public Pipeline NamedEntityRecognizer() {
        return new Pipeline("tokenize,ssplit,pos,lemma,ner")
    }

    private StanfordCoreNLP pipeline;

    protected Pipeline(String annotators) {
        Properties props = new Properties();
        props.setProperty("annotators", annotators);
        pipeline = new StanfordCoreNLP(props);
    }

    public Container process(String text) throws LifException
    {
        Container container = new Container();
        container.setText(text);
        return process(container);
    }

    public Container process(Container container) throws LifException
    {
        logger.debug("Processing container. Size: {}", container.text.length())
        CoreDocument document = new CoreDocument(container.getText());
        pipeline.annotate(document);


        // Process the sentences.
        logger.trace("processing sentences")
        int id = 0;
        View sentences = container.newView();
        for (CoreSentence s : document.sentences()) {
            Pair<Integer,Integer> offsets = s.charOffsets();
            sentences.newAnnotation("s-" + (id++), Uri.SENTENCE, offsets.first, offsets.second);
        }
        if (id > 0) {
            sentences.addContains(Uri.SENTENCE, this.getClass().getName(), "stanford");
        }

        // Process tokens. Include lemmas and part of speech.
        logger.trace('Processing tokens')
        View tokens = container.newView();
        id = 0;
        int pos = 0;
        int lemma = 0;
        for (CoreLabel token : document.tokens()) {
            int start = token.beginPosition();
            int end = token.endPosition();
            Annotation a = tokens.newAnnotation("tok-" + (id++), Uri.TOKEN, start, end);
            if (set(a, Features.Token.LEMMA, token.lemma())) ++lemma
            if (set(a, Features.Token.PART_OF_SPEECH, token.tag())) ++ pos
            set(a, Features.Token.WORD, token.word());
            set(a, "category", token.category());
        }
        if (id > 0) {
            tokens.addContains(Uri.TOKEN, this.getClass().getName(), "stanford");
        }
        if (pos > 0) {
            tokens.addContains(Uri.POS, this.class.name, "stanford")
        }
        if (lemma > 0) {
            tokens.addContains(Uri.LEMMA, this.class.name, 'stanford')
        }

        View ner = container.newView();
        id = 0;
        for (CoreEntityMention mention : document.entityMentions()) {
            Pair<Integer, Integer> offset = mention.charOffsets();
            Annotation a = ner.newAnnotation("ner-" + (id++), Uri.NE, offset.first, offset.second);
            a.addFeature(Features.NamedEntity.CATEGORY, mention.entityType());
        }
        if (id > 0) {
            ner.addContains(Uri.NE, this.getClass().getName(), "stanford");
        }
        else {
            container.views.remove(ner)
        }
//        return new Data(Uri.LIF, container).asPrettyJson();
        logger.debug('Processing complete')
        return container
    }

    protected boolean set(Annotation a, String key, String value) {
        if (value == null) {
            return false;
        }
        a.addFeature(key, value);
        return true;
    }
}
