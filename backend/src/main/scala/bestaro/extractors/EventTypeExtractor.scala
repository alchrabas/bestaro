package bestaro.extractors

import java.io.{FileInputStream, ObjectInputStream}
import java.util
import java.util.logging.Level
import java.util.regex.Pattern

import bestaro.common.types.EventType
import bestaro.core.processors.BaseNameProducer
import bestaro.core.{RawRecord, Tokenizer}
import cc.mallet.classify.{Classifier, ClassifierTrainer, MaxEntTrainer}
import cc.mallet.pipe._
import cc.mallet.types.Instance
import cc.mallet.util.{MalletLogger, MalletProgressMessageLogger}

object EventTypeExtractor {
  val SERIALIZED_CLASSIFIER_PATH = "eventTypeClassifier.bin"
}

class EventTypeExtractor {

  private val pipe: Pipe = createPipeline()
  private var classifier: Option[Classifier] = None

  muteLoggers()

  def classify(record: RawRecord): RawRecord = {
    if (classifier.isEmpty) {
      classifier = Some(deserializeClassifier())
    }

    val instance = classifier.get.getInstancePipe
      .instanceFrom(new Instance(stemmizeMessage(record.message), null, record.recordId.toString, null))

    val classificationResult = classifier.get.classify(instance)
    println(classificationResult.getLabeling.getBestLabel + ": " +
      "%.3f".format(classificationResult.getLabeling.getBestValue))

    record.copy(eventType = EventType.withName(
      classificationResult.getLabeling.getBestLabel.toString))
  }

  private def deserializeClassifier(): Classifier = {
    val ois = new ObjectInputStream(new FileInputStream(EventTypeExtractor.SERIALIZED_CLASSIFIER_PATH))
    val _classifier = ois.readObject.asInstanceOf[Classifier]
    ois.close()
    _classifier
  }

  def stemmizeRecordMessage(record: RawRecord): String = {
    stemmizeMessage(record.message + "\n" + record.secondaryMessage)
  }

  def stemmizeMessage(text: String): String = {
    val tokenizer = new Tokenizer()
    val baseNameProducer = new BaseNameProducer
    tokenizer.tokenize(text).map(baseNameProducer.getBestBaseName).mkString(" ")
  }

  def createPipeline(): Pipe = {
    val pipeList = new util.ArrayList[Pipe]()
    pipeList.add(new Input2CharSequence("UTF-8"))

    val tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+")
    pipeList.add(new CharSequence2TokenSequence(tokenPattern))
    pipeList.add(new TokenSequenceLowercase)
    // Removed stopwords, because they didn't have any positive impact
    pipeList.add(new TokenSequence2FeatureSequence)
    pipeList.add(new Target2Label)
    pipeList.add(new FeatureSequence2FeatureVector)
    new SerialPipes(pipeList)
  }

  private def muteLoggers(): Unit = {
    val malletLogger = MalletLogger.getLogger("")
    malletLogger.setLevel(Level.OFF)
    val malletProgressLogger = MalletProgressMessageLogger.getLogger("")
    malletProgressLogger.setLevel(Level.OFF)
  }

  def createTrainer(): ClassifierTrainer[Classifier] = {
    new MaxEntTrainer().asInstanceOf[ClassifierTrainer[Classifier]]
  }
}
