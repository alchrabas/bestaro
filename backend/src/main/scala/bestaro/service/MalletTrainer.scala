package bestaro.service

import java.io.{File, FileOutputStream, ObjectOutputStream}

import bestaro.common.util.MathUtil
import bestaro.core.processors.BaseNameProducer
import bestaro.core.{JsonSerializer, Tokenizer}
import bestaro.extractors.EventTypeExtractor
import bestaro.helpers.TaggedRecordsManager
import cc.mallet.classify.{Classifier, ClassifierTrainer, Trial}
import cc.mallet.types.{Instance, InstanceList}
import cc.mallet.util.Randoms

import scala.collection.JavaConverters._

object MalletTrainer {
  def main(args: Array[String]): Unit = {
    val malletTrainer = new MalletTrainer()
    malletTrainer.generateTrainedClassifier()
  }
}

class MalletTrainer {
  def generateTrainedClassifier(): Unit = {
    val extractor = new EventTypeExtractor

    val trainer = extractor.createTrainer()
    val trainData = loadTrainData(extractor)

    val trainedClassifier = trainer.train(trainData)

    val classifierPath = EventTypeExtractor.SERIALIZED_CLASSIFIER_PATH
    saveClassifier(trainedClassifier, new File(classifierPath))

    printTrainingResults(trainer, trainData)
  }

  private def printTrainingResults(trainer: ClassifierTrainer[Classifier], trainData: InstanceList): Unit = {
    val trainingResults = (0 to 100).map { _ =>
      testCrossValidatedData(trainData, trainer)
    }

    println("Avg: " + MathUtil.avg(trainingResults))
    println("Std: " + MathUtil.stdDev(trainingResults))
  }

  def testCrossValidatedData(instances: InstanceList, trainer: ClassifierTrainer[Classifier]): Double = {
    val TRAINING = 0
    val TESTING = 1
    val instanceLists = instances.split(new Randoms, Array[Double](0.9, 0.1, 0.0))
    val classifier = trainer.train(instanceLists(TRAINING))

    val trial = new Trial(classifier, instanceLists(TESTING))
    trial.getAccuracy
  }

  private def saveClassifier(classifier: Classifier, serializedFile: File): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(serializedFile))
    oos.writeObject(classifier)
    oos.close()
  }

  private def loadTrainData(extractor: EventTypeExtractor): InstanceList = {
    val jsonSerializer = new JsonSerializer
    val records = jsonSerializer.readRecordsFromFile
    val tagsById = TaggedRecordsManager.allEventTypeRecordsFromCsv()
      .map(record => record.recordId -> record).toMap

    val instancesFromCsvIterator = records
      .filter(record => tagsById.contains(record.recordId))
      .map(record => (record, tagsById(record.recordId).eventType))
      .filter(recordAndTag => recordAndTag._2 != "" && recordAndTag._1.message != "")
      .map(recordAndTag => (recordAndTag._1, seenToFound(recordAndTag._2)))
      .map(recordAndTag => new Instance(stemmize(recordAndTag._1.message), recordAndTag._2, recordAndTag._1.recordId.toString, null))
    println(s"${instancesFromCsvIterator.size} are to be used")

    val instances = new InstanceList(extractor.createPipeline())

    instances.addThruPipe(instancesFromCsvIterator.toIterator.asJava)
    instances
  }

  private def stemmize(text: String): String = {
    val tokenizer = new Tokenizer()
    val baseNameProducer = new BaseNameProducer
    tokenizer.tokenize(text).map(baseNameProducer.getBestBaseName).mkString(" ")
  }

  private def seenToFound(eventType: String): String = {
    if (eventType == "SEEN") {
      "FOUND"
    } else {
      eventType
    }
  }

}
