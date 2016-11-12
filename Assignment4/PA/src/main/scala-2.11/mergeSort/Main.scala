package mergeSort

import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import scalaz.EphemeralStream

import java.io._
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.Sorting

/**
  * Created by hyarragonda on 10/23/16.
  */
object Main {

  /**
    * limits number of sorting can be executed simultaneously.
    */
  private val GLOBAL_THREAD_LIMIT = {
    val ret = Runtime.getRuntime.availableProcessors() / 2
    if (ret > 5) {
      5
    } else {
      ret
    }
  }

  private lazy implicit val executionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(GLOBAL_THREAD_LIMIT))

  def sort(inputStream: InputStream, chunkSize: Int): Future[File] = {

    // open source stream
    val source: Stream[Int] = Source.fromInputStream(inputStream)
      .getLines()
      .map(_.toInt)
      .toStream

    val linesStream: EphemeralStream[EphemeralStream[Int]] = lift(source, chunkSize)
    val chunkCounter = new AtomicInteger(0)

    val sortedFileDir = Files.createTempDir()
    sortedFileDir.deleteOnExit()

    // read source stream, read n entries into memory and save it to file in parallel.
    val saveTmpFiles: Future[List[File]] = Future.sequence(
      linesStream.map(s => {
        val chunk = chunkCounter.getAndIncrement
        Future {
          println("sorting chunk: " + chunk)


          val ret = new File(sortedFileDir, "%d".format(chunk * chunkSize))
          val out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(ret)))


          println("finished sorting chunk: " + chunk)
          ret
        }
      }).toList
    )

    println("start merging...")

    // perform merge sort.
    saveTmpFiles.map {
      files => {
        var merged = files
        while (merged.length > 1) {
          val splited = merged.splitAt(merged.length / 2)
          val tuple = splited._1.zip(splited._2)

          val m2 = tuple.map {
            case (f1, f2) => {
              println(s"merging ${f1.getPath} ${f2.getPath}")
              val ret = new File(sortedFileDir, UUID.randomUUID().toString)

              val source1 = Source.fromFile(f1)
              val source2 = Source.fromFile(f2)
              val out = new PrintWriter(ret)

              try {
                val stream1 = source1.getLines().map(_.toInt).toIterable
                val stream2 = source2.getLines().map(_.toInt).toIterable
                merge(stream1, stream2).foreach(out.println(_))
                ret
              } finally {
                out.close()
                source1.close()
                source2.close()

                FileUtils.deleteQuietly(f1)
                FileUtils.deleteQuietly(f2)
              }

            }
          }
          merged = if (merged.length % 2 > 0) {
            m2 :+ merged.last
          } else {
            m2
          }
        }
        merged.head
      }
    }
  }

  /**
    * Lift a Stream into a Stream of Stream. The size of each sub-stream is specified
    * by the chunkSize.
    *
    * @param stream        the origin stream.
    * @param chunkSize     the size of each substream
    * @tparam A
    * @return              chunked stream of the original stream.
    */
  private def lift[A](stream: Stream[A], chunkSize: Int): EphemeralStream[EphemeralStream[A]] = {

    def tailFn(remaining: Iterable[A]): EphemeralStream[EphemeralStream[A]] = {
      if (remaining.isEmpty) {
        EphemeralStream.emptyEphemeralStream
      } else {
        val (head, tail) = remaining.splitAt(chunkSize)
        EphemeralStream.cons(EphemeralStream.fromStream(head.toStream), tailFn(tail))
      }
    }
    val (head, tail) = stream.splitAt(chunkSize)
    return EphemeralStream.cons(EphemeralStream.fromStream(head), tailFn(tail))
  }


  /**
    * Merge two streams into one stream.
    * @param iteratorA
    * @param iteratorB
    * @return
    */
  private def merge[A](iteratorA: Iterable[A], iteratorB: Iterable[A])(implicit ord: Ordering[A]): Stream[A] = {

    (iteratorA.isEmpty, iteratorB.isEmpty) match {
      case (true, true) => Stream.Empty
      case (false, true) => iteratorA.toStream
      case (true, false) => iteratorB.toStream
      case _ => {
        def a = iteratorA.head
        def b = iteratorB.head

        if (ord.compare(a, b) > 0) {
          Stream.cons(a, merge(iteratorA.tail, iteratorB))
        } else {
          Stream.cons(b, merge(iteratorA, iteratorB.tail))
        }
      }
    }
  }
}
