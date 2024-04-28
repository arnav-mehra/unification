package main
import lib.types.*
import lib.solver.*
import lib.parser.*
import lib.unifier.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      println("Please add a file to run")
      System.exit(1)
    }

    val fname: String = args(0) + ".aunif"
    println("Running: " + fname)
    
    val code: String = scala.io.Source.fromFile(fname).mkString
    println(code)

    val (pfns, pqs) = ProgramParser.parse(code)
    var (_, q_ls) = Indexer(pfns, pqs).digest

    for (q <- q_ls) {
      Unifier(q.callee, q.args).unify()
    }
  }
}