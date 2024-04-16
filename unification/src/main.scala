package main
import lib.unify.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    var SameLen = AST.BinOp(OP.And,
      AST.BinOp(
        OP.Eq,
        AST.Var(0),
        AST.Var(1)
      ),
      AST.BinOp(
        OP.Eq,
        AST.Var(1),
        AST.Var(2)
      )
    )
    
    var vars: ArrayBuffer[Var] = ArrayBuffer(Option.empty, Option.empty, Option.empty)
    var env = Env(vars)

    var (sat, ties) = Union.unify(SameLen, env)
    var sol = env.applied(ties)
    println(sat)
    println(sol.vars)
  }
}