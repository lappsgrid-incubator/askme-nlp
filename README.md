# Stanford Core NLP Service

The Stanford Core NLP service exposes several annotation pipelines for use by external services. They are:

1. *Segmentation* provides `Token` and `Sentence` annotations.
1. *Part of Speech Tagging* adds *pos* tags to tokens.
1. *Lemmatization* adds *lemma* tags to the tokens.
1. *Named Entity Recognition* provides `NamedEntity` annotations. 

Each of the pipelines executes all the annotators from the previous pipelines, that is, the named entity recognizer also produces token and sentence annotions with lemmas and part of speech tags.
  

## Connection Details

**Host** *rabbitmq.lappsgrid.org/nlp*<br/>
**Exchange** *stanford*<br/>
**Mailbox** *pipelines*

| Property | Value |
|------|-----------------------|
| Host | rabbitmq.lappsgrid.org|
| Exchange | stanford |
| Mailbox | pipelines |


```
PostOffice po = new PostOffice("stanford", "rabbitmq.lappsgrid.org/nlp");
Message message = new Message()
...
message.route("pipelines")
po.send(message)
```

## Messages

